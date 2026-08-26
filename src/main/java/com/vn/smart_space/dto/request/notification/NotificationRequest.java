package com.vn.smart_space.dto.request.notification;

import java.util.Map;

import lombok.Builder;

@Builder
public record NotificationRequest(
        String title,
        String body,
        Map<String, String> data) {
}
