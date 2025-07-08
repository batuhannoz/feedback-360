package com.batuhan.feedback360.service;

import com.batuhan.feedback360.config.AuthenticationPrincipalResolver;
import com.batuhan.feedback360.model.converter.EmployeeConverter;
import com.batuhan.feedback360.model.converter.RoleConverter;
import com.batuhan.feedback360.model.entitiy.Company;
import com.batuhan.feedback360.model.entitiy.Employee;
import com.batuhan.feedback360.model.entitiy.Role;
import com.batuhan.feedback360.model.request.RoleRequest;
import com.batuhan.feedback360.model.response.EmployeeDetailResponse;
import com.batuhan.feedback360.model.response.RoleResponse;
import com.batuhan.feedback360.repository.RoleRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import com.batuhan.feedback360.model.response.ApiResponse;
import com.batuhan.feedback360.util.MessageHandler;

@Service
@AllArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final AuthenticationPrincipalResolver principalResolver;
    private final RoleConverter roleConverter;
    private final EmployeeConverter employeeConverter;
    private final MessageHandler messageHandler;


    @Transactional
    public ApiResponse<RoleResponse> createRole(RoleRequest request) {
        Integer companyId = principalResolver.getCompanyId();

        if (roleRepository.findByNameAndCompanyId(request.getName(), companyId).isPresent()) {
            return ApiResponse.failure(messageHandler.getMessage("error.role.nameExists", request.getName()));
        }

        Role role = Role.builder()
            .name(request.getName())
            .company(new Company(companyId))
            .build();

        Role savedRole = roleRepository.save(role);
        return ApiResponse.success(
            roleConverter.toRoleResponse(savedRole),
            messageHandler.getMessage("success.role.created", savedRole.getName())
        );
    }

    @Transactional
    public ApiResponse<Void> deleteRole(Integer roleId) {
        Integer companyId = principalResolver.getCompanyId();
        Role role = roleRepository.findById(roleId).orElse(null);

        if (role == null) {
            return ApiResponse.failure(messageHandler.getMessage("error.role.notFound", roleId));
        }

        if (!role.getCompany().getId().equals(companyId)) {
            return ApiResponse.failure(messageHandler.getMessage("error.role.actionPermission"));
        }

        // TODO check before deletion

        roleRepository.delete(role);
        return ApiResponse.success(null, messageHandler.getMessage("success.role.deleted", roleId));
    }

    @Transactional
    public ApiResponse<List<RoleResponse>> getAllRoles() {
        Integer companyId = principalResolver.getCompanyId();
        List<Role> roles = roleRepository.findRolesByCompanyId(companyId);
        List<RoleResponse> responseData = roles.stream()
            .map(roleConverter::toRoleResponse)
            .collect(Collectors.toList());
        return ApiResponse.success(responseData, messageHandler.getMessage("success.roles.retrieved"));
    }

    @Transactional
    public ApiResponse<List<EmployeeDetailResponse>> getEmployeeForRoleId(Integer roleId) {
        Integer companyId = principalResolver.getCompanyId();
        Role role = roleRepository.findById(roleId).orElse(null);

        if (role == null) {
            return ApiResponse.failure(messageHandler.getMessage("error.role.notFound", roleId));
        }

        if (!role.getCompany().getId().equals(companyId)) {
            return ApiResponse.failure(messageHandler.getMessage("error.role.accessPermission"));
        }

        Set<Employee> employees = role.getEmployees();
        List<EmployeeDetailResponse> responseData = employees.stream()
            .map(employeeConverter::toEmployeeDetailResponse)
            .collect(Collectors.toList());
        return ApiResponse.success(responseData, messageHandler.getMessage("success.role.employeesRetrieved", role.getName()));
    }
}
