package org.flameshine.summarizer.telegram;

import java.time.Instant;

public record TelegramMessage(
    String group,
    Instant timestamp,
    String text
) {}