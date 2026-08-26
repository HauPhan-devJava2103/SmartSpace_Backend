package com.vn.smart_space.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.vn.smart_space.consts.EConversationType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "conversations")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;

    private String conversationAvatar;

    @Enumerated(EnumType.STRING)
    private EConversationType conversationType;

    @Column(name = "participant_hash", unique = true)
    private String participantHash;

    private LocalDateTime createdAt;

    private String lastMessageId;

    private String lastMessageContent;

    private LocalDateTime lastMessageTime;

    // Relationship
    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ConversationParticipant> participants = new ArrayList<>();

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ChatMessage> messages = new ArrayList<>();

    // Helpers
    public void addParticipants(User user) {
        participants.add(ConversationParticipant.builder()
                .conversation(this)
                .user(user)
                .build());
    }
}
