package com.batuhan.feedback360.model.request;

import lombok.Data;

@Data
public class EmployeeSignUpRequest {
    private String invitationToken;
    private String password;
}
