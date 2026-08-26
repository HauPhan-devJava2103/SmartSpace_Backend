package com.vn.smart_space.service.notification;

import java.util.List;

import com.vn.smart_space.dto.request.notification.NotificationRequest;

public interface IFCMService {

    // Send Notification - All Device User
    void sendToUser(String userId, NotificationRequest request);

    // Send Notification - 1 Device
    void sendToToken(String fcmToken, NotificationRequest request);

    // Send Multiple Devices
    void sendToMultipleTokens(List<String> tokens, NotificationRequest request);
}
