package com.batuhan.feedback360.model.converter;

import com.batuhan.feedback360.model.entitiy.Question;
import com.batuhan.feedback360.model.response.QuestionResponse;
import com.batuhan.feedback360.model.response.ScaleOptionResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class QuestionConverter {

    public QuestionResponse toQuestionResponse(Question question) {
        if (question == null) {
            return null;
        }

        List<ScaleOptionResponse> options = new ArrayList<>();
        if (question.getEvaluationScale() != null && question.getEvaluationScale().getOptions() != null) {
            options = question.getEvaluationScale().getOptions().stream()
                .map(option -> new ScaleOptionResponse(option.getScore(), option.getLabel()))
                .sorted(Comparator.comparingInt(ScaleOptionResponse::getScore))
                .toList();
        }

        return QuestionResponse.builder()
            .id(question.getId())
            .questionText(question.getQuestionText())
            .hiddenScores(question.getHiddenScores())
            .scoresRequiringComment(question.getScoresRequiringComment())
            .scaleOptions(options)
            .build();
    }
}