package com.batuhan.feedback360.model.response;

import com.batuhan.feedback360.model.enums.EvaluatorType;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompetencyEvaluatorPermissionResponse {
    private Long id;
    private Integer evaluatorId;
    private String evaluatorName;
    private EvaluatorType evaluatorType;
    private BigDecimal weight;
}