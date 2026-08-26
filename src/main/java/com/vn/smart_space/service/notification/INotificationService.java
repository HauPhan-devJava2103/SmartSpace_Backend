package com.vn.smart_space.service.notification;

import com.vn.smart_space.dto.response.notification.NotificationCountResponse;

public interface INotificationService {
    NotificationCountResponse getUnreadCount(String userId);
}
