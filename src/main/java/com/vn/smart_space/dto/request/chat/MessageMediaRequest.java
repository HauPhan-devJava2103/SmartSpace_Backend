package com.vn.smart_space.dto.request.chat;

public record MessageMediaRequest(
        String fileName, // Tên file (ví dụ: "image.jpg")
        String fileType, // Loại file (ví dụ: "image/jpeg")
        String thumbnailUrl // URL của file đã upload Cloudinary
) {
}