package com.batuhan.feedback360.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentResponse {
    private Integer id;
    private ParticipantResponse evaluatorUser;
    private ParticipantResponse evaluatedUser;
    private EvaluatorResponse evaluator;
}