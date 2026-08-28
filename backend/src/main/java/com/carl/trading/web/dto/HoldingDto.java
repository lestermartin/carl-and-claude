package com.carl.trading.web.dto;

import java.math.BigDecimal;

public record HoldingDto(
        String symbol,
        String exchangeCode,
        String companyName,
        long quantity,
        BigDecimal avgCostBasisUsd,
        BigDecimal priceUsd,
        BigDecimal marketValueUsd,
        BigDecimal costBasisUsd,
        BigDecimal unrealizedPlUsd,
        BigDecimal unrealizedPlPct,
        boolean exchangeOpen) {
}
