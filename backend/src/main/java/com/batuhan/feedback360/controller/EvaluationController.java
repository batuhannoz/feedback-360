package com.batuhan.feedback360.controller;

import com.batuhan.feedback360.model.request.SubmitAnswersRequest;
import com.batuhan.feedback360.model.response.EvaluationDetailResponse;
import com.batuhan.feedback360.model.response.GivenEvaluationResponse;
import com.batuhan.feedback360.model.response.ParticipantCompletionStatusResponse;
import com.batuhan.feedback360.model.response.ReceivedEvaluationResponse;
import com.batuhan.feedback360.service.EvaluationService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/evaluation")
public class EvaluationController {

    private final EvaluationService evaluationService;

    @GetMapping("/periods/{periodId}/participants")
    @PreAuthorize("hasAnyRole('COMPANY', 'ADMIN')")
    public ResponseEntity<List<ParticipantCompletionStatusResponse>> getParticipantStatus(@PathVariable Integer periodId) {
        return ResponseEntity.ok(evaluationService.getParticipantCompletionStatus(periodId));
    }

    @GetMapping("/periods/{periodId}/user/{userId}/received")
    @PreAuthorize("hasAnyRole('COMPANY', 'ADMIN')")
    public ResponseEntity<List<ReceivedEvaluationResponse>> getReceivedEvaluations(@PathVariable Integer periodId, @PathVariable Integer userId) {
        return ResponseEntity.ok(evaluationService.getReceivedEvaluationsForUser(periodId, userId));
    }

    @GetMapping("/periods/{periodId}/user/{userId}/given")
    @PreAuthorize("hasAnyRole('COMPANY', 'ADMIN')")
    public ResponseEntity<List<GivenEvaluationResponse>> getGivenEvaluations(@PathVariable Integer periodId, @PathVariable Integer userId) {
        return ResponseEntity.ok(evaluationService.getGivenEvaluationsForUser(periodId, userId));
    }

    @GetMapping("/{evaluationId}/details")
    @PreAuthorize("hasAnyRole('COMPANY', 'ADMIN')")
    public ResponseEntity<EvaluationDetailResponse> getEvaluationDetailsForAdmin(@PathVariable Integer evaluationId) {
        return ResponseEntity.ok(evaluationService.getEvaluationDetailsForAdmin(evaluationId));
    }
}
