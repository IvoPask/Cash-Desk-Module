package com.bank.cashdesk.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtils {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public static DateTimeFormatter getDateTimeFormatter() {
        return DATE_TIME_FORMATTER;
    }

    public static LocalDateTime parseDate(String dateString, String paramName) {
        if (dateString == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateString, DateTimeUtils.DATE_TIME_FORMATTER);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    String.format("Invalid date format for '%s'. Expected format is yyyy-MM-dd'T'HH:mm:ss", paramName)
            );
        }
    }
}
