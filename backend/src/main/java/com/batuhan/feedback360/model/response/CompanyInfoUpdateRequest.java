package com.batuhan.feedback360.model.response;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CompanyInfoUpdateRequest {
    @NotBlank
    private String name;
    @Email
    @NotBlank
    private String email;
    private String phoneNumber;
    private String address;
    private String website;
    private String emailFooter;
}
