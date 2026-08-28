package com.carl.trading.web.dto;

import com.carl.trading.model.Customer;

import java.math.BigDecimal;

public record ProfileDto(
        String username,
        String firstName,
        String lastName,
        String taxId,
        String addressLine1,
        String addressLine2,
        String city,
        String state,
        String postalCode,
        BigDecimal cashBalanceUsd) {

    public static ProfileDto from(Customer c) {
        return new ProfileDto(
                c.username(), c.firstName(), c.lastName(), c.taxId(),
                c.addressLine1(), c.addressLine2(), c.city(), c.state(), c.postalCode(),
                c.cashBalanceUsd());
    }
}
