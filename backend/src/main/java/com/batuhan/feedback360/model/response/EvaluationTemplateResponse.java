package com.batuhan.feedback360.model.response;

import java.util.Set;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EvaluationTemplateResponse {
    private Integer id;
    private String name;
    private String description;
    private Integer targetRoleId;
    private Set<RoleResponse> evaluatorRoles;
    private Set<QuestionResponse> questions;
}
