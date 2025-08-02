package com.batuhan.feedback360.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserSignUpRequest {
    @NotBlank(message = "Email cannot be empty")
    private String invitationToken;
    private String password;
}
