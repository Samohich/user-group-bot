package ru.samoha.user.group.bot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Сущность чата Telegram. Хранит идентификатор чата и используется
 * как владелец групп. Один чат — много групп.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "chat")
public class ChatEntity {
    @Id
    @Column(name = "id")
    private Long id;
}
