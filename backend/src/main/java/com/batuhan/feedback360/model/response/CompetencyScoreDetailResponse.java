package com.batuhan.feedback360.model.response;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompetencyScoreDetailResponse {
    private Integer competencyId;
    private String competencyTitle;
    private BigDecimal rawAverageScore;
    private BigDecimal finalWeightedScore;
    private List<String> comments;
    private List<QuestionScoreDetailResponse> questionScores;
}
