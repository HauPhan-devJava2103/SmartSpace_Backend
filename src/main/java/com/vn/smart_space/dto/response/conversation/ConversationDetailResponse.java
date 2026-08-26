package com.vn.smart_space.dto.response.conversation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vn.smart_space.consts.EConversationType;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConversationDetailResponse {
    private String id;
    private EConversationType conversationType;
    private String name;
    private String conversationAvatar;
    private List<ParticipantResponse> participantInfo;

    // Thông tin tin nhắn cuối cùng
    private String lastMessageId;
    private String lastMessageContent;
    private LocalDateTime lastMessageTime;

    private LocalDateTime createdAt;
}