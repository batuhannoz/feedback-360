package com.batuhan.feedback360.controller;

import com.batuhan.feedback360.model.request.EvaluationPeriodRequest;
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
@RequestMapping("/api/v1/evaluation/period")
public class EvaluationPeriodController {

    private final EvaluationPeriodService evaluationPeriodService;

    @PostMapping
    @PreAuthorize("hasAnyRole('COMPANY', 'ADMIN')")
    public ResponseEntity<EvaluationPeriodResponse> createEvaluationPeriod(@RequestBody EvaluationPeriodRequest request) {
        EvaluationPeriodResponse createdPeriod = evaluationPeriodService.createPeriod(request);
        return new ResponseEntity<>(createdPeriod, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('COMPANY', 'ADMIN')")
    public ResponseEntity<List<EvaluationPeriodResponse>> getAllPeriods() {
        List<EvaluationPeriodResponse> periods = evaluationPeriodService.findAllPeriodsByCompany();
        return ResponseEntity.ok(periods);
    }

    @GetMapping("/{periodId}")
    @PreAuthorize("hasAnyRole('COMPANY', 'ADMIN')")
    public ResponseEntity<EvaluationPeriodResponse> getPeriodById(@PathVariable Integer periodId) {
        EvaluationPeriodResponse period = evaluationPeriodService.findPeriodById(periodId);
        return ResponseEntity.ok(period);
    }

    @PutMapping("/{periodId}")
    @PreAuthorize("hasAnyRole('COMPANY', 'ADMIN')")
    public ResponseEntity<EvaluationPeriodResponse> updatePeriod(@PathVariable Integer periodId, @RequestBody EvaluationPeriodRequest request) {
        EvaluationPeriodResponse updatedPeriod = evaluationPeriodService.updatePeriod(periodId, request);
        return ResponseEntity.ok(updatedPeriod);
    }

    @DeleteMapping("/{periodId}")
    @PreAuthorize("hasAnyRole('COMPANY', 'ADMIN')")
    public ResponseEntity<Void> deletePeriod(@PathVariable Integer periodId) {
        evaluationPeriodService.deletePeriod(periodId);
        return ResponseEntity.noContent().build();
    }
}
