package com.example.mealbot.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class MealCalendar {

    public static final DateTimeFormatter TITLE =
            DateTimeFormatter.ofPattern("M월 d일 (E)", Locale.KOREAN);
    public static final DateTimeFormatter WEEKDAY =
            DateTimeFormatter.ofPattern("E", Locale.KOREAN);
    public static final DateTimeFormatter MONTH_DAY =
            DateTimeFormatter.ofPattern("M.d", Locale.KOREAN);

    private MealCalendar() {}

    public static boolean isWeekend(LocalDate date) {
        DayOfWeek d = date.getDayOfWeek();
        return d == DayOfWeek.SATURDAY || d == DayOfWeek.SUNDAY;
    }

    public static LocalDate mondayOf(LocalDate date) {
        return date.minusDays(date.getDayOfWeek().getValue() - 1L);
    }
}
