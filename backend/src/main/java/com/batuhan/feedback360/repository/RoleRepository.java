package com.batuhan.feedback360.repository;

import com.batuhan.feedback360.model.entitiy.Role;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    List<Role> findByCompanyId(Integer companyId);
    Optional<Role> findByNameAndCompanyId(String name, Integer companyId);
    List<Role> findRolesByCompanyId(Integer companyId);
    Optional<Role> findByIdAndCompanyId(Integer id, Integer companyId);
    Set<Role> findAllByIdInAndCompanyId(Set<Integer> ids, Integer companyId);
}
