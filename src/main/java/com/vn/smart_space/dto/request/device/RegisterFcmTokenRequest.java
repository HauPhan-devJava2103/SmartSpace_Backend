package com.vn.smart_space.dto.request.device;

import jakarta.validation.constraints.NotBlank;

public record RegisterFcmTokenRequest(
        @NotBlank String fcmToken,
        @NotBlank String platform, // "android" | "ios" | "web"
        String deviceName // tuỳ chọn: "Samsung S24", "Chrome"
) {
}