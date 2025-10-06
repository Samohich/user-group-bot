package ru.samoha.user.group.bot.handler;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import ru.samoha.user.group.bot.service.UserGroupService;

@Component
@RequiredArgsConstructor
public class TelegramBotHandler implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {
    private static final ExecutorService EXECUTOR = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().factory());

    @Value("${telegram.bot.token}")
    private String botToken;

    private final UserGroupService userGroupService;

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(Update update) {
        EXECUTOR.submit(() -> handleUpdate(update));
    }

    private void handleUpdate(Update update) {
        if (update == null || !update.hasMessage()) {
            return;
        }

        Message message = update.getMessage();

        if (message.getText() == null) {
            return;
        }

        String text = message.getText().trim();

        if (text.startsWith("/")) {
            handleCommand(message);
        } else {
            handleRegularMessage(message);
        }
    }

    private void handleCommand(Message message) {
        String text = message.getText();
        String[] parts = text.split("\\s+");
        String command = parts[0].toLowerCase();

        if (parts.length < 2) {
            switch (command) {
                case "/list_groups" -> userGroupService.listGroups(message);
                case "/help" -> userGroupService.sendHelpMessage(message);
                default -> userGroupService.sendMessage(message.getChatId(), message.getMessageId(),
                        "Неизвестная команда. Используйте /help для получения списка доступных команд.");
            }

            return;
        }

        String groupName = normalizeGroupName(parts[1]);

        switch (command) {
            case "/add_group" -> userGroupService.addGroup(message, groupName);
            case "/add_user" -> userGroupService.addUser(message, groupName);
            case "/delete_user" -> userGroupService.deleteUser(message, groupName);
            case "/list_users" -> userGroupService.listUsers(message, groupName);
            default -> userGroupService.sendMessage(message.getChatId(), message.getMessageId(),
                    "Неизвестная команда. Используйте /help для получения списка доступных команд.");
        }
    }

    private void handleRegularMessage(Message message) {
        String text = message.getText();

        if (text == null || text.isBlank()) {
            return;
        }

        String[] tokens = text.split("\\s+");
        Set<String> potencialGroups = new HashSet<>();

        for (String token : tokens) {
            if (token.startsWith("@") && token.length() > 1) {
                potencialGroups.add(normalizeGroupName(token.substring(1)));
            }
        }

        userGroupService.notifyUser(message.getChatId(), message.getMessageId(), potencialGroups);
    }

    private String normalizeGroupName(String name) {
        return name.replace("@", "").trim().toLowerCase();
    }
}
