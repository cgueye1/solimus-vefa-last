package com.wakana.vefa.util;



import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    public static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public static LocalDate parse(String date) {
        try {
            return LocalDate.parse(date, FORMATTER);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                "Invalid date format, expected dd-MM-yyyy : " + date
            );
        }
    }
}
