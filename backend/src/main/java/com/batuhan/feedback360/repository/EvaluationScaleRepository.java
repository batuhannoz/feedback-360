package com.batuhan.feedback360.repository;

import com.batuhan.feedback360.model.entitiy.EvaluationScale;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EvaluationScaleRepository extends JpaRepository<EvaluationScale, Integer> {
    Optional<EvaluationScale> findByIdAndCompanyId(Integer id, Integer companyId);

    List<EvaluationScale> findAllByCompanyId(Integer companyId);

    boolean existsByIdAndCompanyId(Integer id, Integer companyId);
}
