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

@Service
@AllArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final AuthenticationPrincipalResolver principalResolver;
    private final RoleConverter roleConverter;
    private final EmployeeConverter employeeConverter;

    @Transactional
    public RoleResponse createRole(RoleRequest request) {
        Company company = new Company();
        company.setId(Math.toIntExact(principalResolver.getCompanyId()));

        roleRepository.findByNameAndCompanyId(request.getName(), company.getId())
            .ifPresent(r -> {
                throw new IllegalArgumentException("Role with name '" + request.getName() + "' already exists.");
            });

        Role role = Role.builder()
            .name(request.getName())
            .company(company)
            .build();

        Role savedRole = roleRepository.save(role);
        return roleConverter.toRoleResponse(savedRole);
    }

    @Transactional
    public void deleteRole(Integer roleId) {
        Company company = new Company();
        company.setId(Math.toIntExact(principalResolver.getCompanyId()));

        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId));

        if (!role.getCompany().getId().equals(company.getId())) {
            throw new SecurityException("You do not have permission to delete this role.");
        }
        roleRepository.delete(role);
    }

    @Transactional
    public List<RoleResponse> getAllRoles() {
        Integer companyId = Math.toIntExact(principalResolver.getCompanyId());
        List<Role> roles = roleRepository.findRolesByCompanyId(companyId);
        return roles.stream()
            .map(roleConverter::toRoleResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public List<EmployeeDetailResponse> getEmployeeForRoleId(Integer roleId) {
        Integer companyId = Math.toIntExact(principalResolver.getCompanyId());

        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId));

        if (!role.getCompany().getId().equals(companyId)) {
            throw new SecurityException("You do not have permission to access this role.");
        }

        Set<Employee> employees = role.getEmployees();

        return employees.stream()
            .map(employeeConverter::toEmployeeDetailResponse)
            .collect(Collectors.toList());
    }
}
