package com.batuhan.feedback360.model.converter;

import com.batuhan.feedback360.model.entitiy.EvaluationTemplate;
import com.batuhan.feedback360.model.response.EvaluationTemplateResponse;
import com.batuhan.feedback360.model.response.QuestionResponse;
import com.batuhan.feedback360.model.response.RoleResponse;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EvaluationTemplateConverter {

    private final QuestionConverter questionConverter;
    private final RoleConverter roleConverter;

    public EvaluationTemplateResponse toTemplateResponse(EvaluationTemplate template) {
        if (template == null) {
            return null;
        }

        Set<RoleResponse> evaluatorRoles = template.getEvaluatorRoles() == null
            ? Collections.emptySet()
            : template.getEvaluatorRoles().stream()
                .map(roleConverter::toRoleResponse)
                .collect(Collectors.toSet());

        Set<QuestionResponse> questions = template.getQuestions() == null
            ? Collections.emptySet()
            : template.getQuestions().stream()
                .map(questionConverter::toQuestionResponse)
                .collect(Collectors.toSet());

        return EvaluationTemplateResponse.builder()
            .id(template.getId())
            .name(template.getName())
            .description(template.getDescription())
            .targetRoleId(template.getTargetRole().getId())
            .evaluatorRoles(evaluatorRoles)
            .questions(questions)
            .build();
    }
}
