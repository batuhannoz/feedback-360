package com.batuhan.feedback360.model.converter;

import com.batuhan.feedback360.model.entitiy.EvaluationAssignment;
import com.batuhan.feedback360.model.response.AssignmentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AssignmentConverter {

    private final UserConverter userConverter;
    private final EvaluatorConverter evaluatorConverter;

    public AssignmentResponse toAssignmentResponse(EvaluationAssignment assignment) {
        if (assignment == null) {
            return null;
        }

        return AssignmentResponse.builder()
            .id(assignment.getId())
            .evaluatorUser(userConverter.toParticipantResponse(assignment.getEvaluatorUser()))
            .evaluatedUser(userConverter.toParticipantResponse(assignment.getPeriodParticipant().getEvaluatedUser()))
            .evaluator(evaluatorConverter.toEvaluatorResponse(assignment.getEvaluator()))
            .build();
    }
}