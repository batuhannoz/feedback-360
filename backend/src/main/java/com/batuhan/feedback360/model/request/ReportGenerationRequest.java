package com.batuhan.feedback360.model.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReportGenerationRequest {
    private Integer periodId;
    private Integer evaluatedUserId;
    private ReportDisplaySettings settings;
}