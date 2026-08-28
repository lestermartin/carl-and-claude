package com.carl.trading.model;

import java.math.BigDecimal;

public record Security(
        Long id,
        Long exchangeId,
        String exchangeCode,
        String symbol,
        String companyName,
        String currencyNative,
        BigDecimal snapshotPriceUsd) {
}
