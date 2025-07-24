package com.batuhan.feedback360.controller;


import com.batuhan.feedback360.model.request.EvaluatorRequest;
import com.batuhan.feedback360.model.response.ApiResponse;
import com.batuhan.feedback360.model.response.EvaluatorResponse;
import com.batuhan.feedback360.service.PeriodEvaluatorService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/period/{periodId}/evaluator")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class PeriodEvaluatorController {

    private final PeriodEvaluatorService periodEvaluatorService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<EvaluatorResponse>>> getEvaluators(@PathVariable Integer periodId) {
        return ResponseEntity.ok(periodEvaluatorService.getEvaluators(periodId));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<List<EvaluatorResponse>>> setEvaluators(
        @PathVariable Integer periodId,
        @Valid @RequestBody List<EvaluatorRequest> request
    ) {
        return ResponseEntity.ok(periodEvaluatorService.setEvaluators(periodId, request));
    }
}
