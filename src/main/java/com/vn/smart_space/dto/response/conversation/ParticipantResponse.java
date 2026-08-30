package com.vn.smart_space.dto.response.conversation;

import lombok.Builder;

@Builder
public record ParticipantResponse(
                String userId,
                String username) {
}
