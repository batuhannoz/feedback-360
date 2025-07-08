package com.batuhan.feedback360.service;

import com.batuhan.feedback360.config.AuthenticationPrincipalResolver;
import com.batuhan.feedback360.model.converter.EvaluationConverter;
import com.batuhan.feedback360.model.entitiy.Employee;
import com.batuhan.feedback360.model.entitiy.Evaluation;
import com.batuhan.feedback360.model.enums.EvaluationStatus;
import com.batuhan.feedback360.model.response.ApiResponse;
import com.batuhan.feedback360.model.response.EmployeeSimpleResponse;
import com.batuhan.feedback360.model.response.EvaluationDetailResponse;
import com.batuhan.feedback360.model.response.GivenEvaluationResponse;
import com.batuhan.feedback360.model.response.ParticipantCompletionStatusResponse;
import com.batuhan.feedback360.model.response.ReceivedEvaluationResponse;
import com.batuhan.feedback360.repository.EvaluationPeriodRepository;
import com.batuhan.feedback360.repository.EvaluationRepository;
import com.batuhan.feedback360.util.MessageHandler;
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
    private final AuthenticationPrincipalResolver principalResolver;
    private final MessageHandler messageHandler;

    public ApiResponse<List<ParticipantCompletionStatusResponse>> getParticipantCompletionStatus(Integer periodId) {
        Integer companyId = principalResolver.getCompanyId();
        if (!periodRepository.existsByIdAndCompanyId(periodId, companyId)) {
            return ApiResponse.failure(messageHandler.getMessage("error.period.notFound", periodId));
        }

        List<Evaluation> evaluationsInPeriod = evaluationRepository.findAllByPeriodId(periodId);

        Map<Employee, List<Evaluation>> evaluationsByEvaluator = evaluationsInPeriod.stream()
            .collect(Collectors.groupingBy(Evaluation::getEvaluator));

        List<ParticipantCompletionStatusResponse> responseData = evaluationsByEvaluator.entrySet().stream()
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

        return ApiResponse.success(responseData, messageHandler.getMessage("success.evaluation.completionStatusRetrieved", periodId));
    }

    public ApiResponse<List<ReceivedEvaluationResponse>> getReceivedEvaluationsForUser(Integer periodId, Integer userId) {
        Integer companyId = principalResolver.getCompanyId();
        if (!periodRepository.existsByIdAndCompanyId(periodId, companyId)) {
            return ApiResponse.failure(messageHandler.getMessage("error.period.notFound", periodId));
        }

        List<Evaluation> receivedEvaluations = evaluationRepository.findAllByEvaluated_IdAndPeriod_Id(userId, periodId);

        List<ReceivedEvaluationResponse> responseData = receivedEvaluations.stream()
            .map(e -> ReceivedEvaluationResponse.builder()
                .evaluationId(e.getId())
                .evaluator(new EmployeeSimpleResponse(e.getEvaluator().getId(), e.getEvaluator().getFullName()))
                .evaluated(new EmployeeSimpleResponse(e.getEvaluated().getId(), e.getEvaluated().getFullName()))
                .status(e.getStatus())
                .submissionDate(e.getDeliveryDate())
                .build())
            .collect(Collectors.toList());

        return ApiResponse.success(responseData, messageHandler.getMessage("success.evaluation.receivedRetrieved", userId, periodId));
    }

    public ApiResponse<List<GivenEvaluationResponse>> getGivenEvaluationsForUser(Integer periodId, Integer userId) {
        Integer companyId = principalResolver.getCompanyId();
        if (!periodRepository.existsByIdAndCompanyId(periodId, companyId)) {
            return ApiResponse.failure(messageHandler.getMessage("error.period.notFound", periodId));
        }

        List<Evaluation> givenEvaluations = evaluationRepository.findAllByEvaluator_IdAndPeriod_Id(userId, periodId);

        List<GivenEvaluationResponse> responseData = givenEvaluations.stream()
            .map(e -> GivenEvaluationResponse.builder()
                .evaluationId(e.getId())
                .evaluated(new EmployeeSimpleResponse(e.getEvaluated().getId(), e.getEvaluated().getFullName()))
                .evaluator(new EmployeeSimpleResponse(e.getEvaluator().getId(), e.getEvaluator().getFullName()))
                .status(e.getStatus())
                .submissionDate(e.getDeliveryDate())
                .build())
            .collect(Collectors.toList());

        return ApiResponse.success(responseData, messageHandler.getMessage("success.evaluation.givenRetrieved", userId, periodId));
    }

    public ApiResponse<EvaluationDetailResponse> getEvaluationDetailsForAdmin(Integer evaluationId) {
        Integer companyId = principalResolver.getCompanyId();
        Evaluation evaluation = evaluationRepository.findByIdWithAnswersAndQuestions(evaluationId).orElse(null);

        if (evaluation == null) {
            return ApiResponse.failure(messageHandler.getMessage("error.evaluation.notFound", evaluationId));
        }

        if (!evaluation.getPeriod().getCompany().getId().equals(companyId)) {
            return ApiResponse.failure(messageHandler.getMessage("error.evaluation.accessDenied"));
        }

        return ApiResponse.success(
            evaluationConverter.ToDetailResponse(evaluation),
            messageHandler.getMessage("success.evaluation.detailsRetrieved", evaluationId)
        );
    }
}
