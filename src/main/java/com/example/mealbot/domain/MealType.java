package com.example.mealbot.domain;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

public enum MealType {

    BREAKFAST("조식"),
    LUNCH("중식"),
    DINNER("석식");

    public static final Set<MealType> DISPLAYED = EnumSet.of(LUNCH, DINNER);

    private final String sheetLabel;

    MealType(String sheetLabel) {
        this.sheetLabel = sheetLabel;
    }

    public static Optional<MealType> fromSheet(String raw) {
        if (raw == null) return Optional.empty();
        String normalized = raw.replaceAll("\\s+", "");
        return Arrays.stream(values())
                .filter(t -> t.sheetLabel.equals(normalized))
                .findFirst();
    }

    public String displayLabel(LocalDate date) {
        boolean weekend = MealCalendar.isWeekend(date);
        return switch (this) {
            case BREAKFAST -> "아침";
            case LUNCH -> weekend ? "아점" : "점심";
            case DINNER -> "저녁";
        };
    }

    public String emoji(LocalDate date) {
        boolean weekend = MealCalendar.isWeekend(date);
        return switch (this) {
            case BREAKFAST -> ":fried_egg:";
            case LUNCH -> weekend ? ":pancakes:" : ":curry:";
            case DINNER -> ":fork_and_knife:";
        };
    }

    public String color() {
        return switch (this) {
            case BREAKFAST -> "#F47725";
            case LUNCH -> "#0072C6";
            case DINNER -> "#009A93";
        };
    }
}
