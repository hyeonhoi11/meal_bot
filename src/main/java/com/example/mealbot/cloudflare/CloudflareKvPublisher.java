package com.example.mealbot.cloudflare;

import com.example.mealbot.cloudflare.WeekPayload.DayPayload;
import com.example.mealbot.cloudflare.WeekPayload.MealPayload;
import com.example.mealbot.domain.Meal;
import com.example.mealbot.domain.MealCalendar;
import com.example.mealbot.domain.MealMenuFormatter;
import com.example.mealbot.domain.MealMenuFormatter.ParsedMenu;
import com.example.mealbot.domain.MealType;
import com.example.mealbot.service.MealBroadcastService;
import com.example.mealbot.service.MealQueryService;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class CloudflareKvPublisher {

    private static final String SET_SEP = " & ";
    private static final String KV_KEY = "week";

    private final MealQueryService queryService;
    private final ObjectMapper objectMapper;
    private final HttpClient http = HttpClient.newHttpClient();

    @Value("${meal.site-name}")
    private String siteName;

    @Value("${cloudflare.enabled:false}")
    private boolean enabled;

    @Value("${cloudflare.account-id:}")
    private String accountId;

    @Value("${cloudflare.namespace-id:}")
    private String namespaceId;

    @Value("${cloudflare.api-token:}")
    private String apiToken;

    public WeekPayload buildPayload(LocalDate anchor) {
        LocalDate monday = MealCalendar.mondayOf(anchor);
        LocalDate sunday = monday.plusDays(6);
        Map<LocalDate, List<Meal>> week = queryService.findWeekOf(anchor);

        List<DayPayload> days = week.entrySet().stream()
                .map(e -> toDayPayload(e.getKey(), e.getValue()))
                .toList();

        LocalDate lastDataDate = days.stream()
                .filter(d -> d.lunch() != null || d.dinner() != null)
                .map(DayPayload::date)
                .max(Comparator.naturalOrder())
                .orElse(sunday);

        String rangeLabel = "%s — %s".formatted(
                MealCalendar.MONTH_DAY.format(monday), MealCalendar.MONTH_DAY.format(lastDataDate));

        return new WeekPayload(
                siteName, monday, sunday, rangeLabel, LocalDate.now(MealBroadcastService.KST), days);
    }

    public void publishIfEnabled(LocalDate anchor) {
        if (!enabled) return;
        try {
            publish(buildPayload(anchor));
        } catch (Exception e) {
            log.error("Cloudflare KV 발행 중 예외", e);
        }
    }

    private void publish(WeekPayload payload) throws Exception {
        String json = objectMapper.writeValueAsString(payload);
        String url = "https://api.cloudflare.com/client/v4/accounts/%s/storage/kv/namespaces/%s/values/%s"
                .formatted(accountId, namespaceId, KV_KEY);

        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("Authorization", "Bearer " + apiToken)
                .header("Content-Type", "application/json")
                .method("PUT", HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) {
            log.error("Cloudflare KV 응답 오류: {} {}", response.statusCode(), response.body());
            return;
        }
        log.info("Cloudflare KV 발행 완료: {} ~ {}", payload.weekStart(), payload.weekEnd());
    }

    private DayPayload toDayPayload(LocalDate date, List<Meal> meals) {
        return new DayPayload(
                date,
                MealCalendar.WEEKDAY.format(date),
                MealCalendar.MONTH_DAY.format(date),
                toMealPayload(date, meals, MealType.LUNCH),
                toMealPayload(date, meals, MealType.DINNER));
    }

    private MealPayload toMealPayload(LocalDate date, List<Meal> meals, MealType type) {
        return meals.stream()
                .filter(m -> m.getMealType() == type)
                .findFirst()
                .map(m -> {
                    ParsedMenu parsed = MealMenuFormatter.parse(m.getMenu(), SET_SEP);
                    return new MealPayload(
                            parsed.specialLabel(),
                            type.displayLabel(date),
                            type.color(),
                            parsed.items());
                })
                .orElse(null);
    }
}
