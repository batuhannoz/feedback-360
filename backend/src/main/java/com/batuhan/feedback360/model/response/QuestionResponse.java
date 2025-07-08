package com.batuhan.feedback360.model.response;

import com.batuhan.feedback360.model.enums.QuestionType;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@Builder
public class QuestionResponse {
    private Integer id;
    private String question;
    private QuestionType type;
}
