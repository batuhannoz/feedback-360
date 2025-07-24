package com.batuhan.feedback360.controller;

import com.batuhan.feedback360.model.request.AssignCompetencyEvaluatorsRequest;
import com.batuhan.feedback360.model.request.CompetencyRequest;
import com.batuhan.feedback360.model.request.SetCompetencyEvaluatorWeightsRequest;
import com.batuhan.feedback360.model.request.SetCompetencyWeightsRequest;
import com.batuhan.feedback360.model.response.ApiResponse;
import com.batuhan.feedback360.model.response.CompetencyEvaluatorPermissionResponse;
import com.batuhan.feedback360.model.response.CompetencyResponse;
import com.batuhan.feedback360.model.response.CompetencyWeightResponse;
import com.batuhan.feedback360.service.PeriodCompetencyService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/period/{periodId}/competency")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class PeriodCompetencyController {

    private final PeriodCompetencyService periodCompetencyService;

    @PostMapping
    public ResponseEntity<ApiResponse<CompetencyResponse>> addCompetencyToPeriod(
        @PathVariable Integer periodId,
        @RequestBody CompetencyRequest request
    ) {
        return ResponseEntity.ok(periodCompetencyService.addCompetencyToPeriod(periodId, request));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CompetencyResponse>>> getCompetenciesByPeriod(@PathVariable Integer periodId) {
        return ResponseEntity.ok(periodCompetencyService.getCompetenciesByPeriod(periodId));
    }

    @GetMapping("/{competencyId}")
    public ResponseEntity<ApiResponse<CompetencyResponse>> getCompetencyById(
        @PathVariable Integer periodId,
        @PathVariable Integer competencyId
    ) {
        return ResponseEntity.ok(periodCompetencyService.getCompetencyById(periodId, competencyId));
    }

    @DeleteMapping("/{competencyId}")
    public ResponseEntity<ApiResponse<?>> deleteCompetencyFromPeriod(
        @PathVariable Integer periodId,
        @PathVariable Integer competencyId
    ) {
        return ResponseEntity.ok(periodCompetencyService.deleteCompetencyFromPeriod(periodId, competencyId));
    }

    @PostMapping("/{competencyId}/evaluator")
    public ResponseEntity<ApiResponse<List<CompetencyEvaluatorPermissionResponse>>> assignEvaluatorsToCompetency(
        @PathVariable Integer periodId,
        @PathVariable Integer competencyId,
        @RequestBody AssignCompetencyEvaluatorsRequest request
    ) {
        return ResponseEntity.ok(periodCompetencyService.assignEvaluatorsToCompetency(periodId, competencyId, request));
    }

    @GetMapping("/{competencyId}/evaluator")
    public ResponseEntity<ApiResponse<List<CompetencyEvaluatorPermissionResponse>>> getCompetencyEvaluatorPermissions(
        @PathVariable Integer periodId,
        @PathVariable Integer competencyId
    ) {
        return ResponseEntity.ok(periodCompetencyService.getCompetencyEvaluatorPermissions(periodId, competencyId));
    }

    @PostMapping("/{competencyId}/evaluator/weight")
    public ResponseEntity<ApiResponse<List<CompetencyEvaluatorPermissionResponse>>> setCompetencyEvaluatorWeights(
        @PathVariable Integer periodId,
        @PathVariable Integer competencyId,
        @Valid @RequestBody SetCompetencyEvaluatorWeightsRequest request
    ) {
        return ResponseEntity.ok(periodCompetencyService.setCompetencyEvaluatorWeights(periodId, competencyId, request));
    }

    @GetMapping("/weight")
    public ResponseEntity<ApiResponse<List<CompetencyWeightResponse>>> getCompetencyWeights(@PathVariable Integer periodId) {
        return ResponseEntity.ok(periodCompetencyService.getCompetenciesWithWeights(periodId));
    }

    @PostMapping("/weight")
    public ResponseEntity<ApiResponse<List<CompetencyWeightResponse>>> setCompetencyWeights(
        @PathVariable Integer periodId,
        @Valid @RequestBody SetCompetencyWeightsRequest request
    ) {
        return ResponseEntity.ok(periodCompetencyService.setCompetencyWeights(periodId, request));
    }
}
