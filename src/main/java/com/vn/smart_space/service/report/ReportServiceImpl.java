package com.vn.smart_space.service.report;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vn.smart_space.consts.EReportSeverity;
import com.vn.smart_space.dto.response.report.ReportResponse;
import com.vn.smart_space.model.Report;
import com.vn.smart_space.repository.ReportRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements IReportService {

    private final ReportRepository reportRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ReportResponse> getDangerousReports() {
        List<Report> reports = reportRepository.findTopBySeverityInOrderByCreatedAtDesc(
                Arrays.asList(EReportSeverity.HIGH, EReportSeverity.CRITICAL),
                Limit.of(5));
        return reports.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportResponse> getRecentReports(String filter, Double userLat, Double userLong) {
        List<Report> reports;
        boolean calculateDistance = false;

        if ("near_by".equalsIgnoreCase(filter) && userLat != null && userLong != null) {
            reports = reportRepository.findNearestReports(userLat, userLong, Limit.of(5));
            calculateDistance = true;
        } else {
            reports = reportRepository.findTopByOrderByCreatedAtDesc(Limit.of(5));
        }

        final boolean calcDist = calculateDistance;
        return reports.stream().map(r -> {
            ReportResponse res = mapToResponse(r);
            if (calcDist) {
                res.setDistanceInMeters(
                        calculateHaversineDistance(userLat, userLong, r.getLatitude(), r.getLongitude()));
            }
            return res;
        }).collect(Collectors.toList());
    }

    private ReportResponse mapToResponse(Report report) {
        return ReportResponse.builder()
                .id(report.getId())
                .title(report.getTitle())
                .description(report.getDescription())
                .imageUrl(report.getImageUrl())
                .latitude(report.getLatitude())
                .longitude(report.getLongitude())
                .status(report.getStatus().name())
                .createdAt(report.getCreatedAt() != null ? report.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME)
                        : null)
                .build();
    }

    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // Radius of the earth in meters
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
