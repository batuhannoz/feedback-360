package com.batuhan.feedback360.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScoresByEachEvaluatorType {
    private BigDecimal selfScore;
    private BigDecimal peerScore;
    private BigDecimal managerScore;
    private BigDecimal subordinateScore;
    private BigDecimal otherScore;
    private BigDecimal averageScore;
}
