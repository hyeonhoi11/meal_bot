package com.example.mealbot.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MealMenuFormatter {

    private static final Pattern SPECIAL_DAY = Pattern.compile(
            "^[\u2665\u2661\u2764\u2765\u2766\u2767\u2763]\uFE0F?\\s*(.+?)\\s*"
                    + "[\u2665\u2661\u2764\u2765\u2766\u2767\u2763]\uFE0F?$");

    private MealMenuFormatter() {}

    public record ParsedMenu(String specialLabel, List<String> items) {}

    public static ParsedMenu parse(String menu, String setSeparator) {
        String special = null;
        List<String> items = new ArrayList<>();

        for (String raw : menu.split(",\\s*")) {
            String item = raw.trim();
            if (item.isEmpty()) continue;

            Matcher m = SPECIAL_DAY.matcher(item);
            if (m.matches()) {
                special = m.group(1).trim();
            } else {
                items.add(item.replaceAll("\\s*[*/]\\s*", setSeparator));
            }
        }

        return new ParsedMenu(special, items);
    }
}
