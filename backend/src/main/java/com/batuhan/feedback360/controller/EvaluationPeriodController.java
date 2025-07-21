package com.batuhan.feedback360.controller;

import com.batuhan.feedback360.model.request.EvaluationPeriodRequest;
import com.batuhan.feedback360.model.request.UpdatePeriodStatusRequest;
import com.batuhan.feedback360.model.response.ApiResponse;
import com.batuhan.feedback360.service.EvaluationPeriodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
public class EvaluationPeriodController {

    private final EvaluationPeriodService evaluationPeriodService;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createPeriod(@RequestBody EvaluationPeriodRequest request) {
        return ResponseEntity.ok(evaluationPeriodService.createEvaluationPeriod(request));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getPeriods() {
        return ResponseEntity.ok(evaluationPeriodService.getEvaluationPeriods());
    }

    @GetMapping("/{periodId}")
    public ResponseEntity<ApiResponse<?>> getPeriodById(@PathVariable Integer periodId) {
        return ResponseEntity.ok(evaluationPeriodService.getEvaluationPeriodById(periodId));
    }

    @PutMapping("/{periodId}")
    public ResponseEntity<ApiResponse<?>> updatePeriod(
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
}