package com.carl.trading.service;

import com.carl.trading.mapper.ExchangeMapper;
import com.carl.trading.mapper.HoldingMapper;
import com.carl.trading.model.Customer;
import com.carl.trading.model.Exchange;
import com.carl.trading.web.dto.HoldingDto;
import com.carl.trading.web.dto.PortfolioDto;
import com.carl.trading.web.dto.PortfolioRow;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PortfolioService {

    static final int MONEY_SCALE = 2;
    static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    private final HoldingMapper holdingMapper;
    private final ExchangeMapper exchangeMapper;
    private final ExchangeCalendar exchangeCalendar;

    public PortfolioService(HoldingMapper holdingMapper, ExchangeMapper exchangeMapper,
                            ExchangeCalendar exchangeCalendar) {
        this.holdingMapper = holdingMapper;
        this.exchangeMapper = exchangeMapper;
        this.exchangeCalendar = exchangeCalendar;
    }

    public PortfolioDto forCustomer(Customer customer) {
        List<PortfolioRow> rows = holdingMapper.findPortfolio(customer.id());

        Map<String, Boolean> openByExchange = new HashMap<>();
        Map<String, Exchange> exchangesByCode = exchangeMapper.findEnabled().stream()
                .collect(Collectors.toMap(Exchange::code, Function.identity()));

        List<HoldingDto> holdings = new ArrayList<>();
        BigDecimal totalMarketValue = BigDecimal.ZERO;
        BigDecimal totalCostBasis = BigDecimal.ZERO;

        for (PortfolioRow row : rows) {
            BigDecimal qty = BigDecimal.valueOf(row.quantity());
            BigDecimal marketValue = money(row.snapshotPriceUsd().multiply(qty));
            BigDecimal costBasis = money(row.avgCostBasisUsd().multiply(qty));
            BigDecimal pl = marketValue.subtract(costBasis);
            BigDecimal plPct = costBasis.signum() == 0
                    ? BigDecimal.ZERO
                    : pl.multiply(BigDecimal.valueOf(100)).divide(costBasis, MONEY_SCALE, ROUNDING);

            boolean exchangeOpen = openByExchange.computeIfAbsent(row.exchangeCode(),
                    code -> exchangeCalendar.isOpenNow(exchangesByCode.get(code)));

            holdings.add(new HoldingDto(
                    row.symbol(), row.exchangeCode(), row.companyName(), row.quantity(),
                    row.avgCostBasisUsd(), row.snapshotPriceUsd(),
                    marketValue, costBasis, pl, plPct, exchangeOpen));

            totalMarketValue = totalMarketValue.add(marketValue);
            totalCostBasis = totalCostBasis.add(costBasis);
        }

        BigDecimal cash = money(customer.cashBalanceUsd());
        return new PortfolioDto(
                cash,
                totalMarketValue,
                cash.add(totalMarketValue),
                totalCostBasis,
                totalMarketValue.subtract(totalCostBasis),
                holdings);
    }

    private static BigDecimal money(BigDecimal value) {
        return value.setScale(MONEY_SCALE, ROUNDING);
    }
}
