package com.example.mealbot.service;

import com.example.mealbot.domain.Meal;
import com.example.mealbot.domain.MealType;
import com.example.mealbot.domain.PostLog;
import com.example.mealbot.repository.PostLogRepository;
import com.example.mealbot.slack.MealMessageBuilder;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MealBroadcastService {

    public static final String ZONE_ID = "Asia/Seoul";
    public static final ZoneId KST = ZoneId.of(ZONE_ID);

    private final MealQueryService queryService;
    private final PostLogRepository postLogRepository;
    private final MealMessageBuilder messageBuilder;
    private final MethodsClient slack;

    @Value("${slack.channel-id}")
    private String channelId;

    @Scheduled(cron = "${meal.broadcast-cron}", zone = ZONE_ID)
    public void scheduledBroadcast() {
        broadcast(LocalDate.now(KST));
    }

    @Transactional
    public boolean broadcast(LocalDate date) {

        if (postLogRepository.existsByPostDate(date)) {
            log.info("{} 는 이미 게시했습니다. 건너뜁니다.", date);
            return false;
        }

        List<Meal> meals = queryService.findByDate(date).stream()
                .filter(m -> MealType.DISPLAYED.contains(m.getMealType()))
                .toList();

        if (meals.isEmpty()) {
            log.warn("{} 식단 데이터가 없어 게시하지 않습니다.", date);
            warnIfMonthBoundary(date);
            return false;
        }

        try {
            ChatPostMessageResponse res = slack.chatPostMessage(r -> r
                    .channel(channelId)
                    .text(messageBuilder.fallbackText(date))
                    .blocks(messageBuilder.dailyBlocks(date, meals)));

            if (!res.isOk()) {
                log.error("슬랙 게시 실패: {}", res.getError());
                return false;
            }

            postLogRepository.save(new PostLog(date, res.getTs()));
            log.info("{} 게시 완료 (ts={})", date, res.getTs());
            return true;

        } catch (Exception e) {
            log.error("슬랙 호출 중 예외: {}", date, e);
            return false;
        }
    }

    private void warnIfMonthBoundary(LocalDate date) {
        if (date.getDayOfMonth() <= 3) {
            log.error("{} 다음 달 식단표가 시트에 반영되지 않았을 수 있습니다. 확인이 필요합니다.", date);
        }
    }
}
