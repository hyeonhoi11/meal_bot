package com.example.mealbot.sheet;

import com.example.mealbot.domain.Meal;
import com.example.mealbot.domain.MealType;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MealSheetParser {

    private static final Pattern DATE_CELL = Pattern.compile("^(\\d{1,2})월\\s*(\\d{1,2})일");
    private static final Pattern BLOCK_END =
            Pattern.compile("상기\\s*메뉴|원\\s*산\\s*지|주\\s*간\\s*식\\s*단\\s*표");
    
    private static final Pattern EXCLUDED_ITEM = Pattern.compile("^[셀샐]프\\s*라면$");

    private static final int FIRST_COL = 1;
    private static final int LAST_COL = 7;

    private record Key(LocalDate date, MealType type) {}

    public List<Meal> parse(List<List<Object>> rows, int year) {
        if (rows == null || rows.isEmpty()) return List.of();

        Map<Integer, LocalDate> dateByCol = new LinkedHashMap<>();
        Map<Key, List<String>> bucket = new LinkedHashMap<>();
        MealType current = null;

        int prevMonth = -1;
        int yearCursor = year;

        for (List<Object> row : rows) {

            if (isDateHeader(row)) {
                dateByCol.clear();
                current = null;
                for (int c = FIRST_COL; c <= LAST_COL; c++) {
                    Matcher m = DATE_CELL.matcher(cell(row, c));
                    if (!m.find()) continue;
                    int month = Integer.parseInt(m.group(1));
                    int day = Integer.parseInt(m.group(2));
                    if (prevMonth == 12 && month == 1) yearCursor++;  
                    prevMonth = month;
                    dateByCol.put(c, LocalDate.of(yearCursor, month, day));
                }
                continue;
            }

            String head = cell(row, 0);

            if (BLOCK_END.matcher(head).find()) {
                dateByCol.clear();
                current = null;
                continue;
            }

            Optional<MealType> label = MealType.fromSheet(head);
            if (label.isPresent()) current = label.get();

            if (current == null || dateByCol.isEmpty()) continue;

            for (Map.Entry<Integer, LocalDate> e : dateByCol.entrySet()) {
                String item = cell(row, e.getKey());
                if (item.isBlank() || EXCLUDED_ITEM.matcher(item).matches()) continue;
                bucket.computeIfAbsent(new Key(e.getValue(), current), k -> new ArrayList<>())
                        .add(item);
            }
        }

        return bucket.entrySet().stream()
                .map(e -> new Meal(e.getKey().date(), e.getKey().type(),
                        String.join(", ", e.getValue())))
                .toList();
    }

    private boolean isDateHeader(List<Object> row) {
        for (int c = FIRST_COL; c <= LAST_COL; c++) {
            if (DATE_CELL.matcher(cell(row, c)).find()) return true;
        }
        return false;
    }

 
    private String cell(List<Object> row, int idx) {
        if (row == null || idx >= row.size() || row.get(idx) == null) return "";
        return row.get(idx).toString().trim();
    }
}
