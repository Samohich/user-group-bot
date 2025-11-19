package ru.samoha.user.group.bot.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Сущность участника группы. Уникален в связке (group_id, username).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "group_member", uniqueConstraints = {
        @UniqueConstraint(name = "uk_group_username", columnNames = {"group_id", "username"})
})
public class GroupMemberEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private GroupEntity group;

    @Column(name = "username", nullable = false, length = 64)
    private String username;

    @Column(name = "display_name", nullable = false, length = 128)
    private String displayName;
}
