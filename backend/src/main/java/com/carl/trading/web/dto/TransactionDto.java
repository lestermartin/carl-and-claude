package com.carl.trading.web.dto;

import com.carl.trading.model.TransactionRecord;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TransactionDto(
        Long id,
        String side,
        String orderType,
        String status,
        String symbol,
        String exchangeCode,
        long quantity,
        BigDecimal limitPriceUsd,
        BigDecimal executedPriceUsd,
        BigDecimal cashDeltaUsd,
        String reason,
        OffsetDateTime createdAt) {

    public static TransactionDto from(TransactionRecord t) {
        return new TransactionDto(
                t.id(), t.side(), t.orderType(), t.status(), t.symbol(), t.exchangeCode(),
                t.quantity(), t.limitPriceUsd(), t.executedPriceUsd(), t.cashDeltaUsd(),
                t.reason(), t.createdAt());
    }
}
