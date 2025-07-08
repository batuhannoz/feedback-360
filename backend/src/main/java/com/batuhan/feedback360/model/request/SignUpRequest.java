package com.batuhan.feedback360.model.request;

import lombok.Data;

@Data
public class SignUpRequest {
    private String companyName;
    private String email;
    private String password;
}
