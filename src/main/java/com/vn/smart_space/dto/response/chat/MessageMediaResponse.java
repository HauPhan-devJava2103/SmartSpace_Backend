package com.vn.smart_space.dto.response.chat;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record MessageMediaResponse(
        String fileName, // Tên file
        String fileType, // Loại file
        String thumbnailUrl, // URL của file
        LocalDateTime uploadedAt // Thời gian upload
) {
}
