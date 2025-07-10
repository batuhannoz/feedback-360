package com.batuhan.feedback360.controller;

import com.batuhan.feedback360.model.request.EvaluationPeriodRequest;
import com.batuhan.feedback360.model.response.ApiResponse;
import com.batuhan.feedback360.model.response.EvaluationPeriodResponse;
import com.batuhan.feedback360.service.EvaluationPeriodService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
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
@AllArgsConstructor
@PreAuthorize("hasAnyRole('COMPANY', 'ADMIN')")
@RequestMapping("/api/v1/evaluation/period")
public class EvaluationPeriodController {

    private final EvaluationPeriodService evaluationPeriodService;

    @PostMapping
    public ResponseEntity<ApiResponse<EvaluationPeriodResponse>> createEvaluationPeriod(@RequestBody EvaluationPeriodRequest request) {
        return new ResponseEntity<>(evaluationPeriodService.createPeriod(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EvaluationPeriodResponse>>> getAllPeriods() {
        return ResponseEntity.ok(evaluationPeriodService.findAllPeriodsByCompany());
    }

    @GetMapping("/{periodId}")
    public ResponseEntity<ApiResponse<EvaluationPeriodResponse>> getPeriodById(@PathVariable Integer periodId) {
        return ResponseEntity.ok(evaluationPeriodService.findPeriodById(periodId));
    }

    @PutMapping("/{periodId}")
    public ResponseEntity<ApiResponse<EvaluationPeriodResponse>> updatePeriod(@PathVariable Integer periodId, @RequestBody EvaluationPeriodRequest request) {
        return ResponseEntity.ok(evaluationPeriodService.updatePeriod(periodId, request));
    }

    @DeleteMapping("/{periodId}")
    public ResponseEntity<ApiResponse<Void>> deletePeriod(@PathVariable Integer periodId) {
        return ResponseEntity.ok(evaluationPeriodService.deletePeriod(periodId));
    }
}
