package com.batuhan.feedback360.model.request;

import lombok.Data;

@Data
public class CompanySignUpRequest {
    private String companyName;
    private String name;
    private String surname;
    private String email;
}
