package com.batuhan.feedback360.model.converter;

import com.batuhan.feedback360.model.entitiy.Company;
import com.batuhan.feedback360.model.response.CompanyResponse;
import org.springframework.stereotype.Component;

@Component
public class CompanyConverter {

    public CompanyResponse toCompanyResponse(Company company) {
        if (company == null) {
            return null;
        }

        return CompanyResponse.builder()
            .id(company.getId())
            .name(company.getName())
            .build();
    }
}
