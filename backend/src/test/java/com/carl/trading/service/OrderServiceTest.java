package com.carl.trading.service;

import com.carl.trading.mapper.CustomerMapper;
import com.carl.trading.mapper.HoldingMapper;
import com.carl.trading.mapper.SecurityMapper;
import com.carl.trading.mapper.TransactionMapper;
import com.carl.trading.model.Customer;
import com.carl.trading.model.Holding;
import com.carl.trading.model.OrderType;
import com.carl.trading.model.Security;
import com.carl.trading.model.Side;
import com.carl.trading.model.TransactionRecord;
import com.carl.trading.web.dto.OrderRequest;
import com.carl.trading.web.dto.OrderResultDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    SecurityMapper securityMapper;
    @Mock
    HoldingMapper holdingMapper;
    @Mock
    CustomerMapper customerMapper;
    @Mock
    TransactionMapper transactionMapper;
    @InjectMocks
    OrderService orderService;

    private static Customer customer(String cash) {
        return new Customer(1L, "customer1", "hash", "Ann", "Bell", "111-22-3333",
                "1 Main Street", null, "New York", "NY", "10001", new BigDecimal(cash));
    }

    private static Security security(String priceUsd) {
        return new Security(10L, 2L, "NYSE", "IBM", "IBM Corp", "USD", new BigDecimal(priceUsd));
    }

    private TransactionRecord captureTx() {
        ArgumentCaptor<TransactionRecord> captor = ArgumentCaptor.forClass(TransactionRecord.class);
        verify(transactionMapper).insert(captor.capture());
        return captor.getValue();
    }

    @Test
    void marketBuy_withSufficientCash_fillsAndReducesCash() {
        when(securityMapper.findBySymbol("IBM")).thenReturn(security("100.0000"));
        when(holdingMapper.find(1L, 10L)).thenReturn(null);

        OrderResultDto result = orderService.place(customer("40000.00"),
                new OrderRequest("IBM", Side.BUY, OrderType.MARKET, 10, null));

        assertThat(result.status()).isEqualTo("FILLED");
        assertThat(result.executedPriceUsd()).isEqualByComparingTo("100.0000");
        assertThat(result.cashDeltaUsd()).isEqualByComparingTo("-1000.00");
        assertThat(result.newCashBalanceUsd()).isEqualByComparingTo("39000.00");

        verify(customerMapper).updateCash(1L, new BigDecimal("39000.00"));
        ArgumentCaptor<Holding> holding = ArgumentCaptor.forClass(Holding.class);
        verify(holdingMapper).insert(holding.capture());
        assertThat(holding.getValue().quantity()).isEqualTo(10);
        assertThat(holding.getValue().avgCostBasisUsd()).isEqualByComparingTo("100.0000");
        assertThat(captureTx().status()).isEqualTo("FILLED");
    }

    @Test
    void marketBuy_withInsufficientCash_isRejectedAndLoggedWithNoStateChange() {
        when(securityMapper.findBySymbol("IBM")).thenReturn(security("100.0000"));

        OrderResultDto result = orderService.place(customer("500.00"),
                new OrderRequest("IBM", Side.BUY, OrderType.MARKET, 10, null));

        assertThat(result.status()).isEqualTo("REJECTED");
        assertThat(result.reason()).contains("Insufficient cash");
        assertThat(result.newCashBalanceUsd()).isEqualByComparingTo("500.00");

        verify(customerMapper, never()).updateCash(anyLong(), any());
        verify(holdingMapper, never()).insert(any());
        verify(holdingMapper, never()).update(any());
        assertThat(captureTx().status()).isEqualTo("REJECTED");
    }

    @Test
    void marketBuy_intoExistingHolding_recomputesWeightedAverageCost() {
        when(securityMapper.findBySymbol("IBM")).thenReturn(security("110.0000"));
        when(holdingMapper.find(1L, 10L))
                .thenReturn(new Holding(7L, 1L, 10L, 10, new BigDecimal("90.0000")));

        orderService.place(customer("40000.00"),
                new OrderRequest("IBM", Side.BUY, OrderType.MARKET, 10, null));

        ArgumentCaptor<Holding> holding = ArgumentCaptor.forClass(Holding.class);
        verify(holdingMapper).update(holding.capture());
        assertThat(holding.getValue().quantity()).isEqualTo(20);
        assertThat(holding.getValue().avgCostBasisUsd()).isEqualByComparingTo("100.0000");
    }

    @Test
    void marketSell_moreSharesThanHeld_isRejectedAndLogged() {
        when(securityMapper.findBySymbol("IBM")).thenReturn(security("100.0000"));
        when(holdingMapper.find(1L, 10L))
                .thenReturn(new Holding(7L, 1L, 10L, 5, new BigDecimal("80.0000")));

        OrderResultDto result = orderService.place(customer("40000.00"),
                new OrderRequest("IBM", Side.SELL, OrderType.MARKET, 10, null));

        assertThat(result.status()).isEqualTo("REJECTED");
        assertThat(result.reason()).contains("Insufficient shares");
        verify(customerMapper, never()).updateCash(anyLong(), any());
        verify(holdingMapper, never()).update(any());
        verify(holdingMapper, never()).delete(anyLong());
        assertThat(captureTx().status()).isEqualTo("REJECTED");
    }

    @Test
    void marketSell_partial_reducesQuantityAndAddsCash() {
        when(securityMapper.findBySymbol("IBM")).thenReturn(security("100.0000"));
        when(holdingMapper.find(1L, 10L))
                .thenReturn(new Holding(7L, 1L, 10L, 10, new BigDecimal("80.0000")));

        OrderResultDto result = orderService.place(customer("40000.00"),
                new OrderRequest("IBM", Side.SELL, OrderType.MARKET, 4, null));

        assertThat(result.status()).isEqualTo("FILLED");
        assertThat(result.cashDeltaUsd()).isEqualByComparingTo("400.00");
        verify(customerMapper).updateCash(1L, new BigDecimal("40400.00"));
        ArgumentCaptor<Holding> holding = ArgumentCaptor.forClass(Holding.class);
        verify(holdingMapper).update(holding.capture());
        assertThat(holding.getValue().quantity()).isEqualTo(6);
        verify(holdingMapper, never()).delete(anyLong());
    }

    @Test
    void marketSell_entirePosition_deletesHolding() {
        when(securityMapper.findBySymbol("IBM")).thenReturn(security("100.0000"));
        when(holdingMapper.find(1L, 10L))
                .thenReturn(new Holding(7L, 1L, 10L, 10, new BigDecimal("80.0000")));

        orderService.place(customer("40000.00"),
                new OrderRequest("IBM", Side.SELL, OrderType.MARKET, 10, null));

        verify(holdingMapper).delete(7L);
        verify(holdingMapper, never()).update(any());
    }

    @Test
    void limitBuy_atOrAboveMarket_fillsAtSnapshotPrice() {
        when(securityMapper.findBySymbol("IBM")).thenReturn(security("100.0000"));
        when(holdingMapper.find(1L, 10L)).thenReturn(null);

        OrderResultDto result = orderService.place(customer("40000.00"),
                new OrderRequest("IBM", Side.BUY, OrderType.LIMIT, 10, new BigDecimal("105.00")));

        assertThat(result.status()).isEqualTo("FILLED");
        assertThat(result.executedPriceUsd()).isEqualByComparingTo("100.0000");
        assertThat(result.limitPriceUsd()).isEqualByComparingTo("105.00");
    }

    @Test
    void limitBuy_belowMarket_isRejectedAndLogged() {
        when(securityMapper.findBySymbol("IBM")).thenReturn(security("100.0000"));

        OrderResultDto result = orderService.place(customer("40000.00"),
                new OrderRequest("IBM", Side.BUY, OrderType.LIMIT, 10, new BigDecimal("95.00")));

        assertThat(result.status()).isEqualTo("REJECTED");
        assertThat(result.reason()).contains("below market price");
        verify(customerMapper, never()).updateCash(anyLong(), any());
        verify(holdingMapper, never()).insert(any());
        assertThat(captureTx().status()).isEqualTo("REJECTED");
    }

    @Test
    void limitSell_atOrBelowMarket_fills() {
        when(securityMapper.findBySymbol("IBM")).thenReturn(security("100.0000"));
        when(holdingMapper.find(1L, 10L))
                .thenReturn(new Holding(7L, 1L, 10L, 10, new BigDecimal("80.0000")));

        OrderResultDto result = orderService.place(customer("40000.00"),
                new OrderRequest("IBM", Side.SELL, OrderType.LIMIT, 5, new BigDecimal("95.00")));

        assertThat(result.status()).isEqualTo("FILLED");
        assertThat(result.executedPriceUsd()).isEqualByComparingTo("100.0000");
    }

    @Test
    void limitSell_aboveMarket_isRejectedAndLogged() {
        when(securityMapper.findBySymbol("IBM")).thenReturn(security("100.0000"));

        OrderResultDto result = orderService.place(customer("40000.00"),
                new OrderRequest("IBM", Side.SELL, OrderType.LIMIT, 5, new BigDecimal("105.00")));

        assertThat(result.status()).isEqualTo("REJECTED");
        assertThat(result.reason()).contains("above market price");
        verify(holdingMapper, never()).update(any());
        verify(holdingMapper, never()).delete(anyLong());
        assertThat(captureTx().status()).isEqualTo("REJECTED");
    }
}
