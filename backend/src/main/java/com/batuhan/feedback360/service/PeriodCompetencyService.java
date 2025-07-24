package com.batuhan.feedback360.service;

import com.batuhan.feedback360.config.AuthenticationPrincipalResolver;
import com.batuhan.feedback360.model.converter.CompetencyConverter;
import com.batuhan.feedback360.model.converter.CompetencyEvaluatorPermissionConverter;
import com.batuhan.feedback360.model.entitiy.Company;
import com.batuhan.feedback360.model.entitiy.Competency;
import com.batuhan.feedback360.model.entitiy.CompetencyEvaluatorPermission;
import com.batuhan.feedback360.model.entitiy.EvaluationPeriod;
import com.batuhan.feedback360.model.entitiy.Evaluator;
import com.batuhan.feedback360.model.entitiy.PeriodCompetencyWeight;
import com.batuhan.feedback360.model.request.AssignCompetencyEvaluatorsRequest;
import com.batuhan.feedback360.model.request.CompetencyEvaluatorWeight;
import com.batuhan.feedback360.model.request.CompetencyRequest;
import com.batuhan.feedback360.model.request.CompetencyWeightRequest;
import com.batuhan.feedback360.model.request.SetCompetencyEvaluatorWeightsRequest;
import com.batuhan.feedback360.model.request.SetCompetencyWeightsRequest;
import com.batuhan.feedback360.model.response.ApiResponse;
import com.batuhan.feedback360.model.response.CompetencyEvaluatorPermissionResponse;
import com.batuhan.feedback360.model.response.CompetencyResponse;
import com.batuhan.feedback360.model.response.CompetencyWeightResponse;
import com.batuhan.feedback360.repository.CompanyRepository;
import com.batuhan.feedback360.repository.CompetencyEvaluatorPermissionRepository;
import com.batuhan.feedback360.repository.CompetencyRepository;
import com.batuhan.feedback360.repository.EvaluationPeriodRepository;
import com.batuhan.feedback360.repository.EvaluatorRepository;
import com.batuhan.feedback360.repository.PeriodCompetencyWeightRepository;
import com.batuhan.feedback360.repository.QuestionRepository;
import com.batuhan.feedback360.util.MessageHandler;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
public class PeriodCompetencyService {

    private final CompanyRepository companyRepository;
    private final EvaluationPeriodRepository evaluationPeriodRepository;
    private final QuestionRepository questionRepository;
    private final CompetencyRepository competencyRepository;
    private final CompetencyEvaluatorPermissionRepository competencyEvaluatorPermissionRepository;
    private final CompetencyConverter competencyConverter;
    private final CompetencyEvaluatorPermissionConverter competencyEvaluatorPermissionConverter;
    private final MessageHandler messageHandler;
    private final AuthenticationPrincipalResolver principalResolver;
    private final PeriodCompetencyWeightRepository periodCompetencyWeightRepository;
    private final EvaluatorRepository evaluatorRepository;

    @Transactional
    public ApiResponse<CompetencyResponse> addCompetencyToPeriod(Integer periodId, CompetencyRequest request) {
        Company company = companyRepository.getReferenceById(principalResolver.getCompanyId());

        Optional<EvaluationPeriod> periodOptional = evaluationPeriodRepository.findByIdAndCompany(periodId, company);
        if (periodOptional.isEmpty()) {
            return ApiResponse.failure(messageHandler.getMessage("evaluation-period.not-found"));
        }
        EvaluationPeriod period = periodOptional.get();

        Competency competency = Competency.builder()
            .title(request.getTitle())
            .description(request.getDescription())
            .company(period.getCompany())
            .build();
        Competency savedCompetency = competencyRepository.save(competency);

        PeriodCompetencyWeight periodCompetencyWeight = PeriodCompetencyWeight.builder()
            .period(period)
            .competency(savedCompetency)
            .weight(BigDecimal.ZERO)
            .build();

        periodCompetencyWeightRepository.save(periodCompetencyWeight);

        List<Evaluator> evaluators = evaluatorRepository.findAllByPeriod_Id(periodId);
        if (!evaluators.isEmpty()) {
            BigDecimal equalWeight = new BigDecimal("100.00").divide(
                new BigDecimal(evaluators.size()),
                2,
                RoundingMode.HALF_UP
            );

            List<CompetencyEvaluatorPermission> permissions = evaluators.stream()
                .map(evaluator -> CompetencyEvaluatorPermission.builder()
                    .period(period)
                    .competency(savedCompetency)
                    .evaluator(evaluator)
                    .weight(equalWeight)
                    .build())
                .collect(Collectors.toList());

            BigDecimal totalWeight = permissions.stream()
                .map(CompetencyEvaluatorPermission::getWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal difference = new BigDecimal("100.00").subtract(totalWeight);

            if (difference.compareTo(BigDecimal.ZERO) != 0) {
                CompetencyEvaluatorPermission lastPermission = permissions.get(permissions.size() - 1);
                lastPermission.setWeight(lastPermission.getWeight().add(difference));
            }

            competencyEvaluatorPermissionRepository.saveAll(permissions);
        }

        return ApiResponse.success(
            competencyConverter.toCompetencyResponse(savedCompetency),
            messageHandler.getMessage("competency.add.success")
        );
    }

    public ApiResponse<List<CompetencyResponse>> getCompetenciesByPeriod(Integer periodId) {
        if (!evaluationPeriodRepository.existsByIdAndCompanyId(periodId, principalResolver.getCompanyId())) {
            return ApiResponse.failure(messageHandler.getMessage("evaluation-period.not-found"));
        }
        List<PeriodCompetencyWeight> periodCompetencyWeights = periodCompetencyWeightRepository.findAllByPeriod_Id(periodId);
        return ApiResponse.success(
            periodCompetencyWeights.stream().map(p -> competencyConverter.toCompetencyResponse(p.getCompetency())).toList(),
            messageHandler.getMessage("competency.list.success")
        );
    }

    public ApiResponse<CompetencyResponse> getCompetencyById(Integer periodId, Integer competencyId) {
        if (!evaluationPeriodRepository.existsByIdAndCompanyId(periodId, principalResolver.getCompanyId())) {
            return ApiResponse.failure(messageHandler.getMessage("evaluation-period.not-found"));
        }

        Optional<PeriodCompetencyWeight> periodCompetencyWeight =
            periodCompetencyWeightRepository.findByPeriod_IdAndCompetency_Id(periodId, competencyId);
        if (periodCompetencyWeight.isEmpty()) {
            return ApiResponse.failure(messageHandler.getMessage("competency.not-found"));
        }

        return ApiResponse.success(
            competencyConverter.toCompetencyResponse(periodCompetencyWeight.get().getCompetency()),
            messageHandler.getMessage("competency.get.success")
        );
    }

    @Transactional
    public ApiResponse<?> deleteCompetencyFromPeriod(Integer periodId, Integer competencyId) {
        Optional<Competency> competencyOpt = validatePeriodAndCompetency(periodId, competencyId);
        if (competencyOpt.isEmpty()) {
            return ApiResponse.failure(messageHandler.getMessage("competency.not-found-in-period", competencyId));
        }
        Competency competency = competencyOpt.get();

        questionRepository.deleteByCompetency_Id(competency.getId());
        competencyEvaluatorPermissionRepository.deleteByPeriod_IdAndCompetency_Id(periodId, competency.getId());
        periodCompetencyWeightRepository.deleteByPeriod_IdAndCompetency_Id(periodId, competency.getId());
        competencyRepository.delete(competency);

        return ApiResponse.success(null, messageHandler.getMessage("competency.delete.success"));
    }

    @Transactional
    public ApiResponse<List<CompetencyEvaluatorPermissionResponse>> assignEvaluatorsToCompetency(Integer periodId, Integer competencyId, AssignCompetencyEvaluatorsRequest request) {
        EvaluationPeriod period = evaluationPeriodRepository.findByIdAndCompanyId(periodId, principalResolver.getCompanyId())
            .orElse(null);
        if (period == null) {
            return ApiResponse.failure(messageHandler.getMessage("evaluation-period.no-permission-update"));
        }

        Competency competency = validatePeriodAndCompetency(periodId, competencyId).orElse(null);
        if (competency == null) {
            return ApiResponse.failure(messageHandler.getMessage("competency.not-found-in-period", competencyId));
        }

        competencyEvaluatorPermissionRepository.deleteByPeriod_IdAndCompetency_Id(period.getId(), competency.getId());
        competencyEvaluatorPermissionRepository.flush();

        List<Evaluator> validEvaluators = evaluatorRepository.findAllByIdInAndPeriod_Id(request.getEvaluatorIds(), periodId);
        if (validEvaluators.size() != request.getEvaluatorIds().size()) {
            return ApiResponse.failure(messageHandler.getMessage("assignment.invalid-entities"));
        }

        List<CompetencyEvaluatorPermission> newPermissions = validEvaluators.stream()
            .map(evaluator -> CompetencyEvaluatorPermission.builder()
                .period(period)
                .competency(competency)
                .evaluator(evaluator)
                .weight(BigDecimal.ZERO)
                .build())
            .toList();

        List<CompetencyEvaluatorPermission> savedPermissions = competencyEvaluatorPermissionRepository.saveAll(newPermissions);

        return ApiResponse.success(
            savedPermissions.stream().map(competencyEvaluatorPermissionConverter::toResponse).toList(),
            messageHandler.getMessage("competency.evaluator.assign.success")
        );
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

    @Transactional
    public ApiResponse<List<CompetencyWeightResponse>> setCompetencyWeights(Integer periodId, SetCompetencyWeightsRequest request) {
        if (!evaluationPeriodRepository.existsByIdAndCompanyId(periodId, principalResolver.getCompanyId())) {
            return ApiResponse.failure(messageHandler.getMessage("evaluation-period.no-permission-update"));
        }
        BigDecimal totalWeight = request.getCompetencyWeights().stream()
            .map(CompetencyWeightRequest::getWeight)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalWeight.compareTo(new BigDecimal("100")) != 0) {
            return ApiResponse.failure(messageHandler.getMessage("competency.weight.sum.invalid"));
        }
        List<PeriodCompetencyWeight> existingPeriodWeights = periodCompetencyWeightRepository.findAllByPeriod_Id(periodId);
        Map<Integer, PeriodCompetencyWeight> existingWeightsMap = existingPeriodWeights.stream()
            .collect(Collectors.toMap(pcw -> pcw.getCompetency().getId(), pcw -> pcw));

        if (request.getCompetencyWeights().size() != existingWeightsMap.size()) {
            return ApiResponse.failure(messageHandler.getMessage("competency.weight.count.mismatch"));
        }
        for (CompetencyWeightRequest reqWeight : request.getCompetencyWeights()) {
            if (!existingWeightsMap.containsKey(reqWeight.getCompetencyId())) {
                return ApiResponse.failure(messageHandler.getMessage("competency.not-found-in-period", reqWeight.getCompetencyId()));
            }
        }
        request.getCompetencyWeights().forEach(reqWeight -> {
            PeriodCompetencyWeight pcwToUpdate = existingWeightsMap.get(reqWeight.getCompetencyId());
            pcwToUpdate.setWeight(reqWeight.getWeight());
        });

        List<PeriodCompetencyWeight> updatedPeriodWeights = periodCompetencyWeightRepository.saveAll(existingWeightsMap.values());
        List<CompetencyWeightResponse> response = updatedPeriodWeights.stream()
            .map(pcw -> CompetencyWeightResponse.builder()
                .competencyId(pcw.getCompetency().getId())
                .competencyTitle(pcw.getCompetency().getTitle())
                .weight(pcw.getWeight())
                .build()
            ).toList();

        return ApiResponse.success(response, messageHandler.getMessage("competency.weight.update.success"));
    }

    private Optional<Competency> validatePeriodAndCompetency(Integer periodId, Integer competencyId) {
        if (!evaluationPeriodRepository.existsByIdAndCompanyId(periodId, principalResolver.getCompanyId())) {
            return Optional.empty();
        }
        return periodCompetencyWeightRepository.findByPeriod_IdAndCompetency_Id(periodId, competencyId)
            .map(PeriodCompetencyWeight::getCompetency);
    }
}
