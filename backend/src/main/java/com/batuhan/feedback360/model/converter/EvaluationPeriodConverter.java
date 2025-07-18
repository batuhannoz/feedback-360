package com.batuhan.feedback360.model.converter;

import com.batuhan.feedback360.model.entitiy.EvaluationPeriod;
import com.batuhan.feedback360.model.response.EvaluationPeriodResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class EvaluationPeriodConverter {

    private final CompanyConverter companyConverter;

    public EvaluationPeriodResponse toEvaluationPeriodResponse(EvaluationPeriod evaluationPeriod) {
        if (evaluationPeriod == null) {
            return null;
        }

        return EvaluationPeriodResponse.builder()
            .id(evaluationPeriod.getId())
            .name(evaluationPeriod.getName())
            .startDate(evaluationPeriod.getStartDate())
            .endDate(evaluationPeriod.getEndDate())
            .status(evaluationPeriod.getStatus())
            .company(companyConverter.toCompanyResponse(evaluationPeriod.getCompany()))
            .build();
    }
}
