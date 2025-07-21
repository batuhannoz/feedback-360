package com.batuhan.feedback360.repository;


import com.batuhan.feedback360.model.entitiy.Answer;
import com.batuhan.feedback360.model.entitiy.EvaluationAssignment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Integer> {
    void deleteAllByAssignmentIn(List<EvaluationAssignment> assignments);

    List<Answer> findAllByAssignment_Id(Integer assignmentId);

    List<Answer> findAllByAssignment_PeriodParticipant_Period_IdAndAssignment_PeriodParticipant_EvaluatedUser_Id(Integer assignmentPeriodParticipantPeriodId,
                                                                                                                 Integer assignmentPeriodParticipantEvaluatedUserId);
}
