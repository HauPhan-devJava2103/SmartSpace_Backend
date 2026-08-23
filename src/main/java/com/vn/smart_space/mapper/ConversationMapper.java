package com.vn.smart_space.mapper;

import com.vn.smart_space.consts.EConversationType;
import com.vn.smart_space.dto.response.websocket.CreateConversationResponse;
import com.vn.smart_space.dto.response.websocket.ParticipantResponse;
import com.vn.smart_space.model.Conversation;

public final class ConversationMapper {
    private ConversationMapper() {
    }

    public static CreateConversationResponse toConversationResponse(String creatorId, Conversation conversation) {
        EConversationType conversationType = conversation.getConversationType();

        CreateConversationResponse response = CreateConversationResponse.builder()
                .id(conversation.getId())
                .conversationType(conversationType)
                // Map DS participantInfo
                .participantInfo(conversation.getParticipants().stream()
                        .map(participants -> ParticipantResponse.builder()
                                .userId(participants.getUser().getId())
                                .username(participants.getUser().getFullName())
                                .build())
                        .toList())
                .createdAt(conversation.getCreatedAt())
                .build();

        if (conversationType == EConversationType.PRIVATE) {
            // PRIVATE
            conversation.getParticipants().stream()
                    .filter(participants -> !participants.getUser().getId().equals(creatorId)).findFirst()
                    .ifPresent(participantInfo -> response.setName(participantInfo.getUser().getFullName()));
        } else {
            // GROUP
            response.setName(conversation.getName());
            response.setConversationAvatar(conversation.getConversationAvatar());
        }

        return response;
    }
}
