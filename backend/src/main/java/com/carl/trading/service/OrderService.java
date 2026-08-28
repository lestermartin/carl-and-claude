package com.carl.trading.service;

import com.carl.trading.mapper.CustomerMapper;
import com.carl.trading.mapper.HoldingMapper;
import com.carl.trading.mapper.SecurityMapper;
import com.carl.trading.mapper.TransactionMapper;
import com.carl.trading.model.Customer;
import com.carl.trading.model.Holding;
import com.carl.trading.model.OrderStatus;
import com.carl.trading.model.OrderType;
import com.carl.trading.model.Security;
import com.carl.trading.model.Side;
import com.carl.trading.model.TransactionRecord;
import com.carl.trading.web.dto.OrderRequest;
import com.carl.trading.web.dto.OrderResultDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;

/**
 * Deliberately minimal order engine:
 * <ul>
 *   <li>MARKET orders execute at the current snapshot price.</li>
 *   <li>LIMIT orders execute at the snapshot price when the limit is favorable, otherwise they are
 *       rejected and the decision is still written to the transaction log.</li>
 *   <li>The only business rules are "enough cash to buy" and "enough shares to sell".</li>
 * </ul>
 */
@Service
public class OrderService {

    private static final int MONEY_SCALE = 2;
    private static final int PRICE_SCALE = 4;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    private final SecurityMapper securityMapper;
    private final HoldingMapper holdingMapper;
    private final CustomerMapper customerMapper;
    private final TransactionMapper transactionMapper;

    public OrderService(SecurityMapper securityMapper, HoldingMapper holdingMapper,
                        CustomerMapper customerMapper, TransactionMapper transactionMapper) {
        this.securityMapper = securityMapper;
        this.holdingMapper = holdingMapper;
        this.customerMapper = customerMapper;
        this.transactionMapper = transactionMapper;
    }

    @Transactional
    public OrderResultDto place(Customer customer, OrderRequest request) {
        Security security = securityMapper.findBySymbol(request.symbol());
        if (security == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Unknown symbol: " + request.symbol());
        }
        if (request.quantity() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be positive");
        }

        BigDecimal snapshot = security.snapshotPriceUsd();
        BigDecimal limit = request.limitPriceUsd();
        if (request.orderType() == OrderType.LIMIT) {
            if (limit == null || limit.signum() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "A positive limitPriceUsd is required for LIMIT orders");
            }
        } else {
            limit = null;
        }

        long qty = request.quantity();
        BigDecimal qtyBd = BigDecimal.valueOf(qty);

        // 1. Limit-price gate.
        String rejectReason = limitRejectReason(request.side(), request.orderType(), limit, snapshot);
        if (rejectReason != null) {
            return persistRejected(customer, security, request, limit, rejectReason);
        }

        // 2. Business-rule gate + execution. Everything fills at the snapshot price.
        BigDecimal notional = money(snapshot.multiply(qtyBd));

        if (request.side() == Side.BUY) {
            if (notional.compareTo(customer.cashBalanceUsd()) > 0) {
                return persistRejected(customer, security, request, limit,
                        "Insufficient cash: need " + notional + " USD, have "
                                + money(customer.cashBalanceUsd()) + " USD");
            }
            BigDecimal newCash = money(customer.cashBalanceUsd().subtract(notional));
            customerMapper.updateCash(customer.id(), newCash);
            applyBuyToHolding(customer.id(), security.id(), qty, snapshot);
            return persistFilled(customer, security, request, limit, snapshot, qty,
                    notional.negate(), newCash);
        }

        // SELL
        Holding holding = holdingMapper.find(customer.id(), security.id());
        if (holding == null || holding.quantity() < qty) {
            long have = holding == null ? 0 : holding.quantity();
            return persistRejected(customer, security, request, limit,
                    "Insufficient shares: have " + have + ", tried to sell " + qty);
        }
        BigDecimal newCash = money(customer.cashBalanceUsd().add(notional));
        customerMapper.updateCash(customer.id(), newCash);
        long remaining = holding.quantity() - qty;
        if (remaining == 0) {
            holdingMapper.delete(holding.id());
        } else {
            holdingMapper.update(new Holding(holding.id(), holding.customerId(), holding.securityId(),
                    remaining, holding.avgCostBasisUsd()));
        }
        return persistFilled(customer, security, request, limit, snapshot, qty, notional, newCash);
    }

    /** @return a rejection reason if the limit price is unfavorable, otherwise {@code null}. */
    private static String limitRejectReason(Side side, OrderType type, BigDecimal limit, BigDecimal snapshot) {
        if (type != OrderType.LIMIT) {
            return null;
        }
        if (side == Side.BUY && limit.compareTo(snapshot) < 0) {
            return "Limit price " + limit + " is below market price " + snapshot;
        }
        if (side == Side.SELL && limit.compareTo(snapshot) > 0) {
            return "Limit price " + limit + " is above market price " + snapshot;
        }
        return null;
    }

    private void applyBuyToHolding(long customerId, long securityId, long qty, BigDecimal price) {
        Holding existing = holdingMapper.find(customerId, securityId);
        if (existing == null) {
            holdingMapper.insert(new Holding(null, customerId, securityId, qty,
                    price.setScale(PRICE_SCALE, ROUNDING)));
            return;
        }
        long newQty = existing.quantity() + qty;
        BigDecimal existingCost = existing.avgCostBasisUsd().multiply(BigDecimal.valueOf(existing.quantity()));
        BigDecimal addedCost = price.multiply(BigDecimal.valueOf(qty));
        BigDecimal newAvg = existingCost.add(addedCost)
                .divide(BigDecimal.valueOf(newQty), PRICE_SCALE, ROUNDING);
        holdingMapper.update(new Holding(existing.id(), customerId, securityId, newQty, newAvg));
    }

    private OrderResultDto persistFilled(Customer customer, Security security, OrderRequest request,
                                         BigDecimal limit, BigDecimal executionPrice, long qty,
                                         BigDecimal cashDelta, BigDecimal newCash) {
        OffsetDateTime now = OffsetDateTime.now();
        BigDecimal execPrice = executionPrice.setScale(PRICE_SCALE, ROUNDING);
        transactionMapper.insert(new TransactionRecord(
                null, customer.id(), security.id(), security.symbol(), security.exchangeCode(),
                request.side().name(), request.orderType().name(), OrderStatus.FILLED.name(),
                qty, limit, execPrice, money(cashDelta), null, now));
        return new OrderResultDto(
                OrderStatus.FILLED.name(), request.side().name(), request.orderType().name(),
                security.symbol(), security.exchangeCode(), qty, limit, execPrice,
                money(cashDelta), newCash, null, now);
    }

    private OrderResultDto persistRejected(Customer customer, Security security, OrderRequest request,
                                           BigDecimal limit, String reason) {
        OffsetDateTime now = OffsetDateTime.now();
        transactionMapper.insert(new TransactionRecord(
                null, customer.id(), security.id(), security.symbol(), security.exchangeCode(),
                request.side().name(), request.orderType().name(), OrderStatus.REJECTED.name(),
                request.quantity(), limit, null, BigDecimal.ZERO.setScale(MONEY_SCALE), reason, now));
        return new OrderResultDto(
                OrderStatus.REJECTED.name(), request.side().name(), request.orderType().name(),
                security.symbol(), security.exchangeCode(), request.quantity(), limit, null,
                BigDecimal.ZERO.setScale(MONEY_SCALE), money(customer.cashBalanceUsd()), reason, now);
    }

    private static BigDecimal money(BigDecimal value) {
        return value.setScale(MONEY_SCALE, ROUNDING);
    }
}
