package com.batuhan.feedback360.model.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class CompetencyWeightRequest {
    @NotNull(message = "Competency ID cannot be null.")
    private Integer competencyId;

    @NotNull(message = "Weight cannot be null.")
    @PositiveOrZero(message = "Weight must be a positive value or zero.")
    private BigDecimal weight;
}