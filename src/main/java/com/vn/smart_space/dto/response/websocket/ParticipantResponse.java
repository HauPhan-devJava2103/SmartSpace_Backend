package com.vn.smart_space.dto.response.websocket;

import lombok.Builder;

@Builder
public record ParticipantResponse(
        String userId,
        String username) {
}
