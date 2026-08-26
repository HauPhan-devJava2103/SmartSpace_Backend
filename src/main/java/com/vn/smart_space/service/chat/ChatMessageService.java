package com.vn.smart_space.service.chat;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vn.smart_space.dto.request.chat.ChatMessageRequest;
import com.vn.smart_space.dto.response.chat.ChatMessageResponse;
import com.vn.smart_space.dto.response.chat.MessageMediaResponse;
import com.vn.smart_space.exception.BadRequestException;
import com.vn.smart_space.model.ChatMessage;
import com.vn.smart_space.model.Conversation;
import com.vn.smart_space.model.MessageMedia;
import com.vn.smart_space.model.User;
import com.vn.smart_space.repository.ChatMessageRepository;
import com.vn.smart_space.repository.ConversationRepository;
import com.vn.smart_space.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatMessageService implements IChatMessageService {

    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;

    // 1. Send Message To User
    @Transactional(rollbackFor = Exception.class)
    @Override
    public ChatMessageResponse sendMessage(String senderId, ChatMessageRequest request) {

        // 1. Validate Exists User

        User sender = userRepository.findById(senderId).orElseThrow(() -> new BadRequestException("Sender not found"));

        // 2. Validate Conversation exists and Sender is member
        Conversation conversation = conversationRepository.findByIdAndMember(request.conversationId(), senderId)
                .orElseThrow(() -> new BadRequestException("Conversation not found or sender is not a member"));

        // 3. Media - JSON
        List<MessageMedia> media = request.messageMedia() != null && !request.messageMedia().isEmpty()
                ? request.messageMedia().stream()
                        .map(messageMedia -> MessageMedia.builder()
                                .fileName(messageMedia.fileName())
                                .fileType(messageMedia.fileType())
                                .thumbnailUrl(messageMedia.thumbnailUrl())
                                .build())
                        .toList()
                : List.of();

        // 4. Tạo chat message
        ChatMessage message = ChatMessage.builder()
                .conversation(conversation)
                .sender(sender)
                .content(request.content())
                .messageType(request.messageType())
                .mediaFiles(media) // List<MessageMedia> sẽ được lưu dưới dạng JSON
                .build();

        // 5. Lưu message vào database
        chatMessageRepository.save(message);

        // 6. Update lastMessage của conversation
        conversation.setLastMessageId(message.getId());
        conversation.setLastMessageContent(message.getContent());
        conversation.setLastMessageTime(message.getSentAt());
        conversationRepository.save(conversation);

        // 7. Map entity sang response DTO
        return ChatMessageResponse.builder()
                .id(message.getId())
                .tempId(request.tempId())
                .conversationId(message.getConversation().getId())
                .conversationAvatar(message.getConversation().getConversationAvatar())
                .senderId(sender.getId())
                .senderName(sender.getFullName())
                .content(message.getContent())
                .messageType(message.getMessageType())
                .messageMedia(message.getMediaFiles().stream()
                        .map(messageMedia -> MessageMediaResponse.builder()
                                .fileName(messageMedia.getFileName())
                                .fileType(messageMedia.getFileType())
                                .thumbnailUrl(messageMedia.getThumbnailUrl())
                                .uploadedAt(messageMedia.getUploadedAt())
                                .build())
                        .toList())
                .createdAt(message.getSentAt())
                .build();

    }

}
