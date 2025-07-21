package com.batuhan.feedback360.controller;

import com.batuhan.feedback360.model.request.AssignEvaluatorsRequest;
import com.batuhan.feedback360.model.response.ApiResponse;
import com.batuhan.feedback360.model.response.AssignmentResponse;
import com.batuhan.feedback360.service.ParticipantAssignmentService;
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
@RequestMapping("/api/v1/period/{periodId}/participant/{evaluatedUserId}/assignment")
@RequiredArgsConstructor
public class ParticipantAssignmentController {

    private final ParticipantAssignmentService participantAssignmentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AssignmentResponse>>> getEvaluatorsForPeriodAssignment(
        @PathVariable Integer periodId,
        @PathVariable Integer evaluatedUserId
    ) {
        return ResponseEntity.ok(participantAssignmentService.getEvaluatorsForPeriodAssignment(periodId, evaluatedUserId));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<List<AssignmentResponse>>> assignEvaluatorsToPeriodParticipant(
        @PathVariable Integer periodId,
        @PathVariable Integer evaluatedUserId,
        @RequestBody AssignEvaluatorsRequest request
    ) {
        return ResponseEntity.ok(participantAssignmentService.assignEvaluatorsToPeriodParticipant(periodId, evaluatedUserId, request));
    }
}