package com.batuhan.feedback360.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompetencyScoreDetailResponse {
    private Integer competencyId;
    private String competencyTitle;
    private BigDecimal rawAverageScore;
    private BigDecimal weightedScore;
    private List<QuestionScoreDetailResponse> questionScores;
}
