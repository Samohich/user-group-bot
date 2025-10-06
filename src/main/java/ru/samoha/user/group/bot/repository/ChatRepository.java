package ru.samoha.user.group.bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.samoha.user.group.bot.model.ChatEntity;

public interface ChatRepository extends JpaRepository<ChatEntity, Long> {
}


