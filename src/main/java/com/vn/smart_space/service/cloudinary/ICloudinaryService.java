package com.vn.smart_space.service.cloudinary;

import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.vn.smart_space.dto.request.media.MediaConfirmRequest;
import com.vn.smart_space.dto.response.media.UploadResponse;

public interface ICloudinaryService {

    Map<String, Object> getUploadSignature();

    UploadResponse confirmMedia(MediaConfirmRequest request);

    Map<String, Object> cleanupTemporaryMedia();

}
