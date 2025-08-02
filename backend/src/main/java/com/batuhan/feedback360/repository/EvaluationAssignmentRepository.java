package com.batuhan.feedback360.repository;

import com.batuhan.feedback360.model.entitiy.EvaluationAssignment;
import com.batuhan.feedback360.model.entitiy.PeriodParticipant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluationAssignmentRepository extends JpaRepository<EvaluationAssignment, Integer> {
    void deleteAllByPeriodParticipant(PeriodParticipant periodParticipant);

    List<EvaluationAssignment> findAllByPeriodParticipant(PeriodParticipant periodParticipant);

    List<EvaluationAssignment> findAllByEvaluatorUser_IdAndPeriodParticipant_Period_Id(Integer evaluatorUserId, Integer periodParticipantPeriodId);

    Optional<EvaluationAssignment> findFirstByEvaluatorUser_IdAndPeriodParticipant_Period_IdAndPeriodParticipant_EvaluatedUser_Id(Integer evaluatorUserId, Integer periodParticipantPeriodId,
                                                                                                                                  Integer periodParticipantEvaluatedUserId);

    List<EvaluationAssignment> findAllByEvaluatorUser_IdAndPeriodParticipant_Period_IdAndPeriodParticipant_EvaluatedUser_Id(Integer evaluatorUserId, Integer periodParticipantPeriodId,
                                                                                                                            Integer periodParticipantEvaluatedUserId);

    List<EvaluationAssignment> findAllByPeriodParticipant_EvaluatedUser_IdAndPeriodParticipant_Period_Id(Integer periodParticipantEvaluatedUserId, Integer periodParticipantPeriodId);

    List<EvaluationAssignment> findAllByPeriodParticipant_Period_IdIn(Collection<Integer> periodParticipantPeriodIds);
}