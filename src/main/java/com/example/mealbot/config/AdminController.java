package com.example.mealbot.config;

import com.example.mealbot.cloudflare.CloudflareKvPublisher;
import com.example.mealbot.cloudflare.WeekPayload;
import com.example.mealbot.drive.DriveMealImportService;
import com.example.mealbot.service.MealBroadcastService;
import com.example.mealbot.service.MealSyncService;
import com.example.mealbot.service.MealQueryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final MealSyncService syncService;
    private final MealBroadcastService broadcastService;
    private final MealQueryService queryService;
    private final CloudflareKvPublisher kvPublisher;
    private final DriveMealImportService driveImportService;

    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> sync() {
        int count = syncService.sync();
        return ResponseEntity.ok(Map.of("synced", count));
    }

    @PostMapping("/import-drive")
    public ResponseEntity<Map<String, Object>> importDrive() {
        int imported = driveImportService.importNewFilesIfAny();
        return ResponseEntity.ok(Map.of("imported", imported));
    }

    @PostMapping("/broadcast")
    public ResponseEntity<Map<String, Object>> broadcast(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate target = (date != null) ? date : LocalDate.now(MealBroadcastService.KST);
        boolean posted = broadcastService.broadcast(target);
        return ResponseEntity.ok(Map.of("date", target.toString(), "posted", posted));
    }
    @GetMapping("/meals")
    public ResponseEntity<List<Map<String, String>>> meals(
        @RequestParam
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

            return ResponseEntity.ok(queryService.findByDate(date).stream()
                    .map(m -> Map.of(
                            "type", m.getMealType().name(),
                            "menu", m.getMenu()))
                    .toList());
    }

    @GetMapping("/week")
    public ResponseEntity<WeekPayload> week(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate anchor = (date != null) ? date : LocalDate.now(MealBroadcastService.KST);
        return ResponseEntity.ok(kvPublisher.buildPayload(anchor));
    }
}
