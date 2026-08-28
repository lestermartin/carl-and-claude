package com.carl.trading.web.dto;

import com.carl.trading.model.Exchange;

public record ExchangeDto(
        String code,
        String name,
        String timeZone,
        String openLocal,
        String closeLocal,
        String openDays,
        boolean open) {

    public static ExchangeDto from(Exchange e, boolean open) {
        return new ExchangeDto(
                e.code(),
                e.name(),
                e.timeZone(),
                e.openLocal() == null ? null : e.openLocal().toString(),
                e.closeLocal() == null ? null : e.closeLocal().toString(),
                e.openDays(),
                open);
    }
}
