package com.electricity.cms.dto;

import java.time.LocalDateTime;

public record DateRange(LocalDateTime from, LocalDateTime to) {

    public static DateRange currentMonth() {
        LocalDateTime start = LocalDateTime.now().withDayOfMonth(1).toLocalDate().atStartOfDay();
        return new DateRange(start, LocalDateTime.now());
    }
}
