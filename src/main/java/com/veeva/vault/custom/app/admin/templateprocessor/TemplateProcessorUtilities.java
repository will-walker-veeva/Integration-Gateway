package com.veeva.vault.custom.app.admin.templateprocessor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TemplateProcessorUtilities {
    public static String convertInstant(String instant, String format){
        return convertInstant(Instant.parse(instant), format);
    }

    public static String convertInstant(Instant instant, String format){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format).withZone(ZoneId.systemDefault());
        return formatter.format(instant);
    }


    public static String convertLocalDate(String localDate, String format){
        return convertLocalDate(LocalDate.parse(localDate), format);
    }

    public static String convertLocalDate(LocalDate localDate, String format){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format).withZone(ZoneId.systemDefault());
        return formatter.format(localDate);
    }
}
