package com.batuhan.feedback360.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompanySettingsResponse {
    private Integer id;
    private String name;
    private String email;
    private String phoneNumber;
    private String address;
    private String website;
    private String emailFooter;
    private String logoMimeType;
}
