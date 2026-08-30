package com.vn.smart_space.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vn.smart_space.model.Conversation;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, String> {

    @EntityGraph(attributePaths = { "participants", "participants.user" })
    Optional<Conversation> findByParticipantHash(String participantHash);

    // Get Page Conversation
    @EntityGraph(attributePaths = { "participants", "participants.user" })
    @Query("SELECT DISTINCT c FROM Conversation c JOIN c.participants p WHERE p.user.id = :userId ORDER BY c.lastMessageTime DESC NULLS LAST")
    Page<Conversation> findAllByUserId(@Param("userId") String userId, Pageable pageable);

    // Check conversation exists and sender is member
    @Query("SELECT c FROM Conversation c " +
            "WHERE c.id = :conversationId " +
            "AND EXISTS(SELECT p FROM c.participants p WHERE p.user.id = :userId)")
    Optional<Conversation> findByIdAndMember(@Param("conversationId") String conversationId,
            @Param("userId") String userId);

}
