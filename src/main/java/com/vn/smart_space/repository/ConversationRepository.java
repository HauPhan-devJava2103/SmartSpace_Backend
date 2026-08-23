package com.vn.smart_space.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vn.smart_space.model.Conversation;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, String> {

    @EntityGraph(attributePaths = { "participants", "participants.user" })
    Optional<Conversation> findByParticipantHash(String participantHash);
}
