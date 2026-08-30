package com.vn.smart_space.controller.chat;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vn.smart_space.dto.ApiResponse;
import com.vn.smart_space.dto.request.chat.ChatMessageRequest;
import com.vn.smart_space.service.chat.IChatMessageService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat-messages")
public class ChatMessageController {
    private final IChatMessageService chatMessageService;

    @PostMapping
    public ResponseEntity<ApiResponse> sendChatMessage(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid ChatMessageRequest request) {

        var senderId = jwt.getClaims().get("userId").toString();
        var data = chatMessageService.sendMessage(senderId, request);

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Send chat message successfully")
                .data(data)
                .build());
    }

}
