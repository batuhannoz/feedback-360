package com.batuhan.feedback360.model.request;

import lombok.Data;

@Data
public class SignInRequest {
    private String email;
    private String password;
}