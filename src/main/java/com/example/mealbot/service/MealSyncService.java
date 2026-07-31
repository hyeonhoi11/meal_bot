package com.example.mealbot.service;

import com.example.mealbot.cloudflare.CloudflareKvPublisher;
import com.example.mealbot.domain.Meal;
import com.example.mealbot.drive.DriveMealImportService;
import com.example.mealbot.repository.MealRepository;
import com.example.mealbot.sheet.MealSheetParser;
import com.example.mealbot.sheet.SheetReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MealSyncService {

    private final SheetReader sheetReader;
    private final MealSheetParser parser;
    private final MealRepository mealRepository;
    private final CloudflareKvPublisher kvPublisher;
    private final DriveMealImportService driveImportService;

    @Value("${google.year}")
    private int year;

    @Scheduled(cron = "${meal.sync-cron}", zone = MealBroadcastService.ZONE_ID)
    public void scheduledSync() {
        sync();
    }

    @Transactional
    public int sync() {
        try {
            driveImportService.importNewFilesIfAny();
        } catch (Exception e) {
            log.error("Drive 신규 식단표 반영 실패. 기존 시트로 계속 진행합니다.", e);
        }

        List<Meal> parsed;
        try {
            parsed = parser.parse(sheetReader.readRows(), year);
        } catch (Exception e) {
            log.error("시트 읽기 실패. 기존 데이터를 유지합니다.", e);
            return 0;
        }

        if (parsed.isEmpty()) {
            log.warn("시트에서 읽은 행이 없습니다. 동기화를 건너뜁니다.");
            return 0;
        }

        mealRepository.deleteAllInBatch();
        mealRepository.saveAll(parsed);
        log.info("동기화 완료: {}건", parsed.size());

        kvPublisher.publishIfEnabled(LocalDate.now(MealBroadcastService.KST));

        return parsed.size();
    }
}
