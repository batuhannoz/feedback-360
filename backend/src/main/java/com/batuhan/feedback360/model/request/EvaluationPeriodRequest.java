package com.batuhan.feedback360.model.request;

import java.time.LocalDate;
import java.util.List;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class EvaluationPeriodRequest {
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<Integer> templateIds;
}
