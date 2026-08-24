package com.vn.smart_space.service.notification;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.vn.smart_space.dto.request.notification.NotificationRequest;
import com.vn.smart_space.repository.DeviceTokenRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j(topic = "FCM_SERVICE")
@RequiredArgsConstructor
public class FCMService implements IFCMService {

    private final DeviceTokenRepository deviceTokenRepository;

    @Override
    public void sendToUser(String userId, NotificationRequest request) {
        List<String> tokens = deviceTokenRepository.findFcmTokensByUserId(userId);
        if (tokens.isEmpty()) {
            log.warn("No FCM tokens for user: {}", userId);
            return;
        }

        if (tokens.size() == 1) {
            sendToToken(tokens.get(0), request);
        } else {
            sendToMultipleTokens(tokens, request);
        }
    }

    @Override
    public void sendToToken(String fcmToken, NotificationRequest request) {
        try {
            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle(request.title())
                            .setBody(request.body())
                            .build())
                    .putAllData(request.data() != null ? request.data() : Map.of())
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("FCM sent: {}", response);
        } catch (FirebaseMessagingException e) {
            log.error("FCM failed for token {}: {}", fcmToken.substring(0, 20), e.getMessage());
        }
    }

    @Override
    public void sendToMultipleTokens(List<String> tokens, NotificationRequest request) {
        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(tokens)
                .setNotification(Notification.builder()
                        .setTitle(request.title())
                        .setBody(request.body())
                        .build())
                .putAllData(request.data() != null ? request.data() : Map.of())
                .build();

        try {
            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
            log.info("FCM multicast: {} success, {} failed",
                    response.getSuccessCount(), response.getFailureCount());
        } catch (FirebaseMessagingException e) {
            log.error("FCM multicast failed: {}", e.getMessage());
        }
    }
}
