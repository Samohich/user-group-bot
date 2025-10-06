package ru.samoha.user.group.bot.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import ru.samoha.user.group.bot.model.GroupMemberEntity;

/**
 * Репозиторий участников групп.
 */
public interface GroupMemberRepository extends JpaRepository<GroupMemberEntity, Long> {
    @Query("select m from GroupMemberEntity m where m.group.id = :groupId and m.username = :username")
    Optional<GroupMemberEntity> findByGroupIdAndUsername(@Param("groupId") Long groupId, @Param("username") String username);

    @Query("select m from GroupMemberEntity m where m.group.id = :groupId")
    List<GroupMemberEntity> findAllByGroupId(@Param("groupId") Long groupId);

    @Transactional
    @Modifying
    @Query("delete from GroupMemberEntity m where m.group.id = :groupId and m.username = :username")
    int deleteByGroupIdAndUsername(@Param("groupId") Long groupId, @Param("username") String username);

    @Transactional
    @Modifying
    @Query("delete from GroupMemberEntity m where m.group.chat.id = :chatId and m.username = :username")
    int deleteByChatIdAndUsername(@Param("chatId") Long chatId, @Param("username") String username);
}


