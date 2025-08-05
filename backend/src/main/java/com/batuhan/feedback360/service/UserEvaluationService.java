package com.batuhan.feedback360.service;

import com.batuhan.feedback360.config.AuthenticationPrincipalResolver;
import com.batuhan.feedback360.model.converter.AnswerConverter;
import com.batuhan.feedback360.model.converter.EvaluationPeriodConverter;
import com.batuhan.feedback360.model.converter.QuestionConverter;
import com.batuhan.feedback360.model.converter.UserConverter;
import com.batuhan.feedback360.model.entitiy.Answer;
import com.batuhan.feedback360.model.entitiy.Company;
import com.batuhan.feedback360.model.entitiy.CompetencyEvaluatorPermission;
import com.batuhan.feedback360.model.entitiy.EvaluationAssignment;
import com.batuhan.feedback360.model.entitiy.EvaluationPeriod;
import com.batuhan.feedback360.model.entitiy.PeriodParticipant;
import com.batuhan.feedback360.model.entitiy.Question;
import com.batuhan.feedback360.model.enums.PeriodStatus;
import com.batuhan.feedback360.model.request.AnswerSubmissionRequest;
import com.batuhan.feedback360.model.response.AnswerResponse;
import com.batuhan.feedback360.model.response.ApiResponse;
import com.batuhan.feedback360.model.response.EvaluationPeriodResponse;
import com.batuhan.feedback360.model.response.QuestionResponse;
import com.batuhan.feedback360.model.response.UserDetailResponse;
import com.batuhan.feedback360.repository.AnswerRepository;
import com.batuhan.feedback360.repository.CompanyRepository;
import com.batuhan.feedback360.repository.CompetencyEvaluatorPermissionRepository;
import com.batuhan.feedback360.repository.EvaluationAssignmentRepository;
import com.batuhan.feedback360.repository.EvaluationPeriodRepository;
import com.batuhan.feedback360.repository.PeriodParticipantRepository;
import com.batuhan.feedback360.repository.QuestionRepository;
import com.batuhan.feedback360.repository.UserRepository;
import com.batuhan.feedback360.util.MessageHandler;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class UserEvaluationService {

    private final UserRepository userRepository;
    private final MessageHandler messageHandler;
    private final AuthenticationPrincipalResolver principalResolver;
    private final CompanyRepository companyRepository;
    private final UserConverter userConverter;
    private final EvaluationPeriodConverter evaluationPeriodConverter;
    private final PeriodParticipantRepository periodParticipantRepository;
    private final EvaluationAssignmentRepository evaluationAssignmentRepository;
    private final EvaluationPeriodRepository evaluationPeriodRepository;
    private final QuestionConverter questionConverter;
    private final AnswerRepository answerRepository;
    private final AnswerConverter answerConverter;
    private final CompetencyEvaluatorPermissionRepository competencyEvaluatorPermissionRepository;
    private final QuestionRepository questionRepository;

    public ApiResponse<List<EvaluationPeriodResponse>> getEvaluationPeriodsForUser() {
        Company company = companyRepository.getReferenceById(principalResolver.getCompanyId());
        if (userRepository.findByIdAndCompany(principalResolver.getUserId(), company).isEmpty()) {
            return ApiResponse.failure(messageHandler.getMessage("user.not-found"));
        }

        List<PeriodParticipant> participations = periodParticipantRepository.findAllByEvaluatedUser_Id(principalResolver.getUserId());

        List<EvaluationPeriodResponse> response = participations.stream()
            .map(PeriodParticipant::getPeriod)
            .filter(period -> period.getStatus() == PeriodStatus.IN_PROGRESS)
            .sorted(Comparator.comparing(EvaluationPeriod::getStartDate).reversed())
            .map(evaluationPeriodConverter::toEvaluationPeriodResponse)
            .collect(Collectors.toList());

        return ApiResponse.success(response, messageHandler.getMessage("user.evaluation-periods.get.success"));
    }

    public ApiResponse<List<UserDetailResponse>> getEvaluationsForUserPeriod(Integer periodId) {
        Integer evaluatorUserId = principalResolver.getUserId();
        Optional<EvaluationPeriod> periodOpt = evaluationPeriodRepository.findByIdAndCompanyId(periodId, principalResolver.getCompanyId());
        if (periodOpt.isEmpty() || periodOpt.get().getStatus() != PeriodStatus.IN_PROGRESS) {
            return ApiResponse.failure(messageHandler.getMessage("evaluation.period.not-in-progress"));
        }

        List<EvaluationAssignment> assignments = evaluationAssignmentRepository.findAllByEvaluatorUser_IdAndPeriodParticipant_Period_Id(evaluatorUserId, periodId);

        if (assignments.isEmpty()) {
            return ApiResponse.success(Collections.emptyList(), messageHandler.getMessage("user.tasks.get.no-tasks"));
        }

        List<UserDetailResponse> unevaluatedUsers = assignments.stream()
            .map(assignment -> assignment.getPeriodParticipant().getEvaluatedUser())
            .distinct()
            .filter(evaluatedUser -> !hasEvaluationBeenSubmitted(evaluatorUserId, evaluatedUser.getId(), periodId))
            .map(userConverter::toUserDetailResponse)
            .sorted(Comparator.comparing(UserDetailResponse::getFirstName))
            .toList();

        return ApiResponse.success(unevaluatedUsers, messageHandler.getMessage("user.tasks.users.get.success"));
    }

    public ApiResponse<List<QuestionResponse>> getQuestionsForEvaluatedUser(Integer periodId, Integer evaluatedUserId) {
        Integer evaluatorUserId = principalResolver.getUserId();
        Optional<ApiResponse<List<QuestionResponse>>> validationResponse = validateEvaluationState(periodId, evaluatorUserId, evaluatedUserId);
        if (validationResponse.isPresent()) {
            return validationResponse.get();
        }

        List<Question> permittedQuestions = getPermittedQuestions(periodId, evaluatorUserId, evaluatedUserId);
        if (permittedQuestions.isEmpty()) {
            return ApiResponse.success(Collections.emptyList(), messageHandler.getMessage("user.tasks.get.success"));
        }

        List<QuestionResponse> questionResponses = permittedQuestions.stream()
            .map(questionConverter::toQuestionResponse)
            .toList();

        return ApiResponse.success(questionResponses, messageHandler.getMessage("user.tasks.get.success"));
    }

    @Transactional
    public ApiResponse<List<AnswerResponse>> submitAnswersForEvaluatedUser(
        Integer periodId,
        Integer evaluatedUserId,
        List<AnswerSubmissionRequest> answerRequests
    ) {
        Integer evaluatorUserId = principalResolver.getUserId();

        Optional<ApiResponse<List<AnswerResponse>>> prerequisiteValidation = validateSubmissionPrerequisites(periodId, evaluatedUserId, evaluatorUserId);
        if (prerequisiteValidation.isPresent()) {
            return prerequisiteValidation.get();
        }

        List<EvaluationAssignment> assignments = evaluationAssignmentRepository.findAllByEvaluatorUser_IdAndPeriodParticipant_Period_IdAndPeriodParticipant_EvaluatedUser_Id(
            evaluatorUserId, periodId, evaluatedUserId);
        if (assignments.isEmpty()) {
            return ApiResponse.failure(messageHandler.getMessage("evaluation.assignment.not-found"));
        }

        List<Question> permittedQuestions = getPermittedQuestions(periodId, evaluatorUserId, evaluatedUserId);
        Map<Integer, Question> questionMap = permittedQuestions.stream().collect(Collectors.toMap(Question::getId, q -> q));

        Optional<ApiResponse<List<AnswerResponse>>> requestValidation = validateAnswerRequests(answerRequests, questionMap);
        if (requestValidation.isPresent()) {
            return requestValidation.get();
        }

        List<Answer> newAnswersToSave = createAnswersFromRequests(answerRequests, assignments, questionMap, periodId);

        List<Answer> savedAnswers = answerRepository.saveAll(newAnswersToSave);
        List<AnswerResponse> response = savedAnswers.stream()
            .map(answerConverter::toAnswerResponse)
            .collect(Collectors.toList());

        return ApiResponse.success(response, messageHandler.getMessage("evaluation.submit.success"));
    }

    private boolean hasEvaluationBeenSubmitted(Integer evaluatorUserId, Integer evaluatedUserId, Integer periodId) {
        return answerRepository.existsByAssignment_EvaluatorUser_IdAndAssignment_PeriodParticipant_EvaluatedUser_IdAndAssignment_PeriodParticipant_Period_Id(
            evaluatorUserId, evaluatedUserId, periodId
        );
    }

    private Optional<ApiResponse<List<QuestionResponse>>> validateEvaluationState(Integer periodId, Integer evaluatorUserId, Integer evaluatedUserId) {
        Optional<EvaluationPeriod> periodOpt = evaluationPeriodRepository.findByIdAndCompanyId(periodId, principalResolver.getCompanyId());
        if (periodOpt.isEmpty() || periodOpt.get().getStatus() != PeriodStatus.IN_PROGRESS) {
            return Optional.of(ApiResponse.failure(messageHandler.getMessage("evaluation.period.not-in-progress")));
        }
        if (validateUserIsAssignedToEvaluate(periodId, evaluatorUserId, evaluatedUserId).isEmpty()) {
            return Optional.of(ApiResponse.failure(messageHandler.getMessage("user.tasks.permission.denied")));
        }
        if (hasEvaluationBeenSubmitted(evaluatorUserId, evaluatedUserId, periodId)) {
            return Optional.of(ApiResponse.failure(messageHandler.getMessage("evaluation.already.submitted")));
        }
        return Optional.empty();
    }

    private Optional<ApiResponse<List<AnswerResponse>>> validateSubmissionPrerequisites(Integer periodId, Integer evaluatedUserId, Integer evaluatorUserId) {
        EvaluationPeriod period = evaluationPeriodRepository.findByIdAndCompanyId(periodId, principalResolver.getCompanyId()).orElse(null);
        if (period == null || period.getStatus() != PeriodStatus.IN_PROGRESS) {
            return Optional.of(ApiResponse.failure(messageHandler.getMessage("evaluation.period.not-in-progress")));
        }
        if (!userRepository.existsById(evaluatedUserId)) {
            return Optional.of(ApiResponse.failure(messageHandler.getMessage("user.not-found")));
        }
        if (hasEvaluationBeenSubmitted(evaluatorUserId, evaluatedUserId, periodId)) {
            return Optional.of(ApiResponse.failure(messageHandler.getMessage("evaluation.already.submitted")));
        }
        return Optional.empty();
    }

    private List<Question> getPermittedQuestions(Integer periodId, Integer evaluatorUserId, Integer evaluatedUserId) {
        List<EvaluationAssignment> userAssignments = evaluationAssignmentRepository.findAllByEvaluatorUser_IdAndPeriodParticipant_Period_IdAndPeriodParticipant_EvaluatedUser_Id(
            evaluatorUserId, periodId, evaluatedUserId
        );
        if (userAssignments.isEmpty()) {
            return Collections.emptyList();
        }

        List<Integer> evaluatorRoleIds = userAssignments.stream()
            .map(assignment -> assignment.getEvaluator().getId())
            .distinct()
            .toList();

        List<CompetencyEvaluatorPermission> permissions = competencyEvaluatorPermissionRepository.findByPeriod_IdAndEvaluator_IdIn(periodId, evaluatorRoleIds);
        Set<Integer> permittedCompetencyIds = permissions.stream()
            .map(p -> p.getCompetency().getId())
            .collect(Collectors.toSet());

        if (permittedCompetencyIds.isEmpty()) {
            return Collections.emptyList();
        }

        return questionRepository.findByCompetency_IdIn(permittedCompetencyIds);
    }

    private Optional<ApiResponse<List<AnswerResponse>>> validateAnswerRequests(List<AnswerSubmissionRequest> requests, Map<Integer, Question> questionMap) {
        Set<Integer> requestQuestionIds = requests.stream().map(AnswerSubmissionRequest::getQuestionId).collect(Collectors.toSet());

        if (!questionMap.keySet().equals(requestQuestionIds)) {
            return Optional.of(ApiResponse.failure(messageHandler.getMessage("evaluation.question.mismatch")));
        }

        for (AnswerSubmissionRequest req : requests) {
            Question question = questionMap.get(req.getQuestionId());

            if (question.getHiddenScores().contains(req.getScore())) {
                return Optional.of(ApiResponse.failure(messageHandler.getMessage("evaluation.hidden.score.not-allowed", req.getScore(), question.getId())));
            }

            boolean isCommentRequired = question.getScoresRequiringComment().contains(req.getScore());
            boolean isCommentMissing = (req.getComment() == null || req.getComment().isBlank());

            if (isCommentRequired && isCommentMissing) {
                return Optional.of(ApiResponse.failure(messageHandler.getMessage("evaluation.comment.required", req.getScore(), question.getId())));
            }
        }
        return Optional.empty();
    }

    private List<Answer> createAnswersFromRequests(List<AnswerSubmissionRequest> requests, List<EvaluationAssignment> assignments, Map<Integer, Question> questionMap, Integer periodId) {
        Map<Integer, List<EvaluationAssignment>> competencyToAssignmentsMap = new HashMap<>();
        Map<Integer, EvaluationAssignment> roleIdToAssignmentMap = assignments.stream()
            .collect(Collectors.toMap(a -> a.getEvaluator().getId(), a -> a));

        List<Integer> evaluatorRoleIds = new ArrayList<>(roleIdToAssignmentMap.keySet());
        List<CompetencyEvaluatorPermission> permissions = competencyEvaluatorPermissionRepository.findByPeriod_IdAndEvaluator_IdIn(periodId, evaluatorRoleIds);

        for (CompetencyEvaluatorPermission p : permissions) {
            competencyToAssignmentsMap
                .computeIfAbsent(p.getCompetency().getId(), k -> new ArrayList<>())
                .add(roleIdToAssignmentMap.get(p.getEvaluator().getId()));
        }

        List<Answer> newAnswersToSave = new ArrayList<>();
        for (AnswerSubmissionRequest req : requests) {
            Question question = questionMap.get(req.getQuestionId());
            List<EvaluationAssignment> relevantAssignments = competencyToAssignmentsMap.get(question.getCompetency().getId());

            if (relevantAssignments != null) {
                for (EvaluationAssignment assignment : relevantAssignments) {
                    newAnswersToSave.add(Answer.builder()
                        .assignment(assignment)
                        .question(question)
                        .score(req.getScore())
                        .comment(req.getComment())
                        .build());
                }
            }
        }
        return newAnswersToSave;
    }

    private Optional<EvaluationAssignment> validateUserIsAssignedToEvaluate(Integer periodId, Integer evaluatorUserId, Integer evaluatedUserId) {
        return evaluationAssignmentRepository.findFirstByEvaluatorUser_IdAndPeriodParticipant_Period_IdAndPeriodParticipant_EvaluatedUser_Id(
            evaluatorUserId, periodId, evaluatedUserId
        );
    }
}