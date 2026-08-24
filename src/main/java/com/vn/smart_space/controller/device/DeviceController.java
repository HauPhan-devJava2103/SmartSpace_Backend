package com.vn.smart_space.controller.device;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vn.smart_space.dto.ApiResponse;
import com.vn.smart_space.dto.request.device.RegisterFcmTokenRequest;
import com.vn.smart_space.service.device.IDeviceService;

import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/devices")
public class DeviceController {
    private final IDeviceService deviceService;

    @PostMapping("/fcm-token")
    public ResponseEntity<ApiResponse> registerFcmToken(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid RegisterFcmTokenRequest request) {
        String userId = jwt.getClaim("userId").toString();
        deviceService.registerFcmToken(userId, request);
        return ResponseEntity.ok(ApiResponse.success("FCM token registered", null));
    }

    @DeleteMapping("/fcm-token")
    public ResponseEntity<ApiResponse> clearFcmToken(
            @RequestParam String fcmToken) {
        deviceService.clearFcmToken(fcmToken);
        return ResponseEntity.ok(ApiResponse.success("FCM token cleared", null));
    }
}