package com.batuhan.feedback360.model.response;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserPeriodReportResponse {
    private UserDetailResponse user;
    private EvaluationPeriodResponse period;
    private BigDecimal rawAverageScore;
    private BigDecimal competencyWeightedScore;
    private BigDecimal finalWeightedScore;
    private List<ScoreByEvaluatorResponse> scoresByEvaluator;
}
