package com.batuhan.feedback360.model.response;


import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionScoreDetailResponse {
    private Integer questionId;
    private String questionText;
    private List<ScoreByEvaluatorTypeResponse> scoresByEvaluatorType;
}