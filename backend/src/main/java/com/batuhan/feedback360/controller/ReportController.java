package com.batuhan.feedback360.controller;

import com.batuhan.feedback360.model.request.ReportDisplaySettings;
import com.batuhan.feedback360.model.request.ReportGenerationRequest;
import com.batuhan.feedback360.model.response.ApiResponse;
import com.batuhan.feedback360.model.response.UserPeriodReportResponse;
import com.batuhan.feedback360.service.PeriodReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/period/{periodId}/report")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    private final PeriodReportService periodReportService;

    @PostMapping("/user/{evaluatedUserId}")
    public ResponseEntity<ApiResponse<UserPeriodReportResponse>> getUserPeriodReport(
        @PathVariable Integer periodId,
        @PathVariable Integer evaluatedUserId,
        @RequestBody(required = false) ReportDisplaySettings settings
    ) {
        ReportGenerationRequest request = ReportGenerationRequest.builder()
            .periodId(periodId)
            .evaluatedUserId(evaluatedUserId)
            .settings(settings)
            .build();
        return ResponseEntity.ok(periodReportService.generateUserPeriodReport(request));
    }
}