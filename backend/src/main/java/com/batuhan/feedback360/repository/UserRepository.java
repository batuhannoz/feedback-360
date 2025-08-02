package com.batuhan.feedback360.repository;

import com.batuhan.feedback360.model.entitiy.Company;
import com.batuhan.feedback360.model.entitiy.User;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);

    Optional<User> findByInvitationToken(String invitationToken);

    Optional<User> findByIdAndCompany(Integer id, Company company);

    List<User> findAllByIdInAndCompany_Id(List<Integer> userIds, Integer companyId);

    List<User> findAllByIdInAndCompany(Collection<Integer> ids, Company company);

    long countByCompany_Id(Integer companyId);
}