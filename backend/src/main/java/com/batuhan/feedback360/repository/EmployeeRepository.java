package com.batuhan.feedback360.repository;

import com.batuhan.feedback360.model.entitiy.Company;
import com.batuhan.feedback360.model.entitiy.Employee;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    Optional<Employee> findByEmail(String email);
    Optional<Employee> findByInvitationToken(String invitationToken);
    List<Employee> findByCompanyId(Integer companyId);
    List<Employee> findAllByCompany(Company company);
}
