package com.carl.trading.service;

import com.carl.trading.model.Exchange;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class ExchangeCalendarTest {

    private static final Exchange NYSE = new Exchange(1L, "NYSE", "New York Stock Exchange", true,
            "America/New_York", LocalTime.of(9, 30), LocalTime.of(16, 0), "MON,TUE,WED,THU,FRI");
    private static final Exchange LSE = new Exchange(2L, "LSE", "London Stock Exchange", true,
            "Europe/London", LocalTime.of(8, 0), LocalTime.of(16, 30), "MON,TUE,WED,THU,FRI");

    private static ExchangeCalendar at(String instant) {
        return new ExchangeCalendar(Clock.fixed(Instant.parse(instant), ZoneOffset.UTC));
    }

    @Test
    void openDuringRegularHoursOnAWeekday() {
        // Wed 2026-08-26 15:00Z == 11:00 America/New_York
        assertThat(at("2026-08-26T15:00:00Z").isOpenNow(NYSE)).isTrue();
    }

    @Test
    void closedBeforeTheOpeningBell() {
        // Wed 2026-08-26 13:00Z == 09:00 America/New_York (before 09:30)
        assertThat(at("2026-08-26T13:00:00Z").isOpenNow(NYSE)).isFalse();
    }

    @Test
    void closedAfterTheClosingBell() {
        // Wed 2026-08-26 20:30Z == 16:30 America/New_York (after 16:00)
        assertThat(at("2026-08-26T20:30:00Z").isOpenNow(NYSE)).isFalse();
    }

    @Test
    void closedOnTheWeekend() {
        // Saturday 2026-08-29 15:00Z
        assertThat(at("2026-08-29T15:00:00Z").isOpenNow(NYSE)).isFalse();
    }

    @Test
    void timeZoneIsResolvedPerExchange() {
        // Wed 2026-08-26 08:30Z: 09:30 Europe/London (open) but 04:30 New York (closed)
        ExchangeCalendar calendar = at("2026-08-26T08:30:00Z");
        assertThat(calendar.isOpenNow(LSE)).isTrue();
        assertThat(calendar.isOpenNow(NYSE)).isFalse();
    }

    @Test
    void incompleteHoursDataIsTreatedAsAlwaysOpen() {
        Exchange noHours = new Exchange(9L, "X", "Mystery Exchange", true, null, null, null, null);
        assertThat(at("2026-08-29T03:00:00Z").isOpenNow(noHours)).isTrue();
    }
}
