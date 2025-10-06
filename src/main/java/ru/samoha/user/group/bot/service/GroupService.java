package ru.samoha.user.group.bot.service;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.samoha.user.group.bot.model.ChatEntity;
import ru.samoha.user.group.bot.model.GroupEntity;
import ru.samoha.user.group.bot.model.GroupMemberEntity;
import ru.samoha.user.group.bot.repository.ChatRepository;
import ru.samoha.user.group.bot.repository.GroupMemberRepository;
import ru.samoha.user.group.bot.repository.GroupRepository;

@Service
@RequiredArgsConstructor
public class GroupService {
    private final ChatRepository chatRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;

    @Transactional(readOnly = true)
    public boolean existGroup(Long chatId, String groupName) {
        return groupRepository.findByChatIdAndName(chatId, groupName).isPresent();
    }

    @Transactional
    public void createGroup(Long chatId, String groupName) {
        ChatEntity chat = chatRepository.findById(chatId).orElseGet(() -> {
            ChatEntity c = new ChatEntity();
            c.setId(chatId);

            return chatRepository.save(c);
        });

        GroupEntity group = new GroupEntity();
        group.setChat(chat);
        group.setName(groupName);
        groupRepository.save(group);
    }

    @Transactional
    public void addMemberToGroup(Long chatId, String groupName, String username, String displayName) {
        GroupEntity group = groupRepository.findByChatIdAndName(chatId, groupName)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        String normalized = normalizeUsername(username);
        Optional<GroupMemberEntity> existing = groupMemberRepository.findByGroupIdAndUsername(group.getId(), normalized);

        if (existing.isPresent()) {
            return;
        }

        GroupMemberEntity member = new GroupMemberEntity();
        member.setGroup(group);
        member.setUsername(normalized);
        member.setDisplayName(displayName != null && !displayName.isBlank() ? displayName : normalized);
        groupMemberRepository.save(member);
    }

    @Transactional
    public boolean removeMemberFromGroup(Long chatId, String groupName, String username) {
        GroupEntity group = groupRepository.findByChatIdAndName(chatId, groupName).orElse(null);

        if (group == null) {
            return false;
        }

        int affected = groupMemberRepository.deleteByGroupIdAndUsername(group.getId(), normalizeUsername(username));

        return affected > 0;
    }

    @Transactional
    public boolean removeMemberAllGrops(Long chatId, String ignoredGroupName, String username) {
        int affected = groupMemberRepository.deleteByChatIdAndUsername(chatId, normalizeUsername(username));

        return affected > 0;
    }

    @Transactional(readOnly = true)
    public List<String> listGroupNames(Long chatId) {
        return groupRepository.findAllNamesByChatId(chatId);
    }

    @Transactional(readOnly = true)
    public List<GroupMemberEntity> getMembers(Long chatId, String groupName) {
        GroupEntity group = groupRepository.findByChatIdAndName(chatId, groupName)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        return groupMemberRepository.findAllByGroupId(group.getId());
    }

    @Transactional(readOnly = true)
    public List<GroupEntity> listGroup(Long chatId) {
        return groupRepository.findAllByChatId(chatId);
    }

    private String normalizeUsername(String username) {
        if (username == null) {
            return null;
        }

        String v = username.trim();

        if (v.startsWith("@")) {
            v = v.substring(1);
        }

        return v.toLowerCase();
    }
}


