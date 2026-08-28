package com.carl.trading.model;

import java.time.LocalTime;

public record Exchange(
        Long id,
        String code,
        String name,
        boolean enabled,
        String timeZone,
        LocalTime openLocal,
        LocalTime closeLocal,
        String openDays) {
}
