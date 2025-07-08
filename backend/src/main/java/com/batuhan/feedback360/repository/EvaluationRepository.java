package com.batuhan.feedback360.repository;

import com.batuhan.feedback360.model.entitiy.Employee;
import com.batuhan.feedback360.model.entitiy.Evaluation;
import com.batuhan.feedback360.model.entitiy.EvaluationPeriod;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, Integer> {
    List<Evaluation> findByEvaluatorOrEvaluated(Employee evaluator, Employee evaluated);
    List<Evaluation> findByEvaluatorAndPeriod(Employee evaluator, EvaluationPeriod period);

    @Query("SELECT e FROM Evaluation e " +
        "LEFT JOIN FETCH e.answers a " +
        "LEFT JOIN FETCH a.question " +
        "WHERE e.id = :evaluationId")
    Optional<Evaluation> findByIdWithAnswersAndQuestions(@Param("evaluationId") Integer evaluationId);

    List<Evaluation> findAllByPeriodId(Integer periodId);
    List<Evaluation> findAllByEvaluated_IdAndPeriod_Id(Integer evaluatedId, Integer periodId);
    List<Evaluation> findAllByEvaluator_IdAndPeriod_Id(Integer evaluatorId, Integer periodId);
}
