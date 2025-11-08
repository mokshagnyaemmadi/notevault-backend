package com.example.notevault.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

// This DTO will hold the data for our charts (Date and Count)
public class DailyCountDTO {

    private LocalDate date;
    private Long count;

    public DailyCountDTO(Object[] result) {
        // Convert the timestamp/date from the query result
        if (result[0] instanceof Instant) {
            this.date = ((Instant) result[0]).atZone(ZoneOffset.UTC).toLocalDate();
        } else if (result[0] instanceof java.sql.Date) {
            this.date = ((java.sql.Date) result[0]).toLocalDate();
        } else if (result[0] instanceof java.sql.Timestamp) {
             this.date = ((java.sql.Timestamp) result[0]).toLocalDateTime().toLocalDate();
        }
        // Convert the count (which is usually a Long)
        this.count = (Long) result[1];
    }

    // Getters
    public LocalDate getDate() { return date; }
    public Long getCount() { return count; }
}