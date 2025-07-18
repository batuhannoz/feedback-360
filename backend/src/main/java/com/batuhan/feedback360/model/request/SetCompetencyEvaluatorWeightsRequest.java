package com.batuhan.feedback360.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

@Data
public class SetCompetencyEvaluatorWeightsRequest {
    @NotEmpty
    @Valid
    private List<CompetencyEvaluatorWeight> weights;
}