package com.batuhan.feedback360.model.request;

import lombok.Data;

@Data
public class ShareReportRequest {
    private String customMessage;
    private boolean includeRawAverageScore;
    private boolean includeCompetencyWeightedScore;
    private boolean includeFinalWeightedScore;
    private boolean includeScoresByEvaluator;
    private boolean includeCompetencyScores;
    private boolean includeComments;
}