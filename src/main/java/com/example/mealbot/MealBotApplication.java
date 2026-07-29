package com.example.mealbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MealBotApplication {
    public static void main(String[] args) {
        SpringApplication.run(MealBotApplication.class, args);
    }
}
