package ru.samoha.user.group.bot.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.samoha.user.group.bot.model.GroupEntity;

public interface GroupRepository extends JpaRepository<GroupEntity, Long> {
    @Query("select g from GroupEntity g where g.chat.id = :chatId and g.name = :name")
    Optional<GroupEntity> findByChatIdAndName(@Param("chatId") Long chatId, @Param("name") String name);

    @Query("select g from GroupEntity g where g.chat.id = :chatId")
    List<GroupEntity> findAllByChatId(@Param("chatId") Long chatId);

    @Query("select g.name from GroupEntity g where g.chat.id = :chatId order by g.name")
    List<String> findAllNamesByChatId(@Param("chatId") Long chatId);
}


