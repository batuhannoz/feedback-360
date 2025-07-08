package com.batuhan.feedback360.model.response;

import com.batuhan.feedback360.model.enums.EvaluationStatus;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EvaluationTaskResponse {
    private Integer evaluationId;
    private EmployeeSimpleResponse evaluatedEmployee;
    private EvaluationStatus status;
    private LocalDateTime submissionDate;
}
