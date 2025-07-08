package com.batuhan.feedback360.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ParticipantCompletionStatusResponse {
    private EmployeeSimpleResponse participant;
    private int totalEvaluationsAssigned;
    private int completedEvaluations;
    private double completionPercentage;
}