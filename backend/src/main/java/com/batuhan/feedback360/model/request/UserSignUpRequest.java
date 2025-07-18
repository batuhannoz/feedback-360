package com.batuhan.feedback360.model.request;

import lombok.Data;

@Data
public class UserSignUpRequest {
    private String invitationToken;
    private String password;
}
