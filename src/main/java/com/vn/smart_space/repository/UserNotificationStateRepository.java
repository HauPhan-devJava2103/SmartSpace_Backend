package com.vn.smart_space.repository;

import com.vn.smart_space.model.UserNotificationState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserNotificationStateRepository extends JpaRepository<UserNotificationState, String> {

    @Query("SELECT COUNT(uns) FROM UserNotificationState uns WHERE uns.user.id = :userId AND uns.isRead = false")
    long countUnreadBroadcastNotificationsByUser(@Param("userId") String userId);
}
