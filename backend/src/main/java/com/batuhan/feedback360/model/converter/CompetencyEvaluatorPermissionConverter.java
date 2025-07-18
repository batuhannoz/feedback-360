package com.batuhan.feedback360.model.converter;

import com.batuhan.feedback360.model.entitiy.CompetencyEvaluatorPermission;
import com.batuhan.feedback360.model.response.CompetencyEvaluatorPermissionResponse;
import org.springframework.stereotype.Component;

@Component
public class CompetencyEvaluatorPermissionConverter {

    public CompetencyEvaluatorPermissionResponse toResponse(CompetencyEvaluatorPermission permission) {
        if (permission == null) {
            return null;
        }
        return CompetencyEvaluatorPermissionResponse.builder()
            .id(permission.getId())
            .evaluatorId(permission.getEvaluator().getId())
            .evaluatorName(permission.getEvaluator().getName())
            .evaluatorType(permission.getEvaluator().getEvaluatorType())
            .weight(permission.getWeight())
            .build();
    }
}