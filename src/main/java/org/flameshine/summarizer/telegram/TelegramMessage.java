package org.flameshine.summarizer.telegram;

import java.time.Instant;

public record TelegramMessage(
    long id,
    String author,
    String text,
    Instant timestamp
) {}