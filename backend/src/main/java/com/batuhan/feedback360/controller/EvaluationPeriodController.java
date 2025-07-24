package com.batuhan.feedback360.controller;

import com.batuhan.feedback360.model.request.EvaluationPeriodRequest;
import com.batuhan.feedback360.model.request.UpdatePeriodStatusRequest;
import com.batuhan.feedback360.model.response.ApiResponse;
import com.batuhan.feedback360.model.response.EvaluationPeriodResponse;
import com.batuhan.feedback360.service.EvaluationPeriodService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/period")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class EvaluationPeriodController {

    private final EvaluationPeriodService evaluationPeriodService;

    @PostMapping
    public ResponseEntity<ApiResponse<EvaluationPeriodResponse>> createPeriod(@RequestBody EvaluationPeriodRequest request) {
        return ResponseEntity.ok(evaluationPeriodService.createEvaluationPeriod(request));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EvaluationPeriodResponse>>> getPeriods() {
        return ResponseEntity.ok(evaluationPeriodService.getEvaluationPeriods());
    }

    @GetMapping("/{periodId}")
    public ResponseEntity<ApiResponse<EvaluationPeriodResponse>> getPeriodById(@PathVariable Integer periodId) {
        return ResponseEntity.ok(evaluationPeriodService.getEvaluationPeriodById(periodId));
    }

    @PutMapping("/{periodId}")
    public ResponseEntity<ApiResponse<EvaluationPeriodResponse>> updatePeriod(
        @PathVariable Integer periodId,
        @RequestBody EvaluationPeriodRequest request
    ) {
        return ResponseEntity.ok(evaluationPeriodService.updateEvaluationPeriod(periodId, request));
    }

    @PostMapping("/{periodId}/status")
    public ResponseEntity<ApiResponse<?>> updatePeriodStatus(
        @PathVariable Integer periodId,
        @Valid @RequestBody UpdatePeriodStatusRequest request) {
        return ResponseEntity.ok(evaluationPeriodService.updatePeriodStatus(periodId, request));
    }

    @DeleteMapping("/{periodId}")
    public ResponseEntity<ApiResponse<?>> deletePeriod(@PathVariable Integer periodId) {
        return ResponseEntity.ok(evaluationPeriodService.deleteEvaluationPeriod(periodId));
    }
}