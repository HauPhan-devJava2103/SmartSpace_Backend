package com.vn.smart_space.controller.conversation;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vn.smart_space.dto.ApiResponse;
import com.vn.smart_space.dto.request.conversation.CreateConversationRequest;
import com.vn.smart_space.dto.response.conversation.CreateConversationResponse;
import com.vn.smart_space.service.conversation.IConversationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/conversations")
public class ConversationController {

    private final IConversationService conversationService;

    // 1. Create Conversation
    @PostMapping("")
    public ResponseEntity<ApiResponse> createConversation(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid CreateConversationRequest request) {

        var userId = jwt.getClaim("userId").toString();

        CreateConversationResponse response = conversationService.createConversation(userId, request);

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .data(response)
                .message("Conversation created successfully")
                .build());
    }

    // Get My Conversation
    @GetMapping("/my-conversation")
    public ResponseEntity<ApiResponse> getMyConversation(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {

        var userId = jwt.getClaim("userId").toString();
        var data = conversationService.getMyConversation(userId, page, size);

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .data(data)
                .message("My conversation retrieved successfully")
                .build());
    }

}
