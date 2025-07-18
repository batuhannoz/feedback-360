package com.batuhan.feedback360.model.converter;

import com.batuhan.feedback360.model.entitiy.Evaluator;
import com.batuhan.feedback360.model.response.EvaluatorResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class EvaluatorConverter {

    private final EvaluationPeriodConverter evaluationPeriodConverter;

    public EvaluatorResponse toEvaluatorResponse(Evaluator evaluator) {
        if (evaluator == null) {
            return null;
        }

        return EvaluatorResponse.builder()
            .id(evaluator.getId())
            .name(evaluator.getName())
            .period(evaluationPeriodConverter.toEvaluationPeriodResponse(evaluator.getPeriod()))
            .evaluatorType(evaluator.getEvaluatorType())
            .build();
    }
}
