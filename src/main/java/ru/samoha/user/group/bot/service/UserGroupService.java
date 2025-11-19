package ru.samoha.user.group.bot.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.samoha.user.group.bot.model.GroupEntity;
import ru.samoha.user.group.bot.model.GroupMemberEntity;

/**
 * Прикладной сервис, который исполняет сценарии, вызываемые из обработчика бота.
 * Содержит операции создания групп, управления участниками и отправки сообщений.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserGroupService {
    private final TelegramClient client;
    private final GroupService groupService;

    /** Создаёт группу в текущем чате. */
    public void addGroup(Message message, String groupName) {
        if (groupService.existGroup(message.getChatId(), groupName)) {
            sendMessage(message.getChatId(), message.getMessageId(), "Группа '" + groupName + "' уже существует.");
            return;
        }

        groupService.createGroup(message.getChatId(), groupName);
        sendMessage(message.getChatId(), message.getMessageId(), "Группа '" + groupName + "' создана.");
    }

    /** Добавляет пользователей (упомянутых в сообщении) в группу. */
    public void addUser(Message message, String groupName) {
        List<MessageEntity> entities = message.getEntities();
        List<AddedUser> usersToAdd = new ArrayList<>();

        if (CollectionUtils.isEmpty(entities)) {
            return;
        }

        for (MessageEntity entity : entities) {
            switch (entity.getType()) {
                case "text_mention" -> {
                    User user = entity.getUser();
                    if (user != null) {
                        String username = user.getUserName();
                        String displayName = user.getFirstName();
                        usersToAdd.add(new AddedUser(username, displayName));
                    }
                }
                case "mention" -> {
                    String usernameWithAt = message.getText()
                            .substring(entity.getOffset(), entity.getOffset() + entity.getLength());
                    String username = usernameWithAt.replace("@", "");
                    usersToAdd.add(new AddedUser(username, username));
                }
            }
        }

        if (usersToAdd.isEmpty()) {
            sendMessage(message.getChatId(), message.getMessageId(),
                    "Список пользователей пуст. Чтобы добавить пользователей в группу, используйте /add_user @mention");
            return;
        }

        for (AddedUser user : usersToAdd) {
            groupService.addMemberToGroup(message.getChatId(), groupName, user.username(), user.displayName());
        }

        sendMessage(message.getChatId(), message.getMessageId(), String.format("Пользователь добавлен в группу '%s'.", groupName));
    }

    /** Удаляет пользователей из указной группы или из всех групп, если группа не существует. */
    public void deleteUser(Message message, String groupName) {
        List<MessageEntity> entities = message.getEntities();
        boolean removedAny = false;
        boolean foundUser = false;

        if (CollectionUtils.isEmpty(entities)) {
            return;
        }

        for (MessageEntity entity : entities) {
            switch (entity.getType()) {
                case "text_mention" -> {
                    User user = entity.getUser();
                    if (user != null) {
                        foundUser = true;
                        removedAny = removedAny || removeUser(message.getChatId(), groupName, user.getUserName());
                    }
                }
                case "mention" -> {
                    String usernameWithAt = message.getText()
                            .substring(entity.getOffset(), entity.getOffset() + entity.getLength());
                    String username = usernameWithAt.replace("@", "");
                    foundUser = true;

                    removedAny = removedAny || removeUser(message.getChatId(), groupName, username);
                }
            }
        }

        if (!foundUser) {
            sendMessage(message.getChatId(), message.getMessageId(),
                    "Список пользователей пуст. Чтобы удалить пользователей в группе, используйте /delete_user @mention.");
            return;
        }

        if (removedAny) {
            sendMessage(message.getChatId(), message.getMessageId(),
                    String.format("Пользователь(и) удален(ы) из группы '%s'.", groupName));
        } else {
            sendMessage(message.getChatId(), message.getMessageId(),
                    String.format("Пользователь(и) не найден(ы) в группе '%s'.", groupName));
        }
    }

    /** Отправляет список групп текущего чата. */
    public void listGroups(Message message) {
        List<String> groups = groupService.listGroupNames(message.getChatId());
        if (groups.isEmpty()) {
            sendMessage(message.getChatId(), message.getMessageId(), "Нет созданных групп.");
        } else {
            StringBuilder sb = new StringBuilder("Список групп:\n");
            for (String name : groups) {
                sb.append("- ").append(name).append("\n");
            }
            sendMessage(message.getChatId(), message.getMessageId(), sb.toString());
        }
    }

    /** Отправляет список участников указанной группы. */
    public void listUsers(Message message, String groupName) {
        List<GroupMemberEntity> members = groupService.getMembers(message.getChatId(), groupName);
        if (members.isEmpty()) {
            sendMessage(message.getChatId(), message.getMessageId(),
                    String.format("В группе '%s' нет участников.", groupName));
        } else {
            StringBuilder sb = new StringBuilder("Участники группы '").append(groupName).append("':\n");
            for (GroupMemberEntity member : members) {
                sb.append("- @").append(member.getUsername()).append("\n");
            }
            sendMessage(message.getChatId(), message.getMessageId(), sb.toString());
        }
    }

    /** Отправляет краткую справку по командам. */
    public void sendHelpMessage(Message message) {
        String help = """
                Доступные команды:
                /add_group <имя_группы> – создать группу.
                /add_user <имя_группы> @user – добавить пользователя в группу (можно отметить несколько).
                /delete_user <имя_группы> @user – удалить пользователя из группы (можно отметить несколько).
                /delete_user @user – удалить пользователя из всех групп (можно отметить несколько).
                /list_groups – вывести список групп.
                /list_users <имя_группы> – вывести участников группы.
                /help – вывести эту справку.""";
        sendMessage(message.getChatId(), message.getMessageId(), help);
    }

    /**
     * Собирает участников для упомянутых групп и отправляет @упоминания одним сообщением.
     */
    public void notifyUser(Long chatId, Integer replyMessageId, Set<String> potencialGroups) {
        List<GroupEntity> groups = groupService.listGroup(chatId);
        Set<String> mentions = new HashSet<>();

        for (GroupEntity g : groups) {
            if (!potencialGroups.contains(g.getName())) {
                continue;
            }

            List<GroupMemberEntity> members = groupService.getMembers(chatId, g.getName());

            for (GroupMemberEntity m : members) {
                String username = m.getUsername();
                if (username == null || username.isBlank()) {
                    continue;
                }
                String tag = username.startsWith("@") ? username : "@" + username;
                mentions.add(tag);
            }
        }

        sendMessage(chatId, replyMessageId, String.join(" ", mentions));
    }

    /** Отправляет ответное сообщение в чат. */
    public void sendMessage(Long chatId, Integer replyMessageId, String text) {
        SendMessage msg = SendMessage.builder()
                .chatId(chatId)
                .replyToMessageId(replyMessageId)
                .text(text)
                .build();

        try {
            client.execute(msg);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщения", e);
        }
    }

    private boolean removeUser(Long chatId, String groupName, String userName) {
        if (groupService.existGroup(chatId, groupName)) {
            return groupService.removeMemberFromGroup(chatId, groupName, userName);
        }

        return groupService.removeMemberAllGroups(chatId, groupName, userName);
    }

    private record AddedUser(String username, String displayName) {
    }
}
