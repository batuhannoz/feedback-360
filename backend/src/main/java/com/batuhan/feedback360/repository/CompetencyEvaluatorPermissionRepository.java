package com.batuhan.feedback360.repository;

import com.batuhan.feedback360.model.entitiy.CompetencyEvaluatorPermission;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompetencyEvaluatorPermissionRepository extends JpaRepository<CompetencyEvaluatorPermission, Long> {

    List<CompetencyEvaluatorPermission> findByPeriod_IdAndCompetency_Id(Integer periodId, Integer competencyId);

    void deleteByPeriod_IdAndCompetency_Id(Integer periodId, Integer competencyId);

    boolean existsByPeriod_IdAndCompetency_IdAndEvaluator_Id(Integer periodId, Integer competencyId, Integer evaluatorId);

    List<CompetencyEvaluatorPermission> findByPeriod_IdAndEvaluator_IdIn(Integer periodId, Collection<Integer> evaluatorIds);

    List<CompetencyEvaluatorPermission> findAllByPeriodId(Integer periodId);

}