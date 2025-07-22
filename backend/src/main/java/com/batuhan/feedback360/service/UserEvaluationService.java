package com.batuhan.feedback360.service;

import com.batuhan.feedback360.config.AuthenticationPrincipalResolver;
import com.batuhan.feedback360.model.converter.AnswerConverter;
import com.batuhan.feedback360.model.converter.CompetencyConverter;
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
    private final EmailService emailService;
    private final CompanyRepository companyRepository;
    private final UserConverter userConverter;
    private final EvaluationPeriodConverter evaluationPeriodConverter;
    private final PeriodParticipantRepository periodParticipantRepository;
    private final EvaluationAssignmentRepository evaluationAssignmentRepository;
    private final CompetencyConverter competencyConverter;
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
        Optional<EvaluationPeriod> periodOpt = evaluationPeriodRepository.findByIdAndCompanyId(periodId, principalResolver.getCompanyId());
        if (periodOpt.isEmpty()) {
            return ApiResponse.failure(messageHandler.getMessage("evaluation-period.not-found"));
        }
        if (periodOpt.get().getStatus() != PeriodStatus.IN_PROGRESS) {
            return ApiResponse.failure(messageHandler.getMessage("evaluation.period.not-in-progress"));
        }

        List<EvaluationAssignment> assignments = evaluationAssignmentRepository.findAllByEvaluatorUser_IdAndPeriodParticipant_Period_Id(principalResolver.getUserId(), periodId);

        if (assignments.isEmpty()) {
            return ApiResponse.success(Collections.emptyList(), messageHandler.getMessage("user.tasks.get.no-tasks"));
        }

        List<UserDetailResponse> evaluatedUsers = assignments.stream()
            .map(assignment -> assignment.getPeriodParticipant().getEvaluatedUser())
            .distinct()
            .map(userConverter::toUserDetailResponse)
            .sorted(Comparator.comparing(UserDetailResponse::getFirstName))
            .toList();

        return ApiResponse.success(evaluatedUsers, messageHandler.getMessage("user.tasks.users.get.success"));
    }

    public ApiResponse<List<QuestionResponse>> getQuestionsForEvaluatedUser(Integer periodId, Integer evaluatedUserId) {
        Integer evaluatorUserId = principalResolver.getUserId();
        Optional<EvaluationPeriod> periodOpt = evaluationPeriodRepository.findByIdAndCompanyId(periodId, principalResolver.getCompanyId());
        if (periodOpt.isEmpty()) {
            return ApiResponse.failure(messageHandler.getMessage("evaluation-period.not-found"));
        }
        if (periodOpt.get().getStatus() != PeriodStatus.IN_PROGRESS) {
            return ApiResponse.failure(messageHandler.getMessage("evaluation.period.not-in-progress"));
        }
        if (validateUserIsAssignedToEvaluate(periodId, evaluatorUserId, evaluatedUserId).isEmpty()) {
            return ApiResponse.failure(messageHandler.getMessage("user.tasks.permission.denied"));
        }

        List<EvaluationAssignment> userAssignments = evaluationAssignmentRepository.findAllByEvaluatorUser_IdAndPeriodParticipant_Period_IdAndPeriodParticipant_EvaluatedUser_Id(
            evaluatorUserId, periodId, evaluatedUserId
        );

        List<Integer> evaluatorRoleIds = userAssignments.stream()
            .map(assignment -> assignment.getEvaluator().getId())
            .distinct()
            .toList();

        List<CompetencyEvaluatorPermission> permissions = competencyEvaluatorPermissionRepository.findByPeriod_IdAndEvaluator_IdIn(periodId, evaluatorRoleIds);
        Set<Integer> permittedCompetencyIds = permissions.stream()
            .map(p -> p.getCompetency().getId())
            .collect(Collectors.toSet());

        if (permittedCompetencyIds.isEmpty()) {
            return ApiResponse.success(Collections.emptyList(), messageHandler.getMessage("user.tasks.get.success"));
        }

        List<Question> questions = questionRepository.findByCompetency_IdIn(permittedCompetencyIds);

        List<QuestionResponse> questionResponses = questions.stream()
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

        EvaluationPeriod period = evaluationPeriodRepository.findByIdAndCompanyId(periodId, principalResolver.getCompanyId())
            .orElse(null);
        if (period == null || period.getStatus() != PeriodStatus.IN_PROGRESS) {
            return ApiResponse.failure(messageHandler.getMessage("evaluation.period.not-in-progress"));
        }
        if (!userRepository.existsById(evaluatedUserId)) {
            return ApiResponse.failure(messageHandler.getMessage("user.not-found"));
        }

        List<EvaluationAssignment> assignments = evaluationAssignmentRepository.findAllByEvaluatorUser_IdAndPeriodParticipant_Period_IdAndPeriodParticipant_EvaluatedUser_Id(
            evaluatorUserId, periodId, evaluatedUserId
        );
        if (assignments.isEmpty()) {
            return ApiResponse.failure(messageHandler.getMessage("evaluation.assignment.not-found"));
        }

        List<Integer> evaluatorRoleIds = assignments.stream()
            .map(assignment -> assignment.getEvaluator().getId())
            .distinct()
            .toList();

        List<CompetencyEvaluatorPermission> permissions = competencyEvaluatorPermissionRepository.findByPeriod_IdAndEvaluator_IdIn(periodId, evaluatorRoleIds);
        Set<Integer> permittedCompetencyIds = permissions.stream()
            .map(p -> p.getCompetency().getId())
            .collect(Collectors.toSet());

        List<Question> permittedQuestions = questionRepository.findByCompetency_IdIn(permittedCompetencyIds);
        Set<Integer> permittedQuestionIds = permittedQuestions.stream().map(Question::getId).collect(Collectors.toSet());

        Set<Integer> requestQuestionIds = answerRequests.stream().map(AnswerSubmissionRequest::getQuestionId).collect(Collectors.toSet());

        if (!permittedQuestionIds.equals(requestQuestionIds)) {
            return ApiResponse.failure(messageHandler.getMessage("evaluation.question.mismatch"));
        }

        answerRepository.deleteAllByAssignmentIn(assignments);
        answerRepository.flush();

        Map<Integer, Question> questionMap = permittedQuestions.stream()
            .collect(Collectors.toMap(Question::getId, q -> q));

        Map<Integer, List<EvaluationAssignment>> competencyToAssignmentsMap = new HashMap<>();
        Map<Integer, EvaluationAssignment> roleIdToAssignmentMap = assignments.stream()
            .collect(Collectors.toMap(a -> a.getEvaluator().getId(), a -> a));

        for (CompetencyEvaluatorPermission p : permissions) {
            competencyToAssignmentsMap
                .computeIfAbsent(p.getCompetency().getId(), k -> new ArrayList<>())
                .add(roleIdToAssignmentMap.get(p.getEvaluator().getId()));
        }

        List<Answer> newAnswersToSave = new ArrayList<>();
        for (AnswerSubmissionRequest req : answerRequests) {
            Question question = questionMap.get(req.getQuestionId());
            List<EvaluationAssignment> relevantAssignments = competencyToAssignmentsMap.get(question.getCompetency().getId());

            if (relevantAssignments != null) {
                for (EvaluationAssignment assignment : relevantAssignments) {
                    newAnswersToSave.add(Answer.builder()
                        .assignment(assignment)
                        .question(question)
                        .score(req.getScore())
                        .answerText(req.getAnswerText())
                        .build());
                }
            }
        }

        List<Answer> savedAnswers = answerRepository.saveAll(newAnswersToSave);

        List<AnswerResponse> response = savedAnswers.stream()
            .map(answerConverter::toAnswerResponse)
            .collect(Collectors.toList());

        return ApiResponse.success(response, messageHandler.getMessage("evaluation.submit.success"));
    }

    private Optional<EvaluationAssignment> validateUserIsAssignedToEvaluate(Integer periodId, Integer evaluatorUserId, Integer evaluatedUserId) {
        return evaluationAssignmentRepository.findFirstByEvaluatorUser_IdAndPeriodParticipant_Period_IdAndPeriodParticipant_EvaluatedUser_Id(
            evaluatorUserId, periodId, evaluatedUserId
        );
    }
}
