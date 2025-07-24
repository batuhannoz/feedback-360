package com.batuhan.feedback360.repository;

import com.batuhan.feedback360.model.entitiy.Evaluator;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluatorRepository extends JpaRepository<Evaluator, Integer> {
    List<Evaluator> findAllByPeriod_Id(Integer period_id);

    List<Evaluator> findAllByIdInAndPeriod_Id(Collection<Integer> ids, Integer periodId);

    void deleteAllByPeriodId(Integer periodId);
}
