package com.example.mealbot.config;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SlackClientConfig {

    @Bean
    public MethodsClient slackMethodsClient(@Value("${slack.bot-token}") String botToken) {
        return Slack.getInstance().methods(botToken);
    }
}
