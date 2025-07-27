package com.batuhan.feedback360.model.response;

import java.util.Set;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QuestionResponse {
    private Integer id;
    private String questionText;
    private Set<Integer> hiddenScores;
    private Set<Integer> scoresRequiringComment;
}
