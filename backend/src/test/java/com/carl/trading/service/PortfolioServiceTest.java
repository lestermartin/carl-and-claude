package com.carl.trading.service;

import com.carl.trading.mapper.HoldingMapper;
import com.carl.trading.model.Customer;
import com.carl.trading.web.dto.PortfolioDto;
import com.carl.trading.web.dto.PortfolioRow;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceTest {

    @Mock
    HoldingMapper holdingMapper;
    @InjectMocks
    PortfolioService portfolioService;

    private static Customer customer(String cash) {
        return new Customer(1L, "customer1", "hash", "Ann", "Bell", "111-22-3333",
                "1 Main Street", null, "New York", "NY", "10001", new BigDecimal(cash));
    }

    @Test
    void computesPerHoldingAndAccountTotals() {
        when(holdingMapper.findPortfolio(1L)).thenReturn(List.of(
                new PortfolioRow(10L, "IBM", "NYSE", "IBM Corp", "USD", 10,
                        new BigDecimal("90.0000"), new BigDecimal("100.0000")),
                new PortfolioRow(11L, "BP", "LSE", "BP plc", "GBP", 100,
                        new BigDecimal("6.0000"), new BigDecimal("5.5000"))));

        PortfolioDto dto = portfolioService.forCustomer(customer("40000.00"));

        assertThat(dto.holdings()).hasSize(2);
        assertThat(dto.holdings().get(0).marketValueUsd()).isEqualByComparingTo("1000.00");
        assertThat(dto.holdings().get(0).costBasisUsd()).isEqualByComparingTo("900.00");
        assertThat(dto.holdings().get(0).unrealizedPlUsd()).isEqualByComparingTo("100.00");
        assertThat(dto.holdings().get(0).unrealizedPlPct()).isEqualByComparingTo("11.11");
        assertThat(dto.holdings().get(1).marketValueUsd()).isEqualByComparingTo("550.00");
        assertThat(dto.holdings().get(1).unrealizedPlUsd()).isEqualByComparingTo("-50.00");

        assertThat(dto.cashBalanceUsd()).isEqualByComparingTo("40000.00");
        assertThat(dto.holdingsMarketValueUsd()).isEqualByComparingTo("1550.00");
        assertThat(dto.totalCostBasisUsd()).isEqualByComparingTo("1500.00");
        assertThat(dto.totalUnrealizedPlUsd()).isEqualByComparingTo("50.00");
        assertThat(dto.totalAccountValueUsd()).isEqualByComparingTo("41550.00");
    }

    @Test
    void emptyPortfolioReturnsCashOnly() {
        when(holdingMapper.findPortfolio(1L)).thenReturn(List.of());

        PortfolioDto dto = portfolioService.forCustomer(customer("40000.00"));

        assertThat(dto.holdings()).isEmpty();
        assertThat(dto.holdingsMarketValueUsd()).isEqualByComparingTo("0.00");
        assertThat(dto.totalAccountValueUsd()).isEqualByComparingTo("40000.00");
    }
}
