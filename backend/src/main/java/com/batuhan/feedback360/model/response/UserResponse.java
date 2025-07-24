package com.batuhan.feedback360.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
    private Boolean isActive;
    private Boolean isAdmin;
}

