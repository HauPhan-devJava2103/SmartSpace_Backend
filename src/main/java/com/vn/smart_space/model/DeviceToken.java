package com.vn.smart_space.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "device_tokens", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "fcm_token" }))
public class DeviceToken extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Column(name = "fcm_token", nullable = false, length = 512)
    String fcmToken;

    @Column(name = "platform", nullable = false)
    String platform; // "android" | "ios" | "web"

    @Column(name = "device_name")
    String deviceName; // tuỳ chọn, ví dụ: "Samsung S24", "Chrome"

    @Column(name = "last_used_at")
    LocalDateTime lastUsedAt;
}
