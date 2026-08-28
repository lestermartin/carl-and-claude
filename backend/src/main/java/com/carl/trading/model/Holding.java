package com.carl.trading.model;

import java.math.BigDecimal;

public record Holding(
        Long id,
        Long customerId,
        Long securityId,
        long quantity,
        BigDecimal avgCostBasisUsd) {
}
