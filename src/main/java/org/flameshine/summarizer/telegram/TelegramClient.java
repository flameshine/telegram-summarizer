package org.flameshine.summarizer.telegram;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface TelegramClient extends AutoCloseable {
    Map<Long, List<TelegramMessage>> getMessagesByChat(List<Long> chatIds, Instant since, Instant until);
}