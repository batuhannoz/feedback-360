package com.batuhan.feedback360.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QuestionResponse {
    private Integer id;
    private String questionText;
}
