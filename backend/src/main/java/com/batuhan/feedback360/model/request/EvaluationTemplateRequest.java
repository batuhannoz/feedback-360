package com.batuhan.feedback360.model.request;

import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class EvaluationTemplateRequest {
    private String name;
    private String description;
    private Integer targetRoleId;
}
