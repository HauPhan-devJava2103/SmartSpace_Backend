package com.vn.smart_space.repository;

import java.util.List;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vn.smart_space.consts.EReportSeverity;
import com.vn.smart_space.model.Report;

public interface ReportRepository extends JpaRepository<Report, String> {

        @Query("SELECT r FROM Report r WHERE r.severity IN :severities ORDER BY r.createdAt DESC")
        List<Report> findTopBySeverityInOrderByCreatedAtDesc(@Param("severities") List<EReportSeverity> severities,
                        Limit limit);

        @Query("SELECT r FROM Report r ORDER BY r.createdAt DESC")
        List<Report> findTopByOrderByCreatedAtDesc(Limit limit);

        @Query(value = "SELECT * FROM reports r ORDER BY (6371000 * acos(cos(radians(:userLat)) * cos(radians(r.latitude)) * cos(radians(r.longitude) - radians(:userLong)) + sin(radians(:userLat)) * sin(radians(r.latitude)))) ASC", nativeQuery = true)
        List<Report> findNearestReports(@Param("userLat") Double userLat, @Param("userLong") Double userLong,
                        Limit limit);
}
