package com.vn.smart_space.service.websocket;

import com.vn.smart_space.dto.request.websocket.CreateConversationRequest;
import com.vn.smart_space.dto.response.websocket.CreateConversationResponse;

public interface IConversationService {

    // 1. Create Conversation
    CreateConversationResponse createConversation(String creatorId, CreateConversationRequest request);

}
