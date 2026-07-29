package com.example.mealbot.service;

import com.example.mealbot.domain.Meal;
import com.example.mealbot.domain.MealCalendar;
import com.example.mealbot.repository.MealRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MealQueryService {

    private final MealRepository mealRepository;

    public List<Meal> findByDate(LocalDate date) {
        return mealRepository.findByMealDate(date).stream()
                .sorted(Comparator.comparingInt(m -> m.getMealType().ordinal()))
                .toList();
    }

    public Map<LocalDate, List<Meal>> findWeekOf(LocalDate anchor) {
        LocalDate monday = MealCalendar.mondayOf(anchor);
        LocalDate sunday = monday.plusDays(6);

        return mealRepository
                .findByMealDateBetween(monday, sunday)
                .stream()
                .sorted(Comparator.comparing(Meal::getMealDate)
                        .thenComparingInt(m -> m.getMealType().ordinal()))
                .collect(Collectors.groupingBy(
                        Meal::getMealDate, LinkedHashMap::new, Collectors.toList()));
    }
}
