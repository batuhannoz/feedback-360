package com.batuhan.feedback360.model.converter;

import com.batuhan.feedback360.model.entitiy.Answer;
import com.batuhan.feedback360.model.response.AnswerResponse;
import org.springframework.stereotype.Component;

@Component
public class AnswerConverter {

    public AnswerResponse toAnswerResponse(Answer answer) {
        if (answer == null) {
            return null;
        }
        return AnswerResponse.builder()
            .id(answer.getId())
            .questionId(answer.getQuestion().getId())
            .questionText(answer.getQuestion().getQuestionText())
            .score(answer.getScore())
            .answerText(answer.getAnswerText())
            .submittedAt(answer.getSubmittedAt())
            .build();
    }
}