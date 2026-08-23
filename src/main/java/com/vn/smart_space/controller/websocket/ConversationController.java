package com.vn.smart_space.controller.websocket;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vn.smart_space.dto.ApiResponse;
import com.vn.smart_space.dto.request.websocket.CreateConversationRequest;
import com.vn.smart_space.dto.response.websocket.CreateConversationResponse;
import com.vn.smart_space.service.websocket.IConversationService;

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

}
