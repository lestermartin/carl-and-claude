package com.carl.trading.service;

import com.carl.trading.model.Exchange;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.EnumSet;
import java.util.Set;

/**
 * Decides whether an exchange's regular trading session is currently in progress. Single
 * continuous session per day; holidays, half-days, and intraday breaks are not modelled.
 * If an exchange has incomplete hours data it is treated as always open.
 */
@Component
public class ExchangeCalendar {

    private final Clock clock;

    public ExchangeCalendar(Clock clock) {
        this.clock = clock;
    }

    public boolean isOpenNow(Exchange exchange) {
        return isOpen(exchange, clock.instant());
    }

    public boolean isOpen(Exchange exchange, Instant at) {
        if (exchange == null || exchange.timeZone() == null
                || exchange.openLocal() == null || exchange.closeLocal() == null
                || exchange.openDays() == null || exchange.openDays().isBlank()) {
            return true;
        }
        ZonedDateTime local = at.atZone(ZoneId.of(exchange.timeZone()));
        if (!openDays(exchange.openDays()).contains(local.getDayOfWeek())) {
            return false;
        }
        LocalTime now = local.toLocalTime();
        return !now.isBefore(exchange.openLocal()) && now.isBefore(exchange.closeLocal());
    }

    private static Set<DayOfWeek> openDays(String csv) {
        EnumSet<DayOfWeek> days = EnumSet.noneOf(DayOfWeek.class);
        for (String token : csv.split(",")) {
            switch (token.trim().toUpperCase()) {
                case "MON" -> days.add(DayOfWeek.MONDAY);
                case "TUE" -> days.add(DayOfWeek.TUESDAY);
                case "WED" -> days.add(DayOfWeek.WEDNESDAY);
                case "THU" -> days.add(DayOfWeek.THURSDAY);
                case "FRI" -> days.add(DayOfWeek.FRIDAY);
                case "SAT" -> days.add(DayOfWeek.SATURDAY);
                case "SUN" -> days.add(DayOfWeek.SUNDAY);
                default -> { /* ignore unknown tokens */ }
            }
        }
        return days;
    }
}
