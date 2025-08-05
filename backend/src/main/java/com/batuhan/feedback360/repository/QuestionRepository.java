package com.batuhan.feedback360.repository;

import com.batuhan.feedback360.model.entitiy.Question;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Integer> {
    Optional<Question> findByIdAndCompanyId(Integer id, Integer companyId);

    List<Question> findByCompetency_Id(Integer competencyId);

    Optional<Question> findByIdAndCompetency_Id(Integer id, Integer competencyId);

    void deleteByCompetency_Id(Integer competencyId);

    List<Question> findByCompetency_IdIn(Collection<Integer> competencyIds);

    boolean existsByEvaluationScaleId(Integer evaluationScaleId);
}
