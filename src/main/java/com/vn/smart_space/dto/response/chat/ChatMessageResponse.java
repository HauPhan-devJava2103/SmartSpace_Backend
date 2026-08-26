package com.vn.smart_space.dto.response.chat;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vn.smart_space.consts.EMessageType;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse implements Serializable {
    private String id; // Message ID từ database
    private String tempId; // Temporary ID từ client
    private String conversationId;
    private String conversationAvatar;
    private String senderId;
    private String senderName;
    private String content;
    private EMessageType messageType;
    private List<MessageMediaResponse> messageMedia;
    private LocalDateTime createdAt;
}