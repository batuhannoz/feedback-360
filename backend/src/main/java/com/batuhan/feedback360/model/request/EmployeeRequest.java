package com.batuhan.feedback360.model.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class EmployeeRequest {
    private String firstName;
    private String lastName;
    private String email;
    private Boolean isAdmin;
}
