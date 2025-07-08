package com.batuhan.feedback360.model.request;

import lombok.Data;

@Data
public class EmployeeRequest {
    private String firstName;
    private String lastName;
    private String email;
    private boolean isAdmin;
}
