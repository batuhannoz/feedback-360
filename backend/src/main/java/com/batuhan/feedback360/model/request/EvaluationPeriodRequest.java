package com.batuhan.feedback360.model.request;

import com.batuhan.feedback360.model.enums.PeriodStatus;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class EvaluationPeriodRequest {
    private String periodName;
    private String internalPeriodName;
    private String evaluationName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private PeriodStatus status;
}
