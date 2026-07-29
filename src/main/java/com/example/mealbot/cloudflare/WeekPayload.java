package com.example.mealbot.cloudflare;

import java.time.LocalDate;
import java.util.List;

public record WeekPayload(
        String campus,
        LocalDate weekStart,
        LocalDate weekEnd,
        String rangeLabel,
        LocalDate today,
        List<DayPayload> days
) {
    public record DayPayload(
            LocalDate date,
            String label,
            String shortDate,
            MealPayload lunch,
            MealPayload dinner
    ) {}

    public record MealPayload(String special, String label, String color, List<String> items) {}
}
