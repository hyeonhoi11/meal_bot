package com.example.mealbot.sheet;

import com.example.mealbot.domain.Meal;
import com.example.mealbot.domain.MealType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MealSheetParserTest {

    private final MealSheetParser parser = new MealSheetParser();

    private static List<List<Object>> fixture() {
        return List.of(
                row("", "7월 20일(월)", "7월 21일(화)", "7월 22일(수)", "7월 23일(목)",
                        "7월 24일(금)", "7월 25일(토)", "7월 26일(일)"),
                row("조    식", "백미밥", "백미밥", "백미밥", "백미밥", "백미밥"),
                row("", "야채참치죽", "누룽지", "야채참치죽", "야채계란죽", "야채계란죽"),
                row("", "스크램블에그", "돼지고기장조림", "두부지짐", "해물완자전", "참치야채볶음"),
                row("중    식", "백미밥", "백미밥", "백미밥", "야채비빔밥", "삼계탕", "백미밥", "백미밥"),
                row("", "게살스프", "김치콩나물국", "소고기미역국", "얼갈이된장국",
                        "새우살애호박전", "열무된장국", "돼지고기김치찌개"),
                row("", "함박스테이크", "제육볶음", "닭감자조림", "소고기약고추장",
                        "콩나물무침", "메밀전병", "조기구이"),
                row("석    식", "백미밥", "백미밥", "백미밥", "백미밥", "백미밥", "백미밥", "백미밥"),
                row("", "닭곰탕", "어묵무국", "아욱된장국", "열무된장국",
                        "호박새우젓국", "우동장국", "얼갈이된장국"),
                row("상기 메뉴 및 원산지는 기후 및 시장성이나 식재료 수급상황에 따라 변경될 수 있습니다."),
                row("원   산   지", "쌀(밥,죽,누룽지): 국내산", "돈삼겹: 외국산", "돼지: 국내산")
        );
    }

    private static List<Object> row(Object... cells) {
        return List.of(cells);
    }

    @Test
    @DisplayName("끼니 라벨이 아래 행으로 이어받아진다")
    void forwardFillsMealType() {
        List<Meal> meals = parser.parse(fixture(), 2026);

        Meal lunch = find(meals, LocalDate.of(2026, 7, 22), MealType.LUNCH);
        assertThat(lunch.getMenu()).isEqualTo("백미밥, 소고기미역국, 닭감자조림");
    }

    @Test
    @DisplayName("주말에는 조식 레코드가 생기지 않는다")
    void skipsWeekendBreakfast() {
        List<Meal> meals = parser.parse(fixture(), 2026);

        assertThat(meals)
                .noneMatch(m -> m.getMealDate().equals(LocalDate.of(2026, 7, 25))
                        && m.getMealType() == MealType.BREAKFAST);
    }

    @Test
    @DisplayName("원산지 행이 메뉴로 섞이지 않는다")
    void excludesOriginRows() {
        List<Meal> meals = parser.parse(fixture(), 2026);

        assertThat(meals).noneMatch(m -> m.getMenu().contains("국내산"));
    }

    @Test
    @DisplayName("컬럼 위치가 아니라 헤더 날짜를 따른다")
    void mapsColumnsByHeaderDate() {
        List<Meal> meals = parser.parse(fixture(), 2026);

        Meal saturdayLunch = find(meals, LocalDate.of(2026, 7, 25), MealType.LUNCH);
        assertThat(saturdayLunch.getMenu()).startsWith("백미밥, 열무된장국, 메밀전병");
    }

    private Meal find(List<Meal> meals, LocalDate date, MealType type) {
        return meals.stream()
                .filter(m -> m.getMealDate().equals(date) && m.getMealType() == type)
                .findFirst()
                .orElseThrow(() -> new AssertionError(date + " " + type + " 없음"));
    }
}
