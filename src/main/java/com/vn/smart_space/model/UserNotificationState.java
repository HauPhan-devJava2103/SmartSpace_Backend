package com.vn.smart_space.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "user_notification_states")
public class UserNotificationState extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false)
    Notification notification;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    Boolean isRead = true; // Record exists usually implies it's read, but keeping flag for flexibility

}
