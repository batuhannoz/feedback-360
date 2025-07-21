package com.batuhan.feedback360.service;

import com.batuhan.feedback360.config.AuthenticationPrincipalResolver;
import com.batuhan.feedback360.model.converter.AssignmentConverter;
import com.batuhan.feedback360.model.entitiy.Company;
import com.batuhan.feedback360.model.entitiy.EvaluationAssignment;
import com.batuhan.feedback360.model.entitiy.Evaluator;
import com.batuhan.feedback360.model.entitiy.PeriodParticipant;
import com.batuhan.feedback360.model.entitiy.User;
import com.batuhan.feedback360.model.request.AssignEvaluatorsRequest;
import com.batuhan.feedback360.model.request.AssignmentDetail;
import com.batuhan.feedback360.model.response.ApiResponse;
import com.batuhan.feedback360.model.response.AssignmentResponse;
import com.batuhan.feedback360.repository.CompanyRepository;
import com.batuhan.feedback360.repository.EvaluationAssignmentRepository;
import com.batuhan.feedback360.repository.EvaluatorRepository;
import com.batuhan.feedback360.repository.PeriodParticipantRepository;
import com.batuhan.feedback360.repository.UserRepository;
import com.batuhan.feedback360.util.MessageHandler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
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
public class ParticipantAssignmentService {

    private final EvaluationAssignmentRepository evaluationAssignmentRepository;
    private final UserRepository userRepository;
    private final EvaluatorRepository evaluatorRepository;
    private final CompanyRepository companyRepository;
    private final AuthenticationPrincipalResolver principalResolver;
    private final AssignmentConverter assignmentConverter;
    private final MessageHandler messageHandler;
    private final PeriodParticipantService periodParticipantService;
    private final PeriodParticipantRepository periodParticipantRepository;

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
    public ApiResponse<List<AssignmentResponse>> assignEvaluatorsToPeriodParticipant(Integer periodId, Integer evaluatedUserId, AssignEvaluatorsRequest request) {
        Company company = companyRepository.getReferenceById(principalResolver.getCompanyId());
        Optional<PeriodParticipant> participant = periodParticipantRepository.findByPeriod_IdAndEvaluatedUser_Id(periodId, evaluatedUserId);
        if (participant.isEmpty() || !participant.get().getPeriod().getCompany().equals(company)) {
            return ApiResponse.failure(messageHandler.getMessage("period.participant.not-found"));
        }
        evaluationAssignmentRepository.deleteAllByPeriodParticipant(participant.get());

        List<AssignmentDetail> assignments = request.getAssignments();
        if (assignments == null || assignments.isEmpty()) {
            return ApiResponse.success(Collections.emptyList(), messageHandler.getMessage("assignment.success"));
        }

        List<Integer> allEvaluatorUserIds = assignments.stream()
            .map(AssignmentDetail::getEvaluatorUserId)
            .toList();

        Set<Integer> uniqueEvaluatorUserIds = new HashSet<>(allEvaluatorUserIds);

        if (allEvaluatorUserIds.size() != uniqueEvaluatorUserIds.size()) {
            return ApiResponse.failure(messageHandler.getMessage("assignment.duplicate-evaluator"));
        }

        List<Integer> requestedEvaluatorIds = assignments.stream().map(AssignmentDetail::getEvaluatorId).distinct().toList();

        List<User> validUsers = userRepository.findAllByIdInAndCompany(new ArrayList<>(uniqueEvaluatorUserIds), company);
        List<Evaluator> validEvaluators = evaluatorRepository.findAllByIdInAndPeriod_Id(requestedEvaluatorIds, periodId);

        if (validUsers.size() != uniqueEvaluatorUserIds.size() || validEvaluators.size() != requestedEvaluatorIds.size()) {
            return ApiResponse.failure(messageHandler.getMessage("assignment.invalid-entities"));
        }

        Map<Integer, User> userMap = validUsers.stream().collect(Collectors.toMap(User::getId, u -> u));
        Map<Integer, Evaluator> evaluatorMap = validEvaluators.stream().collect(Collectors.toMap(Evaluator::getId, e -> e));

        List<EvaluationAssignment> newAssignments = assignments.stream()
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
}
