package com.batuhan.feedback360.model.response;
import com.batuhan.feedback360.model.enums.EvaluatorType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoreByEvaluatorTypeResponse {
    private EvaluatorType evaluatorType;
    private Integer score;
}
