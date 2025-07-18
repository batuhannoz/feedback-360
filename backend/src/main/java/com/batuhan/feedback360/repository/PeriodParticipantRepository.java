package com.batuhan.feedback360.repository;

import com.batuhan.feedback360.model.entitiy.PeriodParticipant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PeriodParticipantRepository extends JpaRepository<PeriodParticipant, Integer> {
    List<PeriodParticipant> findAllByPeriod_Id(Integer periodId);

    boolean existsByPeriod_IdAndEvaluatedUser_Id(Integer periodId, Integer userId);

    Optional<PeriodParticipant> findByPeriod_IdAndEvaluatedUser_Id(Integer periodId, Integer userId);
}
