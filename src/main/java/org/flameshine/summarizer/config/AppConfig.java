package org.flameshine.summarizer.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.typesafe.config.ConfigFactory;

import org.flameshine.summarizer.util.ObjectMapperFactory;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AppConfig(
    Telegram telegram,
    OpenAI openAi,
    Analytics analytics
) {

    public record Telegram(
        int apiId,
        String apiHash,
        String phoneNumber,
        String sessionDir
    ) {}

    public record OpenAI(
        String apiKey,
        String model,
        double temperature
    ) {}

    public record Analytics(
        int maxMessagesCount,
        int minMessagesPerTopic
    ) {}

    public static AppConfig load() {
        var config = ConfigFactory.load();
        var mapper = ObjectMapperFactory.get();
        return mapper.convertValue(config.root().unwrapped(), AppConfig.class);
    }
}