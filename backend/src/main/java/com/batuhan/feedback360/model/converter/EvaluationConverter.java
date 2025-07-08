package com.batuhan.feedback360.model.converter;

import com.batuhan.feedback360.model.entitiy.Evaluation;
import com.batuhan.feedback360.model.response.AnswerDetailResponse;
import com.batuhan.feedback360.model.response.EmployeeSimpleResponse;
import com.batuhan.feedback360.model.response.EvaluationDetailResponse;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class EvaluationConverter {

    public EvaluationDetailResponse ToDetailResponse(Evaluation evaluation) {
        EmployeeSimpleResponse evaluatedResponse = EmployeeSimpleResponse.builder()
            .id(evaluation.getEvaluated().getId())
            .fullName(evaluation.getEvaluated().getFullName())
            .build();

        EmployeeSimpleResponse evaluatorResponse = EmployeeSimpleResponse.builder()
            .id(evaluation.getEvaluator().getId())
            .fullName(evaluation.getEvaluator().getFullName())
            .build();

        List<AnswerDetailResponse> answerDtos = evaluation.getAnswers().stream()
            .map(answer -> AnswerDetailResponse.builder()
                .answerId(answer.getId())
                .questionText(answer.getQuestion().getQuestion())
                .type(answer.getQuestion().getType())
                .currentValue(answer.getAnswer())
                .build())
            .collect(Collectors.toList());

        return EvaluationDetailResponse.builder()
            .evaluationId(evaluation.getId())
            .status(evaluation.getStatus())
            .evaluator(evaluatorResponse)
            .evaluated(evaluatedResponse)
            .answers(answerDtos)
            .build();
    }
}
