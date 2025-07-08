package com.batuhan.feedback360.model.converter;

import com.batuhan.feedback360.model.entitiy.Question;
import com.batuhan.feedback360.model.response.QuestionResponse;
import org.springframework.stereotype.Component;

@Component
public class QuestionConverter {

    public QuestionResponse toQuestionResponse(Question question) {
        if (question == null) {
            return null;
        }

        return QuestionResponse.builder()
            .id(question.getId())
            .question(question.getQuestion())
            .type(question.getType())
            .build();
    }
}
