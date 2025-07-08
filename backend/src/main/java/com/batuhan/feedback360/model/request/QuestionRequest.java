package com.batuhan.feedback360.model.request;

import com.batuhan.feedback360.model.enums.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class QuestionRequest {
    private String question;
    private QuestionType type;
}
