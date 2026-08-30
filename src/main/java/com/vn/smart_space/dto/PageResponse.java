package com.vn.smart_space.dto;

import lombok.*;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PageResponse<T> implements Serializable {
    private int currentPage; // Page hiện tại (1-based)
    private int pageSize; // Số lượng items per page
    private int totalPages; // Tổng số pages
    private long totalElements; // Tổng số items

    @Builder.Default
    private List<T> content = Collections.emptyList(); // Danh sách items trong page
}