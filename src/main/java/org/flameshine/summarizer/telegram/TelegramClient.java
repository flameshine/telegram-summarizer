package org.flameshine.summarizer.telegram;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface TelegramClient extends AutoCloseable {
    Map<String, List<TelegramMessage>> getMessagesByGroup(List<String> groups, Instant since, Instant until);
}