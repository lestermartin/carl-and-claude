package com.carl.trading.web.dto;

import com.carl.trading.model.Security;

import java.math.BigDecimal;

public record SecurityDto(
        String symbol,
        String companyName,
        String exchangeCode,
        String currencyNative,
        BigDecimal priceUsd) {

    public static SecurityDto from(Security s) {
        return new SecurityDto(s.symbol(), s.companyName(), s.exchangeCode(),
                s.currencyNative(), s.snapshotPriceUsd());
    }
}
