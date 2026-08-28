package com.carl.trading.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TransactionRecord(
        Long id,
        Long customerId,
        Long securityId,
        String symbol,
        String exchangeCode,
        String side,
        String orderType,
        String status,
        long quantity,
        BigDecimal limitPriceUsd,
        BigDecimal executedPriceUsd,
        BigDecimal cashDeltaUsd,
        String reason,
        OffsetDateTime createdAt) {
}
