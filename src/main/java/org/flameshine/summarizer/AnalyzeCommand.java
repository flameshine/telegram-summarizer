package org.flameshine.summarizer;

import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;

import org.flameshine.summarizer.config.AppConfig;
import org.flameshine.summarizer.telegram.TdlibTelegramClient;
import org.flameshine.summarizer.telegram.TelegramClient;

import static picocli.CommandLine.Command;
import static picocli.CommandLine.Option;

@Command(
    name = "analyze",
    mixinStandardHelpOptions = true,
    description = "Analyze specified Telegram groups and print a short summary."
)
public class AnalyzeCommand implements Runnable {

    @Option(names = {"-c", "--chats"}, required = true, description = "Comma-separated list of Telegram chat IDs.")
    private String chats;

    @Option(names = {"-s", "--since"}, description = "Time window back from now (e.g., 24h, 7d). Defaults to 24h.")
    private String since = "24h";

    @Override
    public void run() {

        var config = AppConfig.load();

        var telegramConfig = config.telegram();
        var openAiConfig = config.openAi();
        var analyticsConfig = config.analytics();

        var chatIds = Arrays.stream(chats.split(","))
            .mapToLong(Long::parseLong)
            .boxed()
            .toList();

        var sinceTime = parseSince(since);
        var untilTime = Instant.now();

        try (TelegramClient telegramClient = new TdlibTelegramClient(telegramConfig, analyticsConfig)) {

            var messagesByChat = telegramClient.getMessagesByChat(chatIds, sinceTime, untilTime);

            System.out.println(messagesByChat);

        } catch (Exception e) {
            throw new RuntimeException("An unexpected error has occurred", e);
        }
    }

    // TODO: check timezone

    private static Instant parseSince(String since) {
        var unit = since.charAt(since.length() - 1);
        var number = Long.parseLong(since.substring(0, since.length() - 1));
        var multiplier = getMultiplierForUnit(unit);
        return Instant.now(Clock.systemUTC()).minusSeconds(number * multiplier);
    }

    // TODO: use enum instead

    private static int getMultiplierForUnit(Character unit) {
        return switch (unit) {
            case 'm' -> 60;
            case 'h' -> 3600;
            case 'd' -> 86400;
            default -> throw new IllegalArgumentException("Invalid time unit");
        };
    }
}