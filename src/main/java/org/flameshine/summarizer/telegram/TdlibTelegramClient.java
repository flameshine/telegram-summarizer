package org.flameshine.summarizer.telegram;

import java.nio.file.Paths;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import it.tdlight.Init;
import it.tdlight.Log;
import it.tdlight.Slf4JLogMessageHandler;
import it.tdlight.client.APIToken;
import it.tdlight.client.AuthenticationSupplier;
import it.tdlight.client.SimpleTelegramClient;
import it.tdlight.client.SimpleTelegramClientFactory;
import it.tdlight.client.TDLibSettings;
import it.tdlight.util.UnsupportedNativeLibraryException;

import org.flameshine.summarizer.config.AppConfig;

/**
 * Implementation of {@link TelegramClient} based on <a href="https://github.com/tdlib/td">TDLib</a>.
 */
public class TdlibTelegramClient implements TelegramClient {

    private final SimpleTelegramClient client;

    public TdlibTelegramClient(AppConfig.Telegram telegramConfig) throws UnsupportedNativeLibraryException {

        Init.init();

        Log.setLogMessageHandler(1, new Slf4JLogMessageHandler());

        try (var clientFactory = new SimpleTelegramClientFactory()) {

            var apiToken = new APIToken(telegramConfig.apiId(), telegramConfig.apiHash());
            var settings = TDLibSettings.create(apiToken);
            var sessionPath = Paths.get("tdlight-session");

            settings.setDatabaseDirectoryPath(sessionPath.resolve("data"));
            settings.setDownloadedFilesDirectoryPath(sessionPath.resolve("downloads"));

            var clientBuilder = clientFactory.builder(settings);
            var authenticationData = AuthenticationSupplier.user(telegramConfig.phoneNumber());

            this.client = clientBuilder.build(authenticationData);
        }
    }

    @Override
    public Map<String, List<TelegramMessage>> getMessagesByGroup(List<String> groups, Instant since, Instant until) {

        Map<String, List<TelegramMessage>> builder = new LinkedHashMap<>();

        for (var group : groups) {
            var messages = fetchMessages(group, since, until);
            builder.put(group, messages);
        }

        return Collections.unmodifiableMap(builder);
    }

    @Override
    public void close() throws Exception {
        if (client != null) {
            client.close();
        }
    }
}