package com.example.mealbot.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MealMenuFormatter {

    private static final String HEART = "[\u2665\u2661\u2764\u2765\u2766\u2767\u2763]\uFE0F?";

    // 업체 표기가 2026-09 부터 하트(♥초복♥)에서 별표(*양식특식*)로 바뀌었다.
    // 별표는 양끝이 모두 * 일 때만 특일로 본다 (핫도그*케찹 같은 세트 항목과 구분).
    private static final List<Pattern> SPECIAL_DAY = List.of(
            Pattern.compile("^" + HEART + "\\s*(.+?)\\s*" + HEART + "$"),
            Pattern.compile("^\\*\\s*([^*]+?)\\s*\\*$"));

    private MealMenuFormatter() {}

    public record ParsedMenu(String specialLabel, List<String> items) {}

    public static ParsedMenu parse(String menu, String setSeparator) {
        String special = null;
        List<String> items = new ArrayList<>();

        for (String raw : menu.split(",\\s*")) {
            String item = raw.trim();
            if (item.isEmpty()) continue;

            String label = specialLabel(item);
            if (label != null) {
                special = label;
            } else {
                items.add(item.replaceAll("\\s*[*/]\\s*", setSeparator));
            }
        }

        return new ParsedMenu(special, items);
    }

    private static String specialLabel(String item) {
        for (Pattern pattern : SPECIAL_DAY) {
            Matcher m = pattern.matcher(item);
            if (m.matches()) return m.group(1).trim();
        }
        return null;
    }
}
