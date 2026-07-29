package com.example.mealbot.slack;

import com.example.mealbot.domain.Meal;
import com.example.mealbot.domain.MealType;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.SectionBlock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MealMessageBuilderTest {

    private final MealMessageBuilder builder = new MealMessageBuilder();
    private final List<Meal> meals = List.of(new Meal(LocalDate.of(2026, 7, 29), MealType.LUNCH, "백미밥, 된장찌개"));

    @Test
    @DisplayName("site-url 이 비어있으면 이번 주 식단표 링크를 생략한다")
    void omitsLinkWhenSiteUrlBlank() {
        ReflectionTestUtils.setField(builder, "siteName", "광주 캠퍼스");
        ReflectionTestUtils.setField(builder, "siteUrl", "");

        List<LayoutBlock> blocks = builder.dailyBlocks(LocalDate.of(2026, 7, 29), meals);

        assertThat(sectionTexts(blocks)).noneMatch(text -> text.contains("이번 주 식단표"));
    }

    @Test
    @DisplayName("site-url 이 설정되어 있으면 이번 주 식단표 하이퍼링크를 붙인다")
    void addsLinkWhenSiteUrlSet() {
        ReflectionTestUtils.setField(builder, "siteName", "광주 캠퍼스");
        ReflectionTestUtils.setField(builder, "siteUrl", "https://gwangju-meal.example.workers.dev");

        List<LayoutBlock> blocks = builder.dailyBlocks(LocalDate.of(2026, 7, 29), meals);

        assertThat(sectionTexts(blocks))
                .anyMatch(text -> text.contains("<https://gwangju-meal.example.workers.dev|이번 주 식단표>"));
    }

    private List<String> sectionTexts(List<LayoutBlock> blocks) {
        return blocks.stream()
                .filter(b -> b instanceof SectionBlock)
                .map(b -> ((SectionBlock) b).getText().getText())
                .toList();
    }
}
