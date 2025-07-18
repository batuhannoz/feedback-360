package com.batuhan.feedback360.model.response;

import com.batuhan.feedback360.model.enums.EvaluatorType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EvaluatorResponse {
    private Integer id;
    private EvaluationPeriodResponse period;
    private EvaluatorType evaluatorType;
    private String name;
}
