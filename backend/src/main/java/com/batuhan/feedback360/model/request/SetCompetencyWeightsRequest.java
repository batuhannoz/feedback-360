package com.batuhan.feedback360.model.request;

import java.util.List;
import lombok.Data;

@Data
public class SetCompetencyWeightsRequest {
    private List<CompetencyWeightRequest> competencyWeights;
}
