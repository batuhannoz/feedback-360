package com.batuhan.feedback360.controller;

import com.batuhan.feedback360.model.request.ShareReportRequest;
import com.batuhan.feedback360.model.response.ApiResponse;
import com.batuhan.feedback360.model.response.UserPeriodReportResponse;
import com.batuhan.feedback360.service.PeriodReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping("/user/{evaluatedUserId}")
    public ResponseEntity<ApiResponse<UserPeriodReportResponse>> getUserPeriodReport(
        @PathVariable Integer periodId,
        @PathVariable Integer evaluatedUserId
    ) {
        return ResponseEntity.ok(periodReportService.generateUserPeriodReport(periodId, evaluatedUserId));
    }

    @PostMapping("/user/{evaluatedUserId}/share")
    public ResponseEntity<ApiResponse<?>> shareUserPeriodReport(
        @PathVariable Integer periodId,
        @PathVariable Integer evaluatedUserId,
        @RequestBody ShareReportRequest request
    ) {
        return ResponseEntity.ok(periodReportService.shareUserPeriodReport(periodId, evaluatedUserId, request));
    }
}