package com.carl.trading.web.dto;

import java.math.BigDecimal;

/** Raw joined holding + security row used by {@code PortfolioService} to compute valuations. */
public record PortfolioRow(
        Long securityId,
        String symbol,
        String exchangeCode,
        String companyName,
        String currencyNative,
        long quantity,
        BigDecimal avgCostBasisUsd,
        BigDecimal snapshotPriceUsd) {
}
