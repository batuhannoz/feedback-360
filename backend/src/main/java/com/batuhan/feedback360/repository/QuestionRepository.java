package com.batuhan.feedback360.repository;

import com.batuhan.feedback360.model.entitiy.EvaluationTemplate;
import com.batuhan.feedback360.model.entitiy.Question;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Integer> {
    Optional<Question> findByIdAndCompanyId(Integer id, Integer companyId);
}
