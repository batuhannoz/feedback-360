package com.batuhan.feedback360.repository;

import com.batuhan.feedback360.model.entitiy.Company;
import com.batuhan.feedback360.model.entitiy.EvaluationTemplate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EvaluationTemplateRepository extends JpaRepository<EvaluationTemplate, Integer> {
    Optional<EvaluationTemplate> findByIdAndCompanyId(Integer id, Integer companyId);
    List<EvaluationTemplate> findAllByCompany(Company company);
}
