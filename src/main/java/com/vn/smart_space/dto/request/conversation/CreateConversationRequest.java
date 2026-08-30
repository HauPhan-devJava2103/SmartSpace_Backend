package com.vn.smart_space.dto.request.conversation;

import java.util.List;

import com.vn.smart_space.consts.EConversationType;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CreateConversationRequest(
                String name, // Tên conversation (bắt buộc với GROUP, không cần với PRIVATE)
                String conversationAvatar, // Avatar của nhóm (optional)

                @NotNull(message = "Conversation type is required") EConversationType conversationType, // PRIVATE hoặc
                                                                                                        // GROUP

                @NotEmpty(message = "Participant ids are required") List<String> participantIds // Danh sách userId của
                                                                                                // người
                                                                                                // tham gia
) {
}
