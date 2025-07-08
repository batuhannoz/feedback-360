package com.batuhan.feedback360.model.response;

import com.batuhan.feedback360.model.enums.EvaluationStatus;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EvaluationDetailResponse {
    private Integer evaluationId;
    private EvaluationStatus status;
    private EmployeeSimpleResponse evaluatedEmployee;
    private List<AnswerDetailResponse> answers;
}

