package com.batuhan.feedback360.model.converter;


import com.batuhan.feedback360.model.entitiy.Company;
import com.batuhan.feedback360.model.response.CompanySettingsResponse;
import org.springframework.stereotype.Component;

@Component
public class CompanySettingsConverter {

    public CompanySettingsResponse toCompanySettingsResponse(Company company) {
        if (company == null) {
            return null;
        }
        return CompanySettingsResponse.builder()
            .id(company.getId())
            .name(company.getName())
            .email(company.getEmail())
            .phoneNumber(company.getPhoneNumber())
            .address(company.getAddress())
            .website(company.getWebsite())
            .emailFooter(company.getEmailFooter())
            .logoMimeType(company.getLogoMimeType())
            .build();
    }
}