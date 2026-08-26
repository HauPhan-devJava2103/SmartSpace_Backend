package com.vn.smart_space.dto.response.report;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReportResponse {
    private String id;
    private String title;
    private String description;

    @JsonProperty("image_url")
    private String imageUrl;

    private Double latitude;
    private Double longitude;
    private String status;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("distance_in_meters")
    private Double distanceInMeters;
}
