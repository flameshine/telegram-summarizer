package org.flameshine.summarizer;

import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;

import org.flameshine.summarizer.config.AppConfig;

import static picocli.CommandLine.Command;
import static picocli.CommandLine.Option;

@Command(
    name = "analyze",
    mixinStandardHelpOptions = true,
    description = "Analyze specified Telegram groups and print a short summary."
)
public class AnalyzeCommand implements Runnable {

    @Option(names = {"-c", "--groups"}, required = true, description = "Comma-separated list of Telegram group usernames or IDs.")
    private String groups;

    @Option(names = {"-s", "--since"}, description = "Time window back from now (e.g., 24h, 7d). Defaults to 24h.")
    private String since = "24h";

    @Override
    public void run() {

        var config = AppConfig.load();
        var telegramConfig = config.telegram();
        var openAiConfig = config.openAi();
        var analyticsConfig = config.analytics();

        var groupList = Arrays.asList(groups.split(","));
        var sinceTime = parseSince(since);
        var untilTime = Instant.now();
    }

    // TODO: check timezone

    private static Instant parseSince(String since) {
        var unit = since.charAt(since.length() - 1);
        var number = Long.parseLong(since.substring(0, since.length() - 1));
        var multiplier = getMultiplierForUnit(unit);
        return Instant.now(Clock.systemUTC()).minusSeconds(number * multiplier);
    }

    // TODO: use enum

    private static int getMultiplierForUnit(Character unit) {
        return switch (unit) {
            case 'm' -> 60;
            case 'h' -> 3600;
            case 'd' -> 86400;
            default -> throw new IllegalArgumentException("Invalid time unit. Use 'h' for hours or 'd' for days.");
        };
    }
}