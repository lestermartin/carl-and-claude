package com.carl.trading.web.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record OrderResultDto(
        String status,
        String side,
        String orderType,
        String symbol,
        String exchangeCode,
        long quantity,
        BigDecimal limitPriceUsd,
        BigDecimal executedPriceUsd,
        BigDecimal cashDeltaUsd,
        BigDecimal newCashBalanceUsd,
        String reason,
        OffsetDateTime createdAt) {
}
