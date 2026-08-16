package com.vn.smart_space.dto.request.media;

import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MediaConfirmRequest {
    @NotBlank(message = "publicId không được để trống")
    private String publicId;
    @NotBlank(message = "secureUrl không được để trống")
    private String secureUrl;
    @NotBlank(message = "resourceType không được để trống")
    private String resourceType; // "image"
}
