package com.vn.smart_space.service.cloudinary;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.api.ApiResponse;
import com.cloudinary.utils.ObjectUtils;
import com.vn.smart_space.dto.request.media.MediaConfirmRequest;
import com.vn.smart_space.dto.response.media.UploadResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "CLOUDINARY_SERVICE")
public class CloudinaryService implements ICloudinaryService {

    private final Cloudinary cloudinary;
    @Value("${cloudinary.folder:smartspace}")
    private String folderName;
    @Value("${cloudinary.api-key}")
    private String apiKey;
    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    @Override
    public Map<String, Object> getUploadSignature() {

        long timestamp = System.currentTimeMillis() / 1000;

        String tags = "tmp";

        Map<String, Object> paramToSign = new HashMap<>();
        paramToSign.put("timestamp", timestamp);
        paramToSign.put("folder", folderName);
        paramToSign.put("tags", tags);

        String signature = cloudinary.apiSignRequest(paramToSign, apiSecret);

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", timestamp);
        response.put("signature", signature);
        response.put("cloud_name", cloudName);
        response.put("api_key", apiKey);
        response.put("folder", folderName);
        response.put("tags", tags);
        return response;

    }

    @Override
    public UploadResponse confirmMedia(MediaConfirmRequest request) {

        try {
            cloudinary.uploader().removeTag("tmp", new String[] { request.getPublicId() },
                    ObjectUtils.asMap("resource_type", request.getResourceType()));

        } catch (Exception e) {
            log.warn("Can not delete tag 'tmp' for publicId={}: {}", request.getPublicId(), e.getMessage());
        }

        return UploadResponse.builder()
                .publicId(request.getPublicId())
                .secureUrl(request.getSecureUrl())
                .build();

    }

    @Override
    public Map<String, Object> cleanupTemporaryMedia() {

        Map<String, Object> totalDeleted = new HashMap<>();

        try {

            log.info("Start delete file has tag 'tmp'");
            ApiResponse imageResult = cloudinary.api().deleteResourcesByTag("tmp",
                    ObjectUtils.asMap("resource_type", "image"));
            totalDeleted.put("images", imageResult.get("deleted"));
            log.info("Dọn dẹp hoàn tất: {}", totalDeleted);

        } catch (Exception e) {
            log.error("Lỗi khi dọn dẹp media tạm thời trên Cloudinary", e);
            throw new RuntimeException("Đã xảy ra lỗi khi xóa file tạm trên Cloudinary");
        }

        return totalDeleted;
    }

    @Scheduled(fixedRate = 3 * 60 * 60 * 1000)
    public void scheduledCleanup() {
        log.info("Tự động kích hoạt dọn rác Cloudinary...");
        try {
            cleanupTemporaryMedia();
        } catch (Exception e) {
            log.error("Lỗi khi dọn rác tự động", e);
        }
    }

}
