package com.carl.trading.web.dto;

import com.carl.trading.model.OrderType;
import com.carl.trading.model.Side;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record OrderRequest(
        @NotBlank String symbol,
        @NotNull Side side,
        @NotNull OrderType orderType,
        @Positive long quantity,
        /** Required only for LIMIT orders; ignored for MARKET. */
        BigDecimal limitPriceUsd) {
}
