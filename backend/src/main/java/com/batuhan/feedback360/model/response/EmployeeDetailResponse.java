package com.batuhan.feedback360.model.response;

import java.util.Set;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmployeeDetailResponse {
    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
    private boolean isAdmin;
    private boolean isActive;
    private Set<RoleResponse> roles;
}
