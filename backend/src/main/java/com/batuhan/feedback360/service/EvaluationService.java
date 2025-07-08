package com.batuhan.feedback360.service;

import com.batuhan.feedback360.model.converter.EvaluationConverter;
import com.batuhan.feedback360.model.entitiy.Employee;
import com.batuhan.feedback360.model.entitiy.Evaluation;
import com.batuhan.feedback360.model.enums.EvaluationStatus;
import com.batuhan.feedback360.model.response.EmployeeSimpleResponse;
import com.batuhan.feedback360.model.response.EvaluationDetailResponse;
import com.batuhan.feedback360.model.response.GivenEvaluationResponse;
import com.batuhan.feedback360.model.response.ParticipantCompletionStatusResponse;
import com.batuhan.feedback360.model.response.ReceivedEvaluationResponse;
import com.batuhan.feedback360.repository.EvaluationPeriodRepository;
import com.batuhan.feedback360.repository.EvaluationRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EvaluationService {

    private final EvaluationPeriodRepository periodRepository;
    private final EvaluationRepository evaluationRepository;
    private final EvaluationConverter evaluationConverter;

    public List<ParticipantCompletionStatusResponse> getParticipantCompletionStatus(Integer periodId) {
        if (!periodRepository.existsById(periodId)) {
            throw new EntityNotFoundException("Değerlendirme dönemi bulunamadı: " + periodId);
        }

        List<Evaluation> evaluationsInPeriod = evaluationRepository.findAllByPeriodId(periodId);

        Map<Employee, List<Evaluation>> evaluationsByEvaluator = evaluationsInPeriod.stream()
            .collect(Collectors.groupingBy(Evaluation::getEvaluator));

        return evaluationsByEvaluator.entrySet().stream()
            .map(entry -> {
                Employee evaluator = entry.getKey();
                List<Evaluation> assignedEvaluations = entry.getValue();

                int totalCount = assignedEvaluations.size();
                int completedCount = (int) assignedEvaluations.stream()
                    .filter(e -> e.getStatus() == EvaluationStatus.COMPLETED)
                    .count();

                double percentage = (totalCount == 0) ? 0.0 : ((double) completedCount / totalCount) * 100;

                return ParticipantCompletionStatusResponse.builder()
                    .participant(new EmployeeSimpleResponse(evaluator.getId(), evaluator.getFullName()))
                    .totalEvaluationsAssigned(totalCount)
                    .completedEvaluations(completedCount)
                    .completionPercentage(percentage)
                    .build();
            })
            .collect(Collectors.toList());
    }

    public List<ReceivedEvaluationResponse> getReceivedEvaluationsForUser(Integer periodId, Integer userId) {
        List<Evaluation> receivedEvaluations = evaluationRepository.findAllByEvaluated_IdAndPeriod_Id(userId, periodId);

        return receivedEvaluations.stream()
            .map(e -> ReceivedEvaluationResponse.builder()
                .evaluationId(e.getId())
                .evaluator(new EmployeeSimpleResponse(e.getEvaluator().getId(), e.getEvaluator().getFullName()))
                .status(e.getStatus())
                .submissionDate(e.getDeliveryDate())
                .build())
            .collect(Collectors.toList());
    }

    public List<GivenEvaluationResponse> getGivenEvaluationsForUser(Integer periodId, Integer userId) {
        List<Evaluation> givenEvaluations = evaluationRepository.findAllByEvaluator_IdAndPeriod_Id(userId, periodId);

        return givenEvaluations.stream()
            .map(e -> GivenEvaluationResponse.builder()
                .evaluationId(e.getId())
                .evaluated(new EmployeeSimpleResponse(e.getEvaluated().getId(), e.getEvaluated().getFullName()))
                .status(e.getStatus())
                .submissionDate(e.getDeliveryDate())
                .build())
            .collect(Collectors.toList());
    }

    public EvaluationDetailResponse getEvaluationDetailsForAdmin(Integer evaluationId) {
        Evaluation evaluation = evaluationRepository.findByIdWithAnswersAndQuestions(evaluationId)
            .orElseThrow(() -> new EntityNotFoundException("Değerlendirme bulunamadı: " + evaluationId));
        return evaluationConverter.ToDetailResponse(evaluation);
    }
}
