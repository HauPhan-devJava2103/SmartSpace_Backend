package com.vn.smart_space.repository;

import com.vn.smart_space.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, String> {

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.isRead = false")
    long countUnreadPersonalNotifications(@Param("userId") String userId);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user IS NULL")
    long countTotalBroadcastNotifications();
}
