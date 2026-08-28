package com.carl.trading.model;

import java.math.BigDecimal;

public record Customer(
        Long id,
        String username,
        String passwordHash,
        String firstName,
        String lastName,
        String taxId,
        String addressLine1,
        String addressLine2,
        String city,
        String state,
        String postalCode,
        BigDecimal cashBalanceUsd) {
}
