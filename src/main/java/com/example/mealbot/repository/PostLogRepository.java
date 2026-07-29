package com.example.mealbot.repository;

import com.example.mealbot.domain.PostLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface PostLogRepository extends JpaRepository<PostLog, LocalDate> {
    boolean existsByPostDate(LocalDate postDate);
}
