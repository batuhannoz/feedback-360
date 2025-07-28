package com.batuhan.feedback360.service;

import com.batuhan.feedback360.config.AuthenticationPrincipalResolver;
import com.batuhan.feedback360.model.converter.EvaluationPeriodConverter;
import com.batuhan.feedback360.model.entitiy.Company;
import com.batuhan.feedback360.model.entitiy.Competency;
import com.batuhan.feedback360.model.entitiy.CompetencyEvaluatorPermission;
import com.batuhan.feedback360.model.entitiy.EvaluationPeriod;
import com.batuhan.feedback360.model.entitiy.Evaluator;
import com.batuhan.feedback360.model.entitiy.PeriodCompetencyWeight;
import com.batuhan.feedback360.model.entitiy.PeriodParticipant;
import com.batuhan.feedback360.model.enums.EvaluatorType;
import com.batuhan.feedback360.model.enums.PeriodStatus;
import com.batuhan.feedback360.model.request.EvaluationPeriodRequest;
import com.batuhan.feedback360.model.request.UpdatePeriodStatusRequest;
import com.batuhan.feedback360.model.response.ApiResponse;
import com.batuhan.feedback360.model.response.EvaluationPeriodDetailResponse;
import com.batuhan.feedback360.model.response.EvaluationPeriodResponse;
import com.batuhan.feedback360.model.response.ParticipantDetailResponse;
import com.batuhan.feedback360.repository.CompanyRepository;
import com.batuhan.feedback360.repository.CompetencyEvaluatorPermissionRepository;
import com.batuhan.feedback360.repository.EvaluationAssignmentRepository;
import com.batuhan.feedback360.repository.EvaluationPeriodRepository;
import com.batuhan.feedback360.repository.EvaluatorRepository;
import com.batuhan.feedback360.repository.PeriodCompetencyWeightRepository;
import com.batuhan.feedback360.repository.PeriodParticipantRepository;
import com.batuhan.feedback360.util.MessageHandler;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class EvaluationPeriodService {

    private final EvaluationPeriodRepository evaluationPeriodRepository;
    private final CompanyRepository companyRepository;
    private final AuthenticationPrincipalResolver principalResolver;
    private final EvaluationPeriodConverter evaluationPeriodConverter;
    private final EvaluatorRepository evaluatorRepository;
    private final MessageHandler messageHandler;
    private final PeriodParticipantRepository periodParticipantRepository;
    private final PeriodCompetencyWeightRepository periodCompetencyWeightRepository;
    private final CompetencyEvaluatorPermissionRepository competencyEvaluatorPermissionRepository;
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100.00");
    private final EmailService emailService;
    private final EvaluationAssignmentRepository evaluationAssignmentRepository;
    public ApiResponse<EvaluationPeriodResponse> createEvaluationPeriod(EvaluationPeriodRequest request) {
        Company company = companyRepository.getReferenceById(principalResolver.getCompanyId());

        EvaluationPeriod evaluationPeriod = evaluationPeriodRepository.save(EvaluationPeriod.builder()
            .company(company)
            .evaluationName(request.getEvaluationName())
            .periodName(request.getPeriodName())
            .internalPeriodName(request.getInternalPeriodName())
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .status(PeriodStatus.NOT_STARTED)
            .build());

        List<Evaluator> defaultEvaluators = Arrays.stream(EvaluatorType.values())
            .map(type -> Evaluator.builder()
                .period(evaluationPeriod)
                .evaluatorType(type)
                .build())
            .collect(Collectors.toList());

        evaluatorRepository.saveAll(defaultEvaluators);

        return ApiResponse.success(
            evaluationPeriodConverter.toEvaluationPeriodResponse(evaluationPeriod),
            messageHandler.getMessage("evaluation-period.create.success")
        );
    }

    public ApiResponse<List<EvaluationPeriodResponse>> getEvaluationPeriods() {
        Company company = companyRepository.getReferenceById(principalResolver.getCompanyId());
        List<EvaluationPeriod> evaluationPeriods = evaluationPeriodRepository.findByCompanyOrderByCreatedAtDesc(company);
        List<EvaluationPeriodResponse> responseList = evaluationPeriods.stream()
            .map(evaluationPeriodConverter::toEvaluationPeriodResponse).toList();
        return ApiResponse.success(responseList, messageHandler.getMessage("evaluation-period.list.success"));
    }

    public ApiResponse<EvaluationPeriodResponse> getEvaluationPeriodById(Integer evaluationPeriodId) {
        EvaluationPeriod evaluationPeriod = evaluationPeriodRepository.findById(evaluationPeriodId).orElse(null);
        if (evaluationPeriod == null) {
            return ApiResponse.failure(messageHandler.getMessage("evaluation-period.not-found"));
        }
        if (!evaluationPeriod.getCompany().getId().equals(principalResolver.getCompanyId())) {
            return ApiResponse.failure(messageHandler.getMessage("evaluation-period.no-permission-view"));
        }
        return ApiResponse.success(
            evaluationPeriodConverter.toEvaluationPeriodResponse(evaluationPeriod),
            messageHandler.getMessage("evaluation-period.get.success")
        );
    }

    public ApiResponse<EvaluationPeriodDetailResponse> getEvaluationPeriodDetails(Long periodId) {
        EvaluationPeriod period = evaluationPeriodRepository.findById(periodId.intValue()).orElse(null);
        if (period == null) {
            return ApiResponse.failure(messageHandler.getMessage("evaluation-period.not-found"));
        }

        List<PeriodParticipant> participants = periodParticipantRepository.findAllByPeriod(period);

        List<ParticipantDetailResponse> participantDetails = participants.stream().map(participant -> {
            List<com.batuhan.feedback360.model.entitiy.EvaluationAssignment> assignments = evaluationAssignmentRepository.findAllByPeriodParticipant(participant);
            long completedCount = assignments.stream().filter(a -> a.getAnswers() != null && !a.getAnswers().isEmpty()).count();
            return ParticipantDetailResponse.builder()
                .userId(participant.getEvaluatedUser().getId().longValue())
                .firstName(participant.getEvaluatedUser().getFirstName())
                .lastName(participant.getEvaluatedUser().getLastName())
                .email(participant.getEvaluatedUser().getEmail())
                .totalAssignments(assignments.size())
                .completedAssignments((int) completedCount)
                .build();
        }).collect(Collectors.toList());

        EvaluationPeriodDetailResponse response = EvaluationPeriodDetailResponse.builder()
            .id(period.getId().longValue())
            .periodName(period.getPeriodName())
            .startDate(period.getStartDate().toLocalDate())
            .endDate(period.getEndDate().toLocalDate())
            .status(period.getStatus().name())
            .participants(participantDetails)
            .build();

        return ApiResponse.success(response, messageHandler.getMessage("evaluation-period.detail.get.success"));
    }

    public ApiResponse<EvaluationPeriodResponse> updateEvaluationPeriod(Integer evaluationPeriodId, EvaluationPeriodRequest request) {
        EvaluationPeriod evaluationPeriod = evaluationPeriodRepository.findByIdAndCompanyId(
            evaluationPeriodId,
            principalResolver.getCompanyId()
        ).orElse(null);
        if (evaluationPeriod == null) {
            return ApiResponse.failure(messageHandler.getMessage("evaluation-period.not-found"));
        }
        evaluationPeriod.setEvaluationName(request.getEvaluationName());
        evaluationPeriod.setPeriodName(request.getPeriodName());
        evaluationPeriod.setInternalPeriodName(request.getInternalPeriodName());
        evaluationPeriod.setStartDate(request.getStartDate());
        evaluationPeriod.setEndDate(request.getEndDate());
        evaluationPeriod.setStatus(request.getStatus());
        evaluationPeriodRepository.save(evaluationPeriod);

        return ApiResponse.success(evaluationPeriodConverter.toEvaluationPeriodResponse(evaluationPeriod),
            messageHandler.getMessage("evaluation-period.update.success"));
    }

    @Transactional
    public ApiResponse<?> updatePeriodStatus(Integer periodId, UpdatePeriodStatusRequest request) {
        EvaluationPeriod period = evaluationPeriodRepository.findByIdAndCompanyId(periodId, principalResolver.getCompanyId())
            .orElse(null);

        if (period == null) {
            return ApiResponse.failure(messageHandler.getMessage("evaluation-period.not-found"));
        }

        if (request.getStatus() == PeriodStatus.IN_PROGRESS) {
            if (period.getStatus() != PeriodStatus.NOT_STARTED) {
                return ApiResponse.failure(messageHandler.getMessage("evaluation-period.start.invalid-state"));
            }
            if (periodParticipantRepository.countByPeriod_Id(periodId) == 0) {
                return ApiResponse.failure(messageHandler.getMessage("evaluation-period.start.no-participants"));
            }
            List<PeriodCompetencyWeight> periodCompetencyWeights = periodCompetencyWeightRepository.findAllByPeriod_Id(periodId);
            if (periodCompetencyWeights.isEmpty()) {
                return ApiResponse.failure(messageHandler.getMessage("evaluation-period.start.no-competencies"));
            }
            BigDecimal totalCompetencyWeight = periodCompetencyWeights.stream()
                .map(PeriodCompetencyWeight::getWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (totalCompetencyWeight.compareTo(ONE_HUNDRED) != 0) {
                return ApiResponse.failure(messageHandler.getMessage("evaluation-period.start.competency-weight-sum-invalid"));
            }
            for (PeriodCompetencyWeight pcw : periodCompetencyWeights) {
                Competency competency = pcw.getCompetency();
                List<CompetencyEvaluatorPermission> permissions = competencyEvaluatorPermissionRepository.findByPeriod_IdAndCompetency_Id(periodId, competency.getId());
                if (permissions.isEmpty()) {
                    return ApiResponse.failure(messageHandler.getMessage("evaluation-period.start.no-evaluators-for-competency", competency.getTitle()));
                }
                BigDecimal totalEvaluatorWeight = permissions.stream()
                    .map(CompetencyEvaluatorPermission::getWeight)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                if (totalEvaluatorWeight.compareTo(ONE_HUNDRED) != 0) {
                    return ApiResponse.failure(messageHandler.getMessage("evaluation-period.start.evaluator-weight-sum-invalid", competency.getTitle()));
                }
            }

            List<PeriodParticipant> participants = periodParticipantRepository.findAllByPeriod_Id(periodId);
            for (PeriodParticipant participant : participants) {
                if (participant.getEvaluatedUser() != null && participant.getEvaluatedUser().getEmail() != null) {
                    emailService.sendPeriodStartedEmail(participant.getEvaluatedUser().getEmail(), period.getEvaluationName());
                }
            }
        }

        period.setStatus(request.getStatus());
        evaluationPeriodRepository.save(period);

        return ApiResponse.success(messageHandler.getMessage("evaluation-period.status.update.success"));
    }

    @Transactional
    public ApiResponse<?> deleteEvaluationPeriod(Integer periodId) {
        EvaluationPeriod period = evaluationPeriodRepository.findByIdAndCompanyId(
            periodId,
            principalResolver.getCompanyId()
        ).orElse(null);

        if (period == null) {
            return ApiResponse.failure(messageHandler.getMessage("evaluation-period.not-found"));
        }
        if (period.getStatus() == PeriodStatus.IN_PROGRESS) {
            return ApiResponse.failure(messageHandler.getMessage("evaluation-period.delete.invalid-state", period.getStatus().name()));
        }

        competencyEvaluatorPermissionRepository.deleteAllByPeriodId(periodId);
        periodCompetencyWeightRepository.deleteAllByPeriodId(periodId);
        periodParticipantRepository.deleteAllByPeriodId(periodId);
        evaluatorRepository.deleteAllByPeriodId(periodId);

        evaluationPeriodRepository.delete(period);

        return ApiResponse.success(messageHandler.getMessage("evaluation-period.delete.success"));
    }
}
