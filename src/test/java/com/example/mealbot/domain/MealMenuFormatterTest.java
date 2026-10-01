package com.example.mealbot.domain;

import com.example.mealbot.domain.MealMenuFormatter.ParsedMenu;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MealMenuFormatterTest {

    @Test
    @DisplayName("특일 마커를 추출하고 세트 구분자를 통일한다")
    void parsesSpecialDayAndSetItems() {
        ParsedMenu parsed = MealMenuFormatter.parse("♥초복♥, 삼계탕, 핫도그*케찹", " & ");

        assertThat(parsed.specialLabel()).isEqualTo("초복");
        assertThat(parsed.items()).containsExactly("삼계탕", "핫도그 & 케찹");
    }

    @Test
    @DisplayName("별표로 표기된 특일 마커도 추출하고, 세트 항목의 별표와 구분한다")
    void parsesStarSpecialDay() {
        ParsedMenu parsed = MealMenuFormatter.parse("*불고기특식데이*, 맑은순두부국, 핫도그*케찹", " & ");

        assertThat(parsed.specialLabel()).isEqualTo("불고기특식데이");
        assertThat(parsed.items()).containsExactly("맑은순두부국", "핫도그 & 케찹");
    }

    @Test
    @DisplayName("특일 마커가 없으면 null 을 반환한다")
    void returnsNullWhenNoSpecialDay() {
        ParsedMenu parsed = MealMenuFormatter.parse("백미밥, 된장찌개", " & ");

        assertThat(parsed.specialLabel()).isNull();
        assertThat(parsed.items()).containsExactly("백미밥", "된장찌개");
    }
}
