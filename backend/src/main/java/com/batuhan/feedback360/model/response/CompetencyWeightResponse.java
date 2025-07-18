package com.batuhan.feedback360.model.response;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompetencyWeightResponse {
    private Integer competencyId;
    private String competencyTitle;
    private BigDecimal weight;
}
