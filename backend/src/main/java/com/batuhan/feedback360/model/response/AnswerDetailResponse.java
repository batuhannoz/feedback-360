package com.batuhan.feedback360.model.response;

import com.batuhan.feedback360.model.enums.QuestionType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AnswerDetailResponse {
    private Integer answerId;
    private String questionText;
    private QuestionType type;
    private String currentValue;
}
