package com.carl.trading.web.dto;

import com.carl.trading.model.Exchange;

public record ExchangeDto(String code, String name) {

    public static ExchangeDto from(Exchange e) {
        return new ExchangeDto(e.code(), e.name());
    }
}
