package com.batuhan.feedback360.model.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AnswerResponse {
    private Integer id;
    private Integer questionId;
    private String questionText;
    private int score;
    private String answerText;
    private LocalDateTime submittedAt;
}
