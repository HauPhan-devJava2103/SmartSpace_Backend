package com.vn.smart_space.repository;

import com.vn.smart_space.model.Report;
import com.vn.smart_space.model.ReportSeverity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, String> {

    @Query("SELECT r FROM Report r WHERE r.severity IN :severities ORDER BY r.createdAt DESC LIMIT :limit")
    List<Report> findTopBySeverityInOrderByCreatedAtDesc(@Param("severities") List<ReportSeverity> severities, @Param("limit") int limit);

    @Query("SELECT r FROM Report r ORDER BY r.createdAt DESC LIMIT :limit")
    List<Report> findTopByOrderByCreatedAtDesc(@Param("limit") int limit);

    @Query(value = "SELECT * FROM reports r ORDER BY (6371000 * acos(cos(radians(:userLat)) * cos(radians(r.latitude)) * cos(radians(r.longitude) - radians(:userLong)) + sin(radians(:userLat)) * sin(radians(r.latitude)))) ASC LIMIT :limit", nativeQuery = true)
    List<Report> findNearestReports(@Param("userLat") Double userLat, @Param("userLong") Double userLong, @Param("limit") int limit);
}
