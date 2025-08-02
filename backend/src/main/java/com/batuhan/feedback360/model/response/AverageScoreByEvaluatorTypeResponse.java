package com.batuhan.feedback360.model.response;

import com.batuhan.feedback360.model.enums.EvaluatorType;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AverageScoreByEvaluatorTypeResponse {
    private EvaluatorType evaluatorType;
    private String evaluatorName;
    private BigDecimal averageScore;
}