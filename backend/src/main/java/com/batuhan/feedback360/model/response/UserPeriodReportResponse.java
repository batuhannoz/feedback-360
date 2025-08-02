package com.batuhan.feedback360.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserPeriodReportResponse {

    private UserDetailResponse user;
    private EvaluationPeriodResponse period;

    private String competencyDefinitionTitle;
    private String competencyDefinitionText;
    private String competencySourceWeightsTitle;
    private String competencySourceWeightsText;
    private String sourcesTitle;
    private String sourcesText;

    private OverallScores overallScores;

    private List<AverageScoreByEvaluatorTypeResponse> scoresByEvaluatorType;

    private List<CompetencyScoreDetailResponse> competencyScores;

    private List<CommentResponse> comments;
}
