package com.vn.smart_space.dto.response.conversation;

import com.vn.smart_space.consts.EConversationType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class CreateConversationResponse {
    private String id;
    private String name; // Tên conversation (với PRIVATE: tên của người còn lại, với GROUP: tên nhóm)
    private String conversationAvatar;
    private EConversationType conversationType;
    private List<ParticipantResponse> participantInfo; // Danh sách thông tin participants
    private LocalDateTime createdAt;
}