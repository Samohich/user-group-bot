package ru.samoha.user.group.bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.samoha.user.group.bot.model.ChatEntity;

/**
 * Репозиторий чатов.
 */
public interface ChatRepository extends JpaRepository<ChatEntity, Long> {
}
