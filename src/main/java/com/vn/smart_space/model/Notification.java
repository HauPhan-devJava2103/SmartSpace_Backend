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
@Table(name = "notifications")
public class Notification extends AbstractEntity {

    @Column(name = "title", nullable = false)
    String title;

    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    String message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    User user; // If null, it's a broadcast notification

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    Boolean isRead = false; // Only used for personal notifications

}
