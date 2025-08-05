package com.batuhan.feedback360.model.response;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EvaluationScaleResponse {
    private Integer id;
    private String name;
    private List<ScaleOptionResponse> options;
}
