package com.vn.smart_space.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vn.smart_space.model.DeviceToken;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, String> {

    // Get all device token's user
    List<DeviceToken> findAllByUserId(String userId);

    // Find token
    Optional<DeviceToken> findByUserIdAndFcmToken(String userId, String fcmToken);

    // Delete when Logout
    void deleteByFcmToken(String fcmToken);

    // Get fcmToken strings for multicast
    @Query("SELECT dt.fcmToken FROM DeviceToken dt WHERE dt.user.id = :userId")
    List<String> findFcmTokensByUserId(@Param("userId") String userId);
}

