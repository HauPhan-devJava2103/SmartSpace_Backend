package com.vn.smart_space.controller.media;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vn.smart_space.dto.ApiResponse;
import com.vn.smart_space.dto.request.media.MediaConfirmRequest;
import com.vn.smart_space.dto.response.media.UploadResponse;
import com.vn.smart_space.service.cloudinary.ICloudinaryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
public class MediaController {

    private final ICloudinaryService cloudinaryService;

    // Lấy signature
    @GetMapping("/signature")
    public ResponseEntity<ApiResponse> getSignature() {
        Map<String, Object> data = cloudinaryService.getUploadSignature();
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy thông tin upload Cloudinary thành công", data));
    }

    // Xác nhận upload
    @PostMapping("/confirm")
    @PreAuthorize("hasAnyRole('admin','staff','client')")
    public ResponseEntity<ApiResponse> confirmMedia(
            @Valid @RequestBody MediaConfirmRequest request) {
        UploadResponse data = cloudinaryService.confirmMedia(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Xác nhận media thành công", data));
    }

    // Dọn file rác
    @DeleteMapping("/cleanup-tmp")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ApiResponse> cleanupTmp() {
        Map<String, Object> result = cloudinaryService.cleanupTemporaryMedia();
        return ResponseEntity.ok(ApiResponse.success(
                "Dọn dẹp file tạm thành công", result));
    }

}
