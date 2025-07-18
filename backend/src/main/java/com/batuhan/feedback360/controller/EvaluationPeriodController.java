package com.batuhan.feedback360.controller;

import com.batuhan.feedback360.model.request.AssignCompetencyEvaluatorsRequest;
import com.batuhan.feedback360.model.request.AssignEvaluatorsRequest;
import com.batuhan.feedback360.model.request.CompetencyRequest;
import com.batuhan.feedback360.model.request.EvaluationPeriodRequest;
import com.batuhan.feedback360.model.request.EvaluatorRequest;
import com.batuhan.feedback360.model.request.QuestionRequest;
import com.batuhan.feedback360.model.request.SetCompetencyEvaluatorWeightsRequest;
import com.batuhan.feedback360.model.request.SetCompetencyWeightsRequest;
import com.batuhan.feedback360.model.response.ApiResponse;
import com.batuhan.feedback360.model.response.AssignmentResponse;
import com.batuhan.feedback360.model.response.CompetencyEvaluatorPermissionResponse;
import com.batuhan.feedback360.model.response.CompetencyResponse;
import com.batuhan.feedback360.model.response.CompetencyWeightResponse;
import com.batuhan.feedback360.model.response.EvaluatorResponse;
import com.batuhan.feedback360.model.response.ParticipantResponse;
import com.batuhan.feedback360.model.response.QuestionResponse;
import com.batuhan.feedback360.service.EvaluationPeriodService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ApiResponse<?>> updatePeriod(@PathVariable Integer periodId, @RequestBody EvaluationPeriodRequest request) {
        return ResponseEntity.ok(evaluationPeriodService.updateEvaluationPeriod(periodId, request));
    }

    // TODO @DeleteMapping("/{periodId}")

    // TODO @PutMapping("/{periodId}/status")

    @GetMapping("/{periodId}/evaluator")
    public ResponseEntity<ApiResponse<List<EvaluatorResponse>>> getEvaluators(@PathVariable Integer periodId) {
        return ResponseEntity.ok(evaluationPeriodService.getEvaluators(periodId));
    }

    @PostMapping("/{periodId}/evaluator")
    public ResponseEntity<ApiResponse<List<EvaluatorResponse>>> setEvaluators(
        @PathVariable Integer periodId,
        @RequestBody List<EvaluatorRequest> request
    ) {
        return ResponseEntity.ok(evaluationPeriodService.setEvaluators(periodId, request));
    }

    @GetMapping("/{periodId}/participant")
    public ResponseEntity<ApiResponse<List<ParticipantResponse>>> getParticipantsByPeriod(@PathVariable Integer periodId) {
        return ResponseEntity.ok(evaluationPeriodService.getParticipantsByPeriod(periodId));
    }

    @PostMapping("/{periodId}/participant/{userId}")
    public ResponseEntity<ApiResponse<ParticipantResponse>> addParticipantToPeriod(@PathVariable Integer periodId, @PathVariable Integer userId) {
        return ResponseEntity.ok(evaluationPeriodService.addParticipantToPeriod(periodId, userId));
    }

    @DeleteMapping("/{periodId}/participant/{userId}")
    public ResponseEntity<ApiResponse<?>> deleteParticipantToPeriod(@PathVariable Integer periodId, @PathVariable Integer userId) {
        return ResponseEntity.ok(evaluationPeriodService.deleteParticipantToPeriod(periodId, userId));
    }

    @GetMapping("/{periodId}/participant/{evaluatedUserId}/assignment")
    public ResponseEntity<ApiResponse<List<AssignmentResponse>>> getEvaluatorsForPeriodAssignment(
        @PathVariable Integer periodId,
        @PathVariable Integer evaluatedUserId
    ) {
        return ResponseEntity.ok(evaluationPeriodService.getEvaluatorsForPeriodAssignment(periodId, evaluatedUserId));
    }

    @PostMapping("/{periodId}/participant/{evaluatedId}/assignment")
    public ResponseEntity<ApiResponse<List<AssignmentResponse>>> assignEvaluatorsToPeriodParticipant(
        @PathVariable Integer periodId,
        @PathVariable Integer evaluatedId,
        @RequestBody AssignEvaluatorsRequest request
    ) {
        return ResponseEntity.ok(evaluationPeriodService.assignEvaluatorsToPeriodParticipant(periodId, evaluatedId, request));
    }

    @PostMapping("/{periodId}/competency")
    public ResponseEntity<ApiResponse<?>> addCompetencyToPeriod(@PathVariable Integer periodId, @RequestBody CompetencyRequest request) {
        return ResponseEntity.ok(evaluationPeriodService.addCompetencyToPeriod(periodId, request));
    }

    @GetMapping("/{periodId}/competency")
    public ResponseEntity<ApiResponse<List<CompetencyResponse>>> getCompetenciesByPeriod(@PathVariable Integer periodId) {
        return ResponseEntity.ok(evaluationPeriodService.getCompetenciesByPeriod(periodId));
    }

    @GetMapping("/{periodId}/competency/{competencyId}")
    public ResponseEntity<ApiResponse<CompetencyResponse>> getCompetencyById(@PathVariable Integer periodId, @PathVariable Integer competencyId) {
        return ResponseEntity.ok(evaluationPeriodService.getCompetencyById(periodId, competencyId));
    }

    // @DeleteMapping("/{periodId}/competency/{competencyId}")

    @GetMapping("/{periodId}/competency/{competencyId}/evaluator/permission")
    public ResponseEntity<ApiResponse<List<CompetencyEvaluatorPermissionResponse>>> getCompetencyEvaluatorPermissions(
        @PathVariable Integer periodId,
        @PathVariable Integer competencyId
    ) {
        return ResponseEntity.ok(evaluationPeriodService.getCompetencyEvaluatorPermissions(periodId, competencyId));
    }

    @PutMapping("/{periodId}/competency/{competencyId}/evaluator")
    public ResponseEntity<ApiResponse<List<CompetencyEvaluatorPermissionResponse>>> assignEvaluatorsToCompetency(
        @PathVariable Integer periodId,
        @PathVariable Integer competencyId,
        @RequestBody AssignCompetencyEvaluatorsRequest request
    ) {
        return ResponseEntity.ok(evaluationPeriodService.assignEvaluatorsToCompetency(periodId, competencyId, request));
    }

    @PutMapping("/{periodId}/competency/{competencyId}/evaluator/weight")
    public ResponseEntity<ApiResponse<List<CompetencyEvaluatorPermissionResponse>>> setCompetencyEvaluatorWeights(
        @PathVariable Integer periodId,
        @PathVariable Integer competencyId,
        @Valid @RequestBody SetCompetencyEvaluatorWeightsRequest request
    ) {
        return ResponseEntity.ok(evaluationPeriodService.setCompetencyEvaluatorWeights(periodId, competencyId, request));
    }

    @GetMapping("/{periodId}/competency/weight")
    public ResponseEntity<ApiResponse<?>> getCompetenciesWithWeights(@PathVariable Integer periodId) {
        return ResponseEntity.ok(evaluationPeriodService.getCompetenciesWithWeights(periodId));
    }

    @PutMapping("/{periodId}/competency/weight")
    public ResponseEntity<ApiResponse<List<CompetencyWeightResponse>>> setCompetencyWeights(
        @PathVariable Integer periodId,
        @Valid @RequestBody SetCompetencyWeightsRequest request
    ) {
        return ResponseEntity.ok(evaluationPeriodService.setCompetencyWeights(periodId, request));
    }

    @GetMapping("/{periodId}/competency/{competencyId}/question")
    public ResponseEntity<ApiResponse<List<QuestionResponse>>> getQuestionsForCompetency(
        @PathVariable Integer periodId,
        @PathVariable Integer competencyId
    ) {
        return ResponseEntity.ok(evaluationPeriodService.getQuestionsForCompetency(periodId, competencyId));
    }

    @PostMapping("/{periodId}/competency/{competencyId}/question")
    public ResponseEntity<ApiResponse<QuestionResponse>> addQuestionToCompetency(
        @PathVariable Integer periodId,
        @PathVariable Integer competencyId,
        @Valid @RequestBody QuestionRequest request
    ) {
        return ResponseEntity.ok(evaluationPeriodService.addQuestionToCompetency(periodId, competencyId, request));
    }

    @PutMapping("/{periodId}/competency/{competencyId}/question/{questionId}")
    public ResponseEntity<ApiResponse<QuestionResponse>> updateQuestion(
        @PathVariable Integer periodId,
        @PathVariable Integer competencyId,
        @PathVariable Integer questionId,
        @Valid @RequestBody QuestionRequest request
    ) {
        return ResponseEntity.ok(evaluationPeriodService.updateQuestion(periodId, competencyId, questionId, request));
    }

    @DeleteMapping("/{periodId}/competency/{competencyId}/question/{questionId}")
    public ResponseEntity<ApiResponse<Object>> deleteQuestion(@PathVariable Integer periodId, @PathVariable Integer competencyId, @PathVariable Integer questionId) {
        return ResponseEntity.ok(evaluationPeriodService.deleteQuestion(periodId, competencyId, questionId));
    }
}
