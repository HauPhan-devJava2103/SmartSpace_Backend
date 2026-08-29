package com.vn.smart_space.controller.report;

import com.vn.smart_space.dto.ApiResponse;
import com.vn.smart_space.dto.response.report.ReportResponse;
import com.vn.smart_space.service.report.IReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final IReportService reportService;

    @GetMapping("/dangerous")
    public ResponseEntity<ApiResponse> getDangerousReports() {
        List<ReportResponse> reports = reportService.getDangerousReports();
        return ResponseEntity.ok(ApiResponse.success("Success", reports));
    }

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse> getRecentReports(
            @RequestParam(required = false) String filter,
            @RequestParam(name = "user_lat", required = false) Double userLat,
            @RequestParam(name = "user_long", required = false) Double userLong) {
        
        List<ReportResponse> reports = reportService.getRecentReports(filter, userLat, userLong);
        return ResponseEntity.ok(ApiResponse.success("Success", reports));
    }
}
