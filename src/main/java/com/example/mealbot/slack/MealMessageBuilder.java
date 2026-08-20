package com.example.mealbot.slack;

import com.example.mealbot.domain.Meal;
import com.example.mealbot.domain.MealCalendar;
import com.example.mealbot.domain.MealMenuFormatter;
import com.example.mealbot.domain.MealMenuFormatter.ParsedMenu;
import com.slack.api.model.block.LayoutBlock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.slack.api.model.block.Blocks.context;
import static com.slack.api.model.block.Blocks.divider;
import static com.slack.api.model.block.Blocks.header;
import static com.slack.api.model.block.Blocks.section;
import static com.slack.api.model.block.composition.BlockCompositions.markdownText;
import static com.slack.api.model.block.composition.BlockCompositions.plainText;

@Component
public class MealMessageBuilder {

    private static final String ITEM_SEP = "  /  ";
    private static final String SET_SEP = " &amp; ";

    @Value("${meal.site-name}")
    private String siteName;

    @Value("${meal.site-url:}")
    private String siteUrl;

    public String fallbackText(LocalDate date) {
        return "오늘 " + date.format(MealCalendar.TITLE) + " 식단";
    }

    public List<LayoutBlock> dailyBlocks(LocalDate date, List<Meal> meals) {
        List<LayoutBlock> blocks = new ArrayList<>();

        blocks.add(header(h -> h.text(plainText(
                "오늘 " + date.format(MealCalendar.TITLE) + " 식단 🍚"))));
        blocks.add(context(c -> c.elements(List.of(markdownText(siteName)))));

        for (Meal meal : meals) {
            blocks.add(divider());

            String menuBlock = format(meal.getMenu()).replace("\n", "\n> ");

            String body = "%s  *%s*\n> %s".formatted(
                    meal.getMealType().emoji(date),
                    meal.getMealType().displayLabel(date),
                    menuBlock);
            blocks.add(section(s -> s.text(markdownText(body))));
        }

        if (siteUrl != null && !siteUrl.isBlank()) {
            blocks.add(divider());
            blocks.add(section(s -> s.text(markdownText(
                    "🗓️ <%s|식단표 보러가기>".formatted(siteUrl)))));
        }

        return blocks;
    }

    private String format(String menu) {
        ParsedMenu parsed = MealMenuFormatter.parse(menu, SET_SEP);

        String line = String.join(ITEM_SEP, parsed.items());
        if (parsed.specialLabel() == null) return line;

        String specialLine = "⭐️ " + parsed.specialLabel() + " ⭐️";
        if (line.isEmpty()) return specialLine;
        return specialLine + "\n" + line;
    }
}
