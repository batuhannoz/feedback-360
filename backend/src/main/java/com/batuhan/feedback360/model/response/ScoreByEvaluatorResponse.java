package com.batuhan.feedback360.model.response;

import com.batuhan.feedback360.model.enums.EvaluatorType;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScoreByEvaluatorResponse {
    private String evaluatorName;
    private EvaluatorType evaluatorType;
    private BigDecimal averageScore;
}