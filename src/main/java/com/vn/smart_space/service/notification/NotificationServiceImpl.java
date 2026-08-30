package com.vn.smart_space.service.notification;

import com.vn.smart_space.dto.response.notification.NotificationCountResponse;
import com.vn.smart_space.repository.NotificationRepository;
import com.vn.smart_space.repository.UserNotificationStateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements INotificationService {

    private final NotificationRepository notificationRepository;
    private final UserNotificationStateRepository userNotificationStateRepository;

    @Override
    @Transactional(readOnly = true)
    public NotificationCountResponse getUnreadCount(String userId) {
        long unreadPersonal = notificationRepository.countUnreadPersonalNotifications(userId);
        long unreadBroadcast = userNotificationStateRepository.countUnreadBroadcastNotificationsByUser(userId);
        
        long totalUnread = unreadPersonal + unreadBroadcast;

        return NotificationCountResponse.builder()
                .notifNumber(totalUnread)
                .build();
    }
}
