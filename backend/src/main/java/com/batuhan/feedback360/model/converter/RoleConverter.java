package com.batuhan.feedback360.model.converter;

import com.batuhan.feedback360.model.entitiy.Role;
import com.batuhan.feedback360.model.response.RoleResponse;
import org.springframework.stereotype.Component;

@Component
public class RoleConverter {

    public RoleResponse toRoleResponse(Role role) {
        if (role == null) {
            return null;
        }

        return RoleResponse.builder()
            .id(role.getId())
            .name(role.getName())
            .build();
    }
}
