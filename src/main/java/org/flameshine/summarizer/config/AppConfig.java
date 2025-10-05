package org.flameshine.summarizer.config;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.ConfigFactory;

public record AppConfig(
    Telegram telegram,
    OpenAI openAi,
    Analytics analytics
) {

    public static AppConfig load() {
        var config = ConfigFactory.load();
        var mapper = new ObjectMapper();
        return mapper.convertValue(config.root().unwrapped(), AppConfig.class);
    }

    public record Telegram(
        int apiId,
        String apiHash,
        String phoneNumber,
        List<String> groups
    ) {}

    public record OpenAI(
        String apiKey,
        String model,
        double temperature
    ) {}

    public record Analytics(
        int maxMessagesCount,
        int minMessagesPerTopic,
        List<String> filteredWords
    ) {}
}