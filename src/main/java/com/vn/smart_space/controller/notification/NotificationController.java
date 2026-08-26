package com.vn.smart_space.controller.notification;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vn.smart_space.dto.ApiResponse;
import com.vn.smart_space.dto.request.notification.NotificationRequest;
import com.vn.smart_space.service.notification.IFCMService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {
    private final IFCMService fcmService;

    @PostMapping("/test")
    public ResponseEntity<ApiResponse> testPush(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId").toString();

        fcmService.sendToUser(
                userId,
                new NotificationRequest(
                        "SmartSpace Test",
                        "Notification đã hoạt động!",
                        Map.of("type", "test", "timestamp", String.valueOf(System.currentTimeMillis()))));

        return ResponseEntity.ok(ApiResponse.success("Test notification sent to all devices", null));
    }
}
