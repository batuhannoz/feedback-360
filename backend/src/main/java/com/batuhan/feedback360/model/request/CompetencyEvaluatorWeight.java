package com.batuhan.feedback360.model.request;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class CompetencyEvaluatorWeight {
    @NotNull
    private Integer evaluatorId;

    @NotNull
    private BigDecimal weight;
}
