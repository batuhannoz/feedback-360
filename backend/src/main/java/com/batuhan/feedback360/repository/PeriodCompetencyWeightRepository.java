package com.batuhan.feedback360.repository;

import com.batuhan.feedback360.model.entitiy.PeriodCompetencyWeight;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PeriodCompetencyWeightRepository extends JpaRepository<PeriodCompetencyWeight, Integer> {
    List<PeriodCompetencyWeight> findAllByPeriod_Id(Integer periodId);

    Optional<PeriodCompetencyWeight> findByPeriod_IdAndCompetency_Id(Integer periodId, Integer competencyId);

    void deleteByPeriod_IdAndCompetency_Id(Integer periodId, Integer competencyId);

    void deleteAllByPeriodId(Integer periodId);
}
