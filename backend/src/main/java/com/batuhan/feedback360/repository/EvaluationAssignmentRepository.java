package com.batuhan.feedback360.repository;

import com.batuhan.feedback360.model.entitiy.EvaluationAssignment;
import com.batuhan.feedback360.model.entitiy.PeriodParticipant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluationAssignmentRepository extends JpaRepository<EvaluationAssignment, Integer> {
    void deleteAllByPeriodParticipant(PeriodParticipant periodParticipant);

    List<EvaluationAssignment> findAllByPeriodParticipant(PeriodParticipant periodParticipant);
}