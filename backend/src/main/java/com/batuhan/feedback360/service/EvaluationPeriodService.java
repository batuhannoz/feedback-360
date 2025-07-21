package com.batuhan.feedback360.service;

import com.batuhan.feedback360.config.AuthenticationPrincipalResolver;
import com.batuhan.feedback360.model.converter.AnswerConverter;
import com.batuhan.feedback360.model.converter.AssignmentConverter;
import com.batuhan.feedback360.model.converter.CompetencyConverter;
import com.batuhan.feedback360.model.converter.CompetencyEvaluatorPermissionConverter;
import com.batuhan.feedback360.model.converter.EvaluationPeriodConverter;
import com.batuhan.feedback360.model.converter.EvaluatorConverter;
import com.batuhan.feedback360.model.converter.QuestionConverter;
import com.batuhan.feedback360.model.converter.UserConverter;
import com.batuhan.feedback360.model.entitiy.Company;
import com.batuhan.feedback360.model.entitiy.Competency;
import com.batuhan.feedback360.model.entitiy.CompetencyEvaluatorPermission;
import com.batuhan.feedback360.model.entitiy.EvaluationPeriod;
import com.batuhan.feedback360.model.entitiy.Evaluator;
import com.batuhan.feedback360.model.entitiy.PeriodCompetencyWeight;
import com.batuhan.feedback360.model.enums.EvaluatorType;
import com.batuhan.feedback360.model.enums.PeriodStatus;
import com.batuhan.feedback360.model.request.CompetencyEvaluatorWeight;
import com.batuhan.feedback360.model.request.EvaluationPeriodRequest;
import com.batuhan.feedback360.model.request.EvaluatorRequest;
import com.batuhan.feedback360.model.request.SetCompetencyEvaluatorWeightsRequest;
import com.batuhan.feedback360.model.request.UpdatePeriodStatusRequest;
import com.batuhan.feedback360.model.response.ApiResponse;
import com.batuhan.feedback360.model.response.CompetencyEvaluatorPermissionResponse;
import com.batuhan.feedback360.model.response.CompetencyWeightResponse;
import com.batuhan.feedback360.model.response.EvaluationPeriodResponse;
import com.batuhan.feedback360.model.response.EvaluatorResponse;
import com.batuhan.feedback360.repository.AnswerRepository;
import com.batuhan.feedback360.repository.CompanyRepository;
import com.batuhan.feedback360.repository.CompetencyEvaluatorPermissionRepository;
import com.batuhan.feedback360.repository.CompetencyRepository;
import com.batuhan.feedback360.repository.EvaluationAssignmentRepository;
import com.batuhan.feedback360.repository.EvaluationPeriodRepository;
import com.batuhan.feedback360.repository.EvaluatorRepository;
import com.batuhan.feedback360.repository.PeriodCompetencyWeightRepository;
import com.batuhan.feedback360.repository.PeriodParticipantRepository;
import com.batuhan.feedback360.repository.QuestionRepository;
import com.batuhan.feedback360.repository.UserRepository;
import com.batuhan.feedback360.util.MessageHandler;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
    private final EmailService emailService;
    private final EvaluatorConverter evaluatorConverter;
    private final UserConverter userConverter;
    private final PeriodParticipantRepository periodParticipantRepository;
    private final UserRepository userRepository;
    private final EvaluationAssignmentRepository evaluationAssignmentRepository;
    private final AssignmentConverter assignmentConverter;
    private final CompetencyRepository competencyRepository;
    private final CompetencyConverter competencyConverter;
    private final PeriodCompetencyWeightRepository periodCompetencyWeightRepository;
    private final QuestionConverter questionConverter;
    private final QuestionRepository questionRepository;
    private final CompetencyEvaluatorPermissionRepository competencyEvaluatorPermissionRepository;
    private final CompetencyEvaluatorPermissionConverter competencyEvaluatorPermissionConverter;
    private final AnswerConverter answerConverter;
    private final AnswerRepository answerRepository;

    public ApiResponse<EvaluationPeriodResponse> createEvaluationPeriod(EvaluationPeriodRequest request) {
        Company company = companyRepository.getReferenceById(principalResolver.getCompanyId());

        EvaluationPeriod evaluationPeriod = evaluationPeriodRepository.save(EvaluationPeriod.builder()
            .company(company)
            .name(request.getName())
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

    public ApiResponse<EvaluationPeriodResponse> updateEvaluationPeriod(Integer evaluationPeriodId, EvaluationPeriodRequest request) {
        EvaluationPeriod evaluationPeriod = evaluationPeriodRepository.findByIdAndCompanyId(
            evaluationPeriodId,
            principalResolver.getCompanyId()
        ).orElse(null);
        if (evaluationPeriod == null) {
            return ApiResponse.failure(messageHandler.getMessage("evaluation-period.not-found"));
        }
        evaluationPeriod.setName(request.getName());
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

            if (totalCompetencyWeight.compareTo(new BigDecimal("100.00")) != 0) {
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
                if (totalEvaluatorWeight.compareTo(new BigDecimal("100.00")) != 0) {
                    return ApiResponse.failure(messageHandler.getMessage("evaluation-period.start.evaluator-weight-sum-invalid", competency.getTitle()));
                }
            }
        }

        period.setStatus(request.getStatus());
        evaluationPeriodRepository.save(period);

        return ApiResponse.success(
            evaluationPeriodConverter.toEvaluationPeriodResponse(period),
            messageHandler.getMessage("evaluation-period.status.update.success")
        );
    }


    public ApiResponse<List<EvaluatorResponse>> getEvaluators(Integer periodId) {
        List<Evaluator> evaluators = evaluatorRepository.findAllByPeriod_Id(periodId);
        List<EvaluatorResponse> evaluatorResponses = evaluators.stream()
            .map(evaluatorConverter::toEvaluatorResponse).toList();

        return ApiResponse.success(evaluatorResponses,
            messageHandler.getMessage("evaluation-period.evaluators.success"));
    }

    public ApiResponse<List<EvaluatorResponse>> setEvaluators(Integer periodId, List<EvaluatorRequest> request) {
        if (!evaluationPeriodRepository.existsByIdAndCompanyId(periodId, principalResolver.getCompanyId())) {
            return ApiResponse.failure(messageHandler.getMessage("evaluation-period.no-permission-update"));
        }
        List<Evaluator> existingEvaluators = evaluatorRepository.findAllByPeriod_Id(periodId);
        if (existingEvaluators.isEmpty()) {
            return ApiResponse.failure(messageHandler.getMessage("evaluation-period.evaluators.not-found"));
        }
        Map<EvaluatorType, Evaluator> evaluatorMap = existingEvaluators.stream()
            .collect(Collectors.toMap(Evaluator::getEvaluatorType, evaluator -> evaluator));

        request.forEach(req -> {
            if (req.getEvaluatorType() != null) {
                Evaluator evaluatorToUpdate = evaluatorMap.get(req.getEvaluatorType());
                if (evaluatorToUpdate != null) {
                    evaluatorToUpdate.setName(req.getName());
                }
            }
        });
        List<Evaluator> updatedEvaluators = evaluatorRepository.saveAll(evaluatorMap.values());
        List<EvaluatorResponse> updatedEvaluatorsResponse = updatedEvaluators.stream()
            .map(evaluatorConverter::toEvaluatorResponse)
            .collect(Collectors.toList());

        return ApiResponse.success(updatedEvaluatorsResponse, messageHandler.getMessage("evaluation-period.evaluators.update.success"));
    }



    public ApiResponse<List<CompetencyEvaluatorPermissionResponse>> getCompetencyEvaluatorPermissions(Integer periodId, Integer competencyId) {
        if (validatePeriodAndCompetency(periodId, competencyId).isEmpty()) {
            return ApiResponse.failure(messageHandler.getMessage("competency.not-found-in-period", competencyId));
        }
        List<CompetencyEvaluatorPermission> permissions = competencyEvaluatorPermissionRepository.findByPeriod_IdAndCompetency_Id(periodId, competencyId);
        List<CompetencyEvaluatorPermissionResponse> response = permissions.stream()
            .map(competencyEvaluatorPermissionConverter::toResponse)
            .collect(Collectors.toList());

        return ApiResponse.success(response, messageHandler.getMessage("competency.evaluator.permission.get.success"));
    }


    @Transactional
    public ApiResponse<List<CompetencyEvaluatorPermissionResponse>> setCompetencyEvaluatorWeights(
        Integer periodId,
        Integer competencyId,
        SetCompetencyEvaluatorWeightsRequest request
    ) {
        if (validatePeriodAndCompetency(periodId, competencyId).isEmpty()) {
            return ApiResponse.failure(messageHandler.getMessage("competency.not-found-in-period", competencyId));
        }
        List<CompetencyEvaluatorPermission> permissions = competencyEvaluatorPermissionRepository.findByPeriod_IdAndCompetency_Id(periodId, competencyId);
        if (permissions.isEmpty()) {
            return ApiResponse.failure(messageHandler.getMessage("competency.evaluator.not-assigned"));
        }
        BigDecimal totalWeight = request.getWeights().stream().map(CompetencyEvaluatorWeight::getWeight).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalWeight.compareTo(new BigDecimal("100.00")) != 0) {
            return ApiResponse.failure(messageHandler.getMessage("competency.evaluator.weight.sum.invalid"));
        }

        Map<Integer, CompetencyEvaluatorPermission> permissionMap = permissions.stream()
            .collect(Collectors.toMap(p -> p.getEvaluator().getId(), p -> p));

        Set<Integer> assignedEvaluatorIds = permissionMap.keySet();
        Set<Integer> requestEvaluatorIds = request.getWeights().stream().map(CompetencyEvaluatorWeight::getEvaluatorId).collect(Collectors.toSet());

        if (!assignedEvaluatorIds.equals(requestEvaluatorIds)) {
            return ApiResponse.failure(messageHandler.getMessage("competency.evaluator.assignment.mismatch"));
        }

        request.getWeights().forEach(dto -> {
            CompetencyEvaluatorPermission permission = permissionMap.get(dto.getEvaluatorId());
            permission.setWeight(dto.getWeight());
        });

        List<CompetencyEvaluatorPermission> updatedPermissions = competencyEvaluatorPermissionRepository.saveAll(permissionMap.values());

        List<CompetencyEvaluatorPermissionResponse> response = updatedPermissions.stream()
            .map(competencyEvaluatorPermissionConverter::toResponse)
            .collect(Collectors.toList());

        return ApiResponse.success(response, messageHandler.getMessage("competency.evaluator.weight.update.success"));
    }

    public ApiResponse<List<CompetencyWeightResponse>> getCompetenciesWithWeights(Integer periodId) {
        if (!evaluationPeriodRepository.existsByIdAndCompanyId(periodId, principalResolver.getCompanyId())) {
            return ApiResponse.failure(messageHandler.getMessage("evaluation-period.no-permission-view"));
        }

        List<PeriodCompetencyWeight> periodWeights = periodCompetencyWeightRepository.findAllByPeriod_Id(periodId);
        List<CompetencyWeightResponse> response = periodWeights.stream()
            .map(pcw -> CompetencyWeightResponse.builder()
                .competencyId(pcw.getCompetency().getId())
                .competencyTitle(pcw.getCompetency().getTitle())
                .weight(pcw.getWeight())
                .build()).toList();

        return ApiResponse.success(response, messageHandler.getMessage("competency.weight.get.success"));
    }

    private Optional<Competency> validatePeriodAndCompetency(Integer periodId, Integer competencyId) {
        if (!evaluationPeriodRepository.existsByIdAndCompanyId(periodId, principalResolver.getCompanyId())) {
            return Optional.empty();
        }
        return periodCompetencyWeightRepository.findByPeriod_IdAndCompetency_Id(periodId, competencyId)
            .map(PeriodCompetencyWeight::getCompetency);
    }
}
