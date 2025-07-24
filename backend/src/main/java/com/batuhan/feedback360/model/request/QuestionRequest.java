package com.batuhan.feedback360.model.request;

import java.util.Set;
import lombok.Data;

@Data
public class QuestionRequest {
    private String questionText;
    private Set<Integer> hiddenScores;
    private Set<Integer> scoresRequiringComment;
}
