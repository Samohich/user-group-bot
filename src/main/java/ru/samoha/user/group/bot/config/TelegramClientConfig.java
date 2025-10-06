package ru.samoha.user.group.bot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * Конфигурация Telegram-клиента.
 * <p>
 * Создаёт и настраивает {@link TelegramClient}, который используется сервисами
 * для отправки сообщений и взаимодействия с Bot API.
 */
@Configuration
public class TelegramClientConfig {
    @Value("${telegram.bot.token}")
    private String botToken;

    /**
     * Бин Telegram-клиента на базе OkHttp.
     *
     * @return готовый к использованию {@link TelegramClient}
     */
    @Bean
    public TelegramClient telegramClient() {
        return new OkHttpTelegramClient(botToken);
    }
}
