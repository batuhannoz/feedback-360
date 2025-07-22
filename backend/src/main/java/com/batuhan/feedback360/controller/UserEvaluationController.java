package com.batuhan.feedback360.controller;

import com.batuhan.feedback360.model.request.AnswerSubmissionRequest;
import com.batuhan.feedback360.model.response.AnswerResponse;
import com.batuhan.feedback360.model.response.ApiResponse;
import com.batuhan.feedback360.model.response.EvaluationPeriodResponse;
import com.batuhan.feedback360.model.response.QuestionResponse;
import com.batuhan.feedback360.model.response.UserDetailResponse;
import com.batuhan.feedback360.service.UserEvaluationService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
public class UserEvaluationController {

    private final UserEvaluationService userEvaluationService;

    @GetMapping("/period")
    public ResponseEntity<ApiResponse<List<EvaluationPeriodResponse>>> getMyEvaluationPeriods() {
        return ResponseEntity.ok(userEvaluationService.getEvaluationPeriodsForUser());
    }

    @GetMapping("/period/{periodId}/evaluation")
    public ResponseEntity<ApiResponse<List<UserDetailResponse>>> getMyEvaluationsForPeriod(@PathVariable Integer periodId) {
        return ResponseEntity.ok(userEvaluationService.getEvaluationsForUserPeriod(periodId));
    }

    @GetMapping("/period/{periodId}/evaluation/{evaluatedUserId}")
    public ResponseEntity<ApiResponse<List<QuestionResponse>>> getQuestionsForEvaluation(
        @PathVariable Integer periodId,
        @PathVariable Integer evaluatedUserId
    ) {
        return ResponseEntity.ok(userEvaluationService.getQuestionsForEvaluatedUser(periodId, evaluatedUserId));
    }

    @PostMapping("/period/{periodId}/evaluation/{evaluatedUserId}")
    public ResponseEntity<ApiResponse<List<AnswerResponse>>> submitAnswers(
        @PathVariable Integer periodId,
        @PathVariable Integer evaluatedUserId,
        @Valid @RequestBody List<AnswerSubmissionRequest> request
    ) {
        return ResponseEntity.ok(userEvaluationService.submitAnswersForEvaluatedUser(periodId, evaluatedUserId, request));
    }
}
