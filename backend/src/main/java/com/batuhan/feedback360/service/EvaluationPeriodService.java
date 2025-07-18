package com.batuhan.feedback360.service;

import com.batuhan.feedback360.config.AuthenticationPrincipalResolver;
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
import com.batuhan.feedback360.model.entitiy.EvaluationAssignment;
import com.batuhan.feedback360.model.entitiy.EvaluationPeriod;
import com.batuhan.feedback360.model.entitiy.Evaluator;
import com.batuhan.feedback360.model.entitiy.PeriodCompetencyWeight;
import com.batuhan.feedback360.model.entitiy.PeriodParticipant;
import com.batuhan.feedback360.model.entitiy.Question;
import com.batuhan.feedback360.model.entitiy.User;
import com.batuhan.feedback360.model.enums.EvaluatorType;
import com.batuhan.feedback360.model.enums.PeriodStatus;
import com.batuhan.feedback360.model.request.AssignCompetencyEvaluatorsRequest;
import com.batuhan.feedback360.model.request.AssignEvaluatorsRequest;
import com.batuhan.feedback360.model.request.AssignmentDetail;
import com.batuhan.feedback360.model.request.CompetencyEvaluatorWeight;
import com.batuhan.feedback360.model.request.CompetencyRequest;
import com.batuhan.feedback360.model.request.CompetencyWeightRequest;
import com.batuhan.feedback360.model.request.EvaluationPeriodRequest;
import com.batuhan.feedback360.model.request.EvaluatorRequest;
import com.batuhan.feedback360.model.request.QuestionRequest;
import com.batuhan.feedback360.model.request.SetCompetencyEvaluatorWeightsRequest;
import com.batuhan.feedback360.model.request.SetCompetencyWeightsRequest;
import com.batuhan.feedback360.model.response.ApiResponse;
import com.batuhan.feedback360.model.response.AssignmentResponse;
import com.batuhan.feedback360.model.response.CompetencyEvaluatorPermissionResponse;
import com.batuhan.feedback360.model.response.CompetencyResponse;
import com.batuhan.feedback360.model.response.CompetencyWeightResponse;
import com.batuhan.feedback360.model.response.EvaluationPeriodResponse;
import com.batuhan.feedback360.model.response.EvaluatorResponse;
import com.batuhan.feedback360.model.response.ParticipantResponse;
import com.batuhan.feedback360.model.response.QuestionResponse;
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
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
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

    public ApiResponse<List<ParticipantResponse>> getParticipantsByPeriod(Integer periodId) {
        if (!evaluationPeriodRepository.existsByIdAndCompanyId(periodId, principalResolver.getCompanyId())) {
            return ApiResponse.failure(messageHandler.getMessage("evaluation-period.not-found"));
        }
        List<PeriodParticipant> participants = periodParticipantRepository.findAllByPeriod_Id(periodId);

        List<ParticipantResponse> response = participants.stream()
            .map(PeriodParticipant::getEvaluatedUser)
            .map(userConverter::toParticipantResponse)
            .collect(Collectors.toList());

        return ApiResponse.success(response, messageHandler.getMessage("evaluation-period.participants.get.success"));
    }

    public ApiResponse<ParticipantResponse> addParticipantToPeriod(Integer periodId, Integer userId) {
        Company company = companyRepository.getReferenceById(principalResolver.getCompanyId());

        Optional<EvaluationPeriod> period = evaluationPeriodRepository.findByIdAndCompany(periodId, company);
        if (period.isEmpty()) {
            return ApiResponse.failure(messageHandler.getMessage("evaluation-period.not-found"));
        }

        Optional<User> user = userRepository.findByIdAndCompany(userId, company);
        if (user.isEmpty()) {
            return ApiResponse.failure(messageHandler.getMessage("user.not-found"));
        }

        if (periodParticipantRepository.existsByPeriod_IdAndEvaluatedUser_Id(periodId, userId)) {
            return ApiResponse.failure(messageHandler.getMessage("evaluation-period.participant.already-exists"));
        }

        PeriodParticipant newParticipant = PeriodParticipant.builder()
            .period(period.get())
            .evaluatedUser(user.get())
            .build();

        periodParticipantRepository.save(newParticipant);

        ParticipantResponse response = userConverter.toParticipantResponse(user.get());
        return ApiResponse.success(response, messageHandler.getMessage("evaluation-period.participant.add.success"));
    }

    @Transactional
    public ApiResponse<ParticipantResponse> deleteParticipantToPeriod(Integer periodId, Integer userId) {
        Company company = companyRepository.getReferenceById(principalResolver.getCompanyId());
        Optional<PeriodParticipant> participant = periodParticipantRepository.findByPeriod_IdAndEvaluatedUser_Id(periodId, userId);
        if (participant.isEmpty() || !participant.get().getPeriod().getCompany().equals(company)) {
            return ApiResponse.failure(messageHandler.getMessage("period.participant.not-found"));
        }
        evaluationAssignmentRepository.deleteAllByPeriodParticipant(participant.get());
        periodParticipantRepository.delete(participant.get());
        ParticipantResponse response = userConverter.toParticipantResponse(participant.get().getEvaluatedUser());
        return ApiResponse.success(response, messageHandler.getMessage("evaluation-period.participant.delete.success"));
    }

    @Transactional
    public ApiResponse<List<AssignmentResponse>> assignEvaluatorsToPeriodParticipant(Integer periodId, Integer evaluatedUserId, AssignEvaluatorsRequest request) {
        Company company = companyRepository.getReferenceById(principalResolver.getCompanyId());
        Optional<PeriodParticipant> participant = periodParticipantRepository.findByPeriod_IdAndEvaluatedUser_Id(periodId, evaluatedUserId);
        if (participant.isEmpty() || !participant.get().getPeriod().getCompany().equals(company)) {
            return ApiResponse.failure(messageHandler.getMessage("period.participant.not-found"));
        }
        evaluationAssignmentRepository.deleteAllByPeriodParticipant(participant.get());

        if (request.getAssignments() == null || request.getAssignments().isEmpty()) {
            return ApiResponse.success(Collections.emptyList(), messageHandler.getMessage("assignment.success"));
        }

        List<Integer> requestedUserIds = request.getAssignments().stream().map(AssignmentDetail::getEvaluatorUserId).distinct().toList();
        List<Integer> requestedEvaluatorIds = request.getAssignments().stream().map(AssignmentDetail::getEvaluatorId).distinct().toList();

        List<User> validUsers = userRepository.findAllByIdInAndCompany(requestedUserIds, company);
        List<Evaluator> validEvaluators = evaluatorRepository.findAllByIdInAndPeriod_Id(requestedEvaluatorIds, periodId);

        if (validUsers.size() != requestedUserIds.size() || validEvaluators.size() != requestedEvaluatorIds.size()) {
            return ApiResponse.failure(messageHandler.getMessage("assignment.invalid-entities"));
        }

        Map<Integer, User> userMap = validUsers.stream().collect(Collectors.toMap(User::getId, u -> u));
        Map<Integer, Evaluator> evaluatorMap = validEvaluators.stream().collect(Collectors.toMap(Evaluator::getId, e -> e));

        List<EvaluationAssignment> newAssignments = request.getAssignments().stream()
            .map(assignmentDetail -> EvaluationAssignment.builder()
                .periodParticipant(participant.get())
                .evaluatorUser(userMap.get(assignmentDetail.getEvaluatorUserId()))
                .evaluator(evaluatorMap.get(assignmentDetail.getEvaluatorId()))
                .build())
            .toList();

        List<EvaluationAssignment> savedAssignments = evaluationAssignmentRepository.saveAll(newAssignments);

        List<AssignmentResponse> response = savedAssignments.stream()
            .map(assignmentConverter::toAssignmentResponse)
            .collect(Collectors.toList());

        return ApiResponse.success(response, messageHandler.getMessage("assignment.success"));
    }

    public ApiResponse<List<AssignmentResponse>> getEvaluatorsForPeriodAssignment(Integer periodId, Integer evaluatedUserId) {
        Company company = companyRepository.getReferenceById(principalResolver.getCompanyId());
        Optional<PeriodParticipant> participant = periodParticipantRepository.findByPeriod_IdAndEvaluatedUser_Id(periodId, evaluatedUserId);
        if (participant.isEmpty() || !participant.get().getPeriod().getCompany().equals(company)) {
            return ApiResponse.failure(messageHandler.getMessage("period.participant.not-found"));
        }
        List<EvaluationAssignment> assignments = evaluationAssignmentRepository.findAllByPeriodParticipant(participant.get());

        List<AssignmentResponse> response = assignments.stream()
            .map(assignmentConverter::toAssignmentResponse)
            .collect(Collectors.toList());
        return ApiResponse.success(response, messageHandler.getMessage("assignment.get.success"));
    }

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
    public ApiResponse<List<CompetencyEvaluatorPermissionResponse>> assignEvaluatorsToCompetency(Integer periodId, Integer competencyId, AssignCompetencyEvaluatorsRequest request) {
        EvaluationPeriod period = evaluationPeriodRepository.findByIdAndCompanyId(periodId, principalResolver.getCompanyId())
            .orElse(null);
        if (period == null) {
            return ApiResponse.failure(messageHandler.getMessage("evaluation-period.no-permission-update"));
        }

        Competency competency = validatePeriodAndCompetency(periodId, competencyId)
            .orElse(null);
        if (competency == null) {
            return ApiResponse.failure(messageHandler.getMessage("competency.not-found-in-period", competencyId));
        }

        competencyEvaluatorPermissionRepository.deleteByPeriod_IdAndCompetency_Id(periodId, competencyId);

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

        List<CompetencyEvaluatorPermissionResponse> response = savedPermissions.stream()
            .map(competencyEvaluatorPermissionConverter::toResponse)
            .collect(Collectors.toList());

        return ApiResponse.success(response, messageHandler.getMessage("competency.evaluator.assign.success"));
    }

    @Transactional
    public ApiResponse<List<CompetencyEvaluatorPermissionResponse>> setCompetencyEvaluatorWeights(Integer periodId, Integer competencyId, SetCompetencyEvaluatorWeightsRequest request) {
        if (validatePeriodAndCompetency(periodId, competencyId).isEmpty()) {
            return ApiResponse.failure(messageHandler.getMessage("competency.not-found-in-period", competencyId));
        }

        List<CompetencyEvaluatorPermission> permissions = competencyEvaluatorPermissionRepository.findByPeriod_IdAndCompetency_Id(periodId, competencyId);
        if (permissions.isEmpty()) {
            return ApiResponse.failure(messageHandler.getMessage("competency.evaluator.not-assigned"));
        }

        BigDecimal totalWeight = request.getWeights().stream()
            .map(CompetencyEvaluatorWeight::getWeight)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

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
            .map(pcw -> CompetencyWeightResponse.builder().competencyId(pcw.getCompetency().getId()).competencyTitle(pcw.getCompetency().getTitle()).weight(pcw.getWeight()).build())
            .collect(Collectors.toList());

        return ApiResponse.success(response, messageHandler.getMessage("competency.weight.update.success"));
    }

    public ApiResponse<List<QuestionResponse>> getQuestionsForCompetency(Integer periodId, Integer competencyId) {
        Optional<Competency> competencyOpt = validatePeriodAndCompetency(periodId, competencyId);
        if (competencyOpt.isEmpty()) {
            return ApiResponse.failure(messageHandler.getMessage("competency.not-found-in-period", competencyId));
        }

        List<Question> questions = questionRepository.findByCompetency_Id(competencyId);
        List<QuestionResponse> response = questions.stream()
            .map(questionConverter::toQuestionResponse)
            .collect(Collectors.toList());

        return ApiResponse.success(response, messageHandler.getMessage("question.list.success"));
    }

    public ApiResponse<QuestionResponse> addQuestionToCompetency(Integer periodId, Integer competencyId, QuestionRequest request) {
        Optional<Competency> competencyOpt = validatePeriodAndCompetency(periodId, competencyId);
        if (competencyOpt.isEmpty()) {
            return ApiResponse.failure(messageHandler.getMessage("competency.not-found-in-period", competencyId));
        }
        Competency competency = competencyOpt.get();

        Question newQuestion = Question.builder()
            .questionText(request.getQuestionText())
            .competency(competency)
            .company(competency.getCompany())
            .build();

        Question savedQuestion = questionRepository.save(newQuestion);
        return ApiResponse.success(
            questionConverter.toQuestionResponse(savedQuestion),
            messageHandler.getMessage("question.add.success")
        );
    }

    @Transactional
    public ApiResponse<QuestionResponse> updateQuestion(Integer periodId, Integer competencyId, Integer questionId, QuestionRequest request) {
        if (validatePeriodAndCompetency(periodId, competencyId).isEmpty()) {
            return ApiResponse.failure(messageHandler.getMessage("competency.not-found-in-period", competencyId));
        }
        return questionRepository.findByIdAndCompetency_Id(questionId, competencyId)
            .map(questionToUpdate -> {
                questionToUpdate.setQuestionText(request.getQuestionText());
                Question updatedQuestion = questionRepository.save(questionToUpdate);
                return ApiResponse.success(
                    questionConverter.toQuestionResponse(updatedQuestion),
                    messageHandler.getMessage("question.update.success")
                );
            })
            .orElse(ApiResponse.failure(messageHandler.getMessage("question.not-found")));
    }

    @Transactional
    public ApiResponse<Object> deleteQuestion(Integer periodId, Integer competencyId, Integer questionId) {
        if (validatePeriodAndCompetency(periodId, competencyId).isEmpty()) {
            return ApiResponse.failure(messageHandler.getMessage("competency.not-found-in-period", competencyId));
        }

        return questionRepository.findByIdAndCompetency_Id(questionId, competencyId)
            .map(question -> {
                questionRepository.delete(question);
                return ApiResponse.success(null, messageHandler.getMessage("question.delete.success"));
            })
            .orElse(ApiResponse.failure(messageHandler.getMessage("question.not-found")));
    }

    private Optional<Competency> validatePeriodAndCompetency(Integer periodId, Integer competencyId) {
        if (!evaluationPeriodRepository.existsByIdAndCompanyId(periodId, principalResolver.getCompanyId())) {
            return Optional.empty();
        }
        return periodCompetencyWeightRepository.findByPeriod_IdAndCompetency_Id(periodId, competencyId)
            .map(PeriodCompetencyWeight::getCompetency);
    }
}
