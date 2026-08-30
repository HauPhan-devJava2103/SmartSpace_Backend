package com.vn.smart_space.service.report;

import com.vn.smart_space.dto.response.report.ReportResponse;
import java.util.List;

public interface IReportService {
    List<ReportResponse> getDangerousReports();
    List<ReportResponse> getRecentReports(String filter, Double userLat, Double userLong);
}
