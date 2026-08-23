package com.vn.smart_space.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vn.smart_space.model.ChatMessage;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {

}
