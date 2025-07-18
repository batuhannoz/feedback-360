package com.batuhan.feedback360.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UserSimpleResponse {
    private Integer id;
    private String fullName;
}
