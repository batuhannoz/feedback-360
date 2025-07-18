package com.batuhan.feedback360.model.response;

import com.batuhan.feedback360.model.enums.PeriodStatus;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EvaluationPeriodResponse {
    private Integer id;
    private String name;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private PeriodStatus status;
    private CompanyResponse company;
}
