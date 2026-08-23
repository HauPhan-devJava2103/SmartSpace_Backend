package com.vn.smart_space.service.conversation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.vn.smart_space.consts.EConversationType;
import com.vn.smart_space.dto.PageResponse;
import com.vn.smart_space.dto.request.conversation.CreateConversationRequest;
import com.vn.smart_space.dto.response.conversation.ConversationDetailResponse;
import com.vn.smart_space.dto.response.conversation.CreateConversationResponse;
import com.vn.smart_space.mapper.ConversationMapper;
import com.vn.smart_space.model.Conversation;
import com.vn.smart_space.model.User;
import com.vn.smart_space.repository.ConversationRepository;
import com.vn.smart_space.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConversationService implements IConversationService {

    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;

    // 1. Create Conversation
    @Override
    public CreateConversationResponse createConversation(String creatorId, CreateConversationRequest request) {

        List<String> participantIds = new ArrayList<>(request.participantIds());

        // Check creator
        if (!participantIds.contains(creatorId)) {
            participantIds.add(creatorId);
        }

        // Get List User
        List<User> participantInfos = userRepository.findAllById(participantIds);

        // Check user exists
        if (participantInfos.size() != participantIds.size()) {
            throw new IllegalArgumentException("Not found user participant");
        }

        EConversationType conversationType = request.conversationType();
        String participantHash = null;
        if (conversationType.equals(EConversationType.PRIVATE)) {
            if (participantIds.size() != 2)
                throw new IllegalArgumentException("Private conversation must have 2 participants");

            participantHash = participantInfos.stream()
                    .map(User::getId)
                    .sorted()
                    .collect(Collectors.joining("_"));
            // Check conversation exists
            Optional<Conversation> existing = conversationRepository.findByParticipantHash(participantHash);
            if (existing.isPresent()) {
                // Nếu đã tồn tại, trả về conversation cũ
                return ConversationMapper.toConversationResponse(creatorId, existing.get());
            }
        }

        // Group Conversation
        if (conversationType == EConversationType.GROUP) {
            // Group conversation must be has name
            if (request.name() == null || request.name().trim().isEmpty())
                throw new IllegalArgumentException("Group conversation must be has name");

            // Group conversation must be has at least 3 participants
            if (participantIds.size() < 3)
                throw new IllegalArgumentException("Group conversation must be has at least 3 participants");
        }

        Conversation conversation = Conversation.builder()
                .name(request.name())
                .conversationType(conversationType)
                .conversationAvatar(request.conversationAvatar())
                .participantHash(participantHash) // Chỉ có giá trị với PRIVATE
                .createdAt(LocalDateTime.now())
                .build();

        participantInfos.forEach(conversation::addParticipants);
        conversationRepository.save(conversation);

        return ConversationMapper.toConversationResponse(creatorId, conversation);

    }

    @Override
    public PageResponse<ConversationDetailResponse> getMyConversation(String userId, int page, int size) {

        Pageable pageable = PageRequest.of(page - 1, size);

        Page<Conversation> conversationPage = conversationRepository.findAllByUserId(userId, pageable);

        List<Conversation> conversations = conversationPage.getContent();

        List<ConversationDetailResponse> responses = conversations.stream()
                .map(conversation -> ConversationMapper.toConversationDetailResponse(userId, conversation))
                .toList();

        return PageResponse.<ConversationDetailResponse>builder()
                .currentPage(page)
                .pageSize(pageable.getPageSize())
                .totalPages(conversationPage.getTotalPages())
                .totalElements(conversationPage.getTotalElements())
                .content(responses)
                .build();
    }

}
