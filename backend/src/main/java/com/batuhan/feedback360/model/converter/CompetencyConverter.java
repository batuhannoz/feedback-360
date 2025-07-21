package com.batuhan.feedback360.model.converter;

import com.batuhan.feedback360.model.entitiy.Competency;
import com.batuhan.feedback360.model.response.CompetencyResponse;
import java.util.Collections;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CompetencyConverter {

    private final QuestionConverter questionConverter;

    public CompetencyResponse toCompetencyResponse(Competency competency) {
        if (competency == null) {
            return null;
        }

        return CompetencyResponse.builder()
            .id(competency.getId())
            .title(competency.getTitle())
            .description(competency.getDescription())
            .questions(Optional.ofNullable(competency.getQuestions())
                .orElseGet(Collections::emptySet)
                .stream()
                .map(questionConverter::toQuestionResponse)
                .toList())
            .createdAt(competency.getCreatedAt())
            .build();
    }
}