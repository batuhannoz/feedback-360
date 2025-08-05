package com.batuhan.feedback360.model.request;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionRequest {
    private String questionText;
    private Set<Integer> hiddenScores;
    private Set<Integer> scoresRequiringComment;
    private Integer evaluationScaleId;
}
