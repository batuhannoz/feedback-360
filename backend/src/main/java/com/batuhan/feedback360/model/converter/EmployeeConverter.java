package com.batuhan.feedback360.model.converter;

import com.batuhan.feedback360.model.entitiy.Employee;
import com.batuhan.feedback360.model.response.EmployeeDetailResponse;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmployeeConverter {

    private final RoleConverter roleConverter;

    public EmployeeDetailResponse toEmployeeDetailResponse(Employee employee) {
        if (employee == null) {
            return null;
        }

        return EmployeeDetailResponse.builder()
            .id(employee.getId())
            .firstName(employee.getFirstName())
            .lastName(employee.getLastName())
            .email(employee.getEmail())
            .isAdmin(employee.getIsAdmin())
            .isActive(employee.getIsActive())
            .roles(employee.getRoles().stream()
                .map(roleConverter::toRoleResponse)
                .collect(Collectors.toSet()))
            .build();
    }
}
