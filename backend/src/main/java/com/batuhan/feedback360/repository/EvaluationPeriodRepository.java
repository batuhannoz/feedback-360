package com.batuhan.feedback360.repository;

import com.batuhan.feedback360.model.entitiy.EvaluationPeriod;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EvaluationPeriodRepository extends JpaRepository<EvaluationPeriod, Integer> {
    List<EvaluationPeriod> findByCompanyId(Integer companyId);
    Optional<EvaluationPeriod> findByIdAndCompanyId(Integer id, Integer companyId);
    boolean existsByIdAndCompanyId(Integer id, Integer companyId);
}
