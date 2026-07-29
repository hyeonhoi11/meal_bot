package com.example.mealbot.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "post_log")
public class PostLog {

    @Id
    @Column(name = "post_date")
    private LocalDate postDate;

    @Column(name = "slack_ts", length = 40)
    private String slackTs;

    @Column(name = "posted_at", nullable = false)
    private Instant postedAt;

    public PostLog(LocalDate postDate, String slackTs) {
        this.postDate = postDate;
        this.slackTs = slackTs;
        this.postedAt = Instant.now();
    }
}
