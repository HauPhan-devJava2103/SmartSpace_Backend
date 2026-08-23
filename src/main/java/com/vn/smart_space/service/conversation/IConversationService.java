package com.vn.smart_space.service.conversation;

import com.vn.smart_space.dto.PageResponse;
import com.vn.smart_space.dto.request.conversation.CreateConversationRequest;
import com.vn.smart_space.dto.response.conversation.ConversationDetailResponse;
import com.vn.smart_space.dto.response.conversation.CreateConversationResponse;

public interface IConversationService {

    // 1. Create Conversation
    CreateConversationResponse createConversation(String creatorId, CreateConversationRequest request);

    PageResponse<ConversationDetailResponse> getMyConversation(String userId, int page, int size);

}
