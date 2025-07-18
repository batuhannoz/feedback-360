package com.batuhan.feedback360.model.request;

import java.util.List;
import lombok.Data;

@Data
public class AssignCompetencyEvaluatorsRequest {
    private List<Integer> evaluatorIds;
}