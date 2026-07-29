package com.example.mealbot.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "meal",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_meal_date_type",
                columnNames = {"meal_date", "meal_type"})
)
public class Meal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "meal_date", nullable = false)
    private LocalDate mealDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false, length = 20)
    private MealType mealType;

    @Column(name = "menu", nullable = false, length = 1000)
    private String menu;

    public Meal(LocalDate mealDate, MealType mealType, String menu) {
        this.mealDate = mealDate;
        this.mealType = mealType;
        this.menu = menu;
    }
}
