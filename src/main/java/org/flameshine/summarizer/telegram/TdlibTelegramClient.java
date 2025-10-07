package org.flameshine.summarizer.telegram;

import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.google.common.base.Strings;
import it.tdlight.Init;
import it.tdlight.Log;
import it.tdlight.Slf4JLogMessageHandler;
import it.tdlight.client.APIToken;
import it.tdlight.client.AuthenticationSupplier;
import it.tdlight.client.SimpleTelegramClient;
import it.tdlight.client.SimpleTelegramClientFactory;
import it.tdlight.client.TDLibSettings;
import it.tdlight.jni.TdApi;
import it.tdlight.util.UnsupportedNativeLibraryException;
import lombok.extern.slf4j.Slf4j;

import org.flameshine.summarizer.config.AppConfig;

/**
 * Implementation of {@link TelegramClient} based on <a href="https://github.com/tdlib/td">TDLib</a>.
 */
@Slf4j
public class TdlibTelegramClient implements TelegramClient {

    private static final int BATCH_SIZE = 100;

    private final SimpleTelegramClient client;
    private final AppConfig.Analytics config;

    public TdlibTelegramClient(AppConfig.Telegram telegramConfig, AppConfig.Analytics analyticsConfig) {

        try {
            Init.init();
        } catch (UnsupportedNativeLibraryException e) {
            throw new ExceptionInInitializerError("Unable to initialize Telegram client" + e.getMessage());
        }

        // TODO: change to 2

        Log.setLogMessageHandler(3, new Slf4JLogMessageHandler());

        var apiToken = new APIToken(telegramConfig.apiId(), telegramConfig.apiHash());
        var settings = TDLibSettings.create(apiToken);
        var sessionPath = Paths.get(telegramConfig.sessionDir());

        settings.setDatabaseDirectoryPath(sessionPath.resolve("data"));
        settings.setDownloadedFilesDirectoryPath(sessionPath.resolve("downloads"));

        try (var clientFactory = new SimpleTelegramClientFactory()) {
            var clientBuilder = clientFactory.builder(settings);
            var authenticationData = AuthenticationSupplier.user(telegramConfig.phoneNumber());
            this.client = clientBuilder.build(authenticationData);
        }

        this.config = analyticsConfig;
    }

    @Override
    public Map<Long, List<TelegramMessage>> getMessagesByChat(List<Long> chatIds, Instant since, Instant until) {

        Map<Long, List<TelegramMessage>> builder = new LinkedHashMap<>();

        for (var chatId : chatIds) {
            var messages = fetchMessagesForChat(chatId, since, until);
            builder.put(chatId, messages);
        }

        return Collections.unmodifiableMap(builder);
    }

    @Override
    public void close() throws Exception {
        if (client != null) {
            client.close();
        }
    }

    private List<TelegramMessage> fetchMessagesForChat(long chatId, Instant since, Instant until) {

        List<TelegramMessage> messagesBuilder = new ArrayList<>();

        var fromMessageId = 0L;
        var maxBatches = Math.max(1, config.maxMessagesCount() / BATCH_SIZE);

        for (var batchIndex = 0; batchIndex < maxBatches; batchIndex++) {

            log.info("Processing batch {}", batchIndex);

            var batch = fetchBatch(chatId, fromMessageId, since, until);

            if (batch.isEmpty()) {
                break;
            }

            messagesBuilder.addAll(batch);

            if (batch.size() < BATCH_SIZE) {
                break;
            }

            fromMessageId = batch.getLast().id();
        }

        log.info("Fetched {} messages from chat {}", messagesBuilder.size(), chatId);

        return messagesBuilder;
    }

    private List<TelegramMessage> fetchBatch(long chatId, long fromMessageId, Instant since, Instant until) {

        var response = sendSync(new TdApi.GetChatHistory(chatId, fromMessageId, 0, BATCH_SIZE, false));

        if (!(response instanceof TdApi.Messages batch) || batch.messages.length == 0) {
            log.info("No more messages in chat {}", chatId);
            return List.of();
        }

        List<TelegramMessage> messagesBuilder = new ArrayList<>();

        for (var message : batch.messages) {

            var timestamp = Instant.ofEpochSecond(message.date);

            if (timestamp.isAfter(until)) {
                continue;
            }

            if (timestamp.isBefore(since)) {
                break;
            }

            if (message.content instanceof TdApi.MessageText text && text.text != null) {
                var content = text.text.text;
                if (Strings.isNullOrEmpty(content)) {
                    messagesBuilder.add(new TelegramMessage(message.id, message.importInfo.senderName, content, timestamp));
                }
            }
        }

        return Collections.unmodifiableList(messagesBuilder);
    }

    private TdApi.Object sendSync(TdApi.Function<TdApi.Messages> function) {

        CompletableFuture<TdApi.Object> future = new CompletableFuture<>();

        client.send(function);

        var result = future.join();

        if (result instanceof TdApi.Error error) {
            throw new RuntimeException("TDLib error: " + error.message);
        }

        return result;
    }
}