package com.batuhan.feedback360.service;

import com.batuhan.feedback360.config.AuthenticationPrincipalResolver;
import com.batuhan.feedback360.model.converter.AnswerConverter;
import com.batuhan.feedback360.model.converter.AssignmentConverter;
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
import com.batuhan.feedback360.model.entitiy.User;
import com.batuhan.feedback360.model.enums.PeriodStatus;
import com.batuhan.feedback360.model.request.AnswerRequest;
import com.batuhan.feedback360.model.request.AnswerSubmissionRequest;
import com.batuhan.feedback360.model.request.UserRequest;
import com.batuhan.feedback360.model.response.AnswerResponse;
import com.batuhan.feedback360.model.response.ApiResponse;
import com.batuhan.feedback360.model.response.AssignmentResponse;
import com.batuhan.feedback360.model.response.EvaluationPeriodResponse;
import com.batuhan.feedback360.model.response.QuestionResponse;
import com.batuhan.feedback360.model.response.UserAssignmentsResponse;
import com.batuhan.feedback360.model.response.UserDetailResponse;
import com.batuhan.feedback360.repository.AnswerRepository;
import com.batuhan.feedback360.repository.CompanyRepository;
import com.batuhan.feedback360.repository.CompetencyEvaluatorPermissionRepository;
import com.batuhan.feedback360.repository.EvaluationAssignmentRepository;
import com.batuhan.feedback360.repository.EvaluationPeriodRepository;
import com.batuhan.feedback360.repository.PeriodParticipantRepository;
import com.batuhan.feedback360.repository.QuestionRepository;
import com.batuhan.feedback360.repository.UserRepository;
import com.batuhan.feedback360.repository.specification.UserSpecification;
import com.batuhan.feedback360.util.MessageHandler;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {

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
    private final QuestionRepository questionRepository;
    private final CompetencyEvaluatorPermissionRepository competencyEvaluatorPermissionRepository;
    private final EvaluationPeriodRepository evaluationPeriodRepository;
    private final QuestionConverter questionConverter;
    private final AnswerRepository answerRepository;
    private final AnswerConverter answerConverter;
    private final AssignmentConverter assignmentConverter;
    private final EvaluationPeriodRepository periodRepository;

    @Transactional
    public ApiResponse<UserDetailResponse> createUser(UserRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ApiResponse.failure(messageHandler.getMessage("error.user.emailExists", request.getEmail()));
        }

        Company company = companyRepository.getReferenceById(principalResolver.getCompanyId());
        User user = User.builder()
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .email(request.getEmail())
            .company(company)
            .isAdmin(request.getIsAdmin())
            .invitationToken(UUID.randomUUID().toString())
            .isActive(true)
            .invitationValidityDate(LocalDateTime.now().plusDays(7))
            .build();
        emailService.sendInvitationEmail(user.getEmail(), user.getInvitationToken());
        userRepository.save(user);
        return ApiResponse.success(userConverter.toUserDetailResponse(user), null);
    }

    @Transactional
    public ApiResponse<UserDetailResponse> updateEmployee(Integer userId, UserRequest request) {
        Integer companyId = principalResolver.getCompanyId();

        User employee = userRepository.findById(userId).orElse(null);
        if (employee == null) {
            return ApiResponse.failure(messageHandler.getMessage("error.employee.notFound", userId));
        }

        if (!employee.getCompany().getId().equals(companyId)) {
            return ApiResponse.failure(messageHandler.getMessage("error.employee.noPermissionUpdate"));
        }

        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        // TODO if email changed send mail with invitation token
        employee.setEmail(request.getEmail());
        employee.setIsAdmin(request.getIsAdmin());
        employee.setIsActive(request.getIsActive());

        User updatedUser = userRepository.save(employee);
        return ApiResponse.success(userConverter.toUserDetailResponse(updatedUser), "");
    }

    @Transactional
    public ApiResponse<List<UserDetailResponse>> getUsers(Boolean active, String name) {
        Company company = companyRepository.getReferenceById(principalResolver.getCompanyId());

        Specification<User> spec = UserSpecification.filterUsers(Long.valueOf(company.getId()), active, name);

        List<User> users = userRepository.findAll(spec);

        List<UserDetailResponse> userResponses = users.stream()
            .map(userConverter::toUserDetailResponse)
            .collect(Collectors.toList());

        return ApiResponse.success(userResponses, "");
    }

    @Transactional
    public ApiResponse<UserDetailResponse> getUserById(Integer userId) {
        Company company = companyRepository.getReferenceById(principalResolver.getCompanyId());
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ApiResponse.failure(messageHandler.getMessage("error.employee.notFound", userId));
        }
        if (!user.getCompany().getId().equals(company.getId())) {
            return ApiResponse.failure(messageHandler.getMessage("error.employee.no-permission-view"));
        }
        return ApiResponse.success(userConverter.toUserDetailResponse(user), "");
    }

    @Transactional
    public ApiResponse<UserAssignmentsResponse> getUserAssignments(Integer periodId, Integer userId) {
        if (!periodRepository.existsByIdAndCompanyId(periodId, principalResolver.getCompanyId())) {
            return ApiResponse.failure(messageHandler.getMessage("evaluation-period.not-found"));
        }
        Company company = companyRepository.getReferenceById(principalResolver.getCompanyId());
        Optional<User> userOpt = userRepository.findByIdAndCompany(userId, company);
        if (userOpt.isEmpty()) {
            return ApiResponse.failure(messageHandler.getMessage("user.not-found"));
        }

        List<EvaluationAssignment> madeAssignments = evaluationAssignmentRepository.findAllByEvaluatorUser_IdAndPeriodParticipant_Period_Id(userId, periodId);
        List<AssignmentResponse> madeResponses = madeAssignments.stream()
            .map(assignmentConverter::toAssignmentResponse)
            .toList();

        List<EvaluationAssignment> receivedAssignments = evaluationAssignmentRepository.findAllByPeriodParticipant_EvaluatedUser_IdAndPeriodParticipant_Period_Id(userId, periodId);
        List<AssignmentResponse> receivedResponses = receivedAssignments.stream()
            .map(assignmentConverter::toAssignmentResponse)
            .toList();

        UserAssignmentsResponse response = UserAssignmentsResponse.builder()
            .user(userConverter.toUserDetailResponse(userOpt.get()))
            .evaluationsMade(madeResponses)
            .evaluationsReceived(receivedResponses)
            .build();

        return ApiResponse.success(response, messageHandler.getMessage("admin.assignments.get.success"));
    }

    @Transactional
    public ApiResponse<List<AnswerResponse>> getAnswersForAssignment(Integer assignmentId) {
        Optional<EvaluationAssignment> assignmentOpt = evaluationAssignmentRepository.findById(assignmentId);
        if (assignmentOpt.isEmpty() || !assignmentOpt.get().getPeriodParticipant().getPeriod().getCompany().getId().equals(principalResolver.getCompanyId())) {
            return ApiResponse.failure(messageHandler.getMessage("admin.assignment.not-found"));
        }

        List<Answer> answers = answerRepository.findAllByAssignment_Id(assignmentId);
        List<AnswerResponse> response = answers.stream()
            .map(answerConverter::toAnswerResponse)
            .toList();

        return ApiResponse.success(response, messageHandler.getMessage("admin.assignment.answers.get.success"));
    }

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
    public ApiResponse<?> submitAnswers(Integer periodId, Integer evaluatedUserId, List<AnswerRequest> answers) {
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

        Map<Integer, EvaluationAssignment> roleIdToAssignmentMap = assignments.stream()
            .collect(Collectors.toMap(a -> a.getEvaluator().getId(), a -> a));
        List<CompetencyEvaluatorPermission> permissions = competencyEvaluatorPermissionRepository.findByPeriod_IdAndEvaluator_IdIn(periodId, roleIdToAssignmentMap.keySet());

        Map<Integer, List<EvaluationAssignment>> competencyToAssignmentsMap = new HashMap<>();
        for (CompetencyEvaluatorPermission p : permissions) {
            competencyToAssignmentsMap
                .computeIfAbsent(p.getCompetency().getId(), k -> new ArrayList<>())
                .add(roleIdToAssignmentMap.get(p.getEvaluator().getId()));
        }

        List<Question> permittedQuestions = questionRepository.findByCompetency_IdIn(competencyToAssignmentsMap.keySet());
        Set<Integer> permittedQuestionIds = permittedQuestions.stream().map(Question::getId).collect(Collectors.toSet());
        Set<Integer> requestQuestionIds = answers.stream().map(AnswerRequest::getQuestionId).collect(Collectors.toSet());

        if (!permittedQuestionIds.equals(requestQuestionIds)) {
            return ApiResponse.failure(messageHandler.getMessage("evaluation.question.mismatch"));
        }

        answerRepository.deleteAllByAssignmentIn(assignments);
        answerRepository.flush();

        Map<Integer, Question> questionMap = permittedQuestions.stream().collect(Collectors.toMap(Question::getId, q -> q));
        List<Answer> newAnswersToSave = new ArrayList<>();

        for (AnswerRequest req : answers) {
            Question question = questionMap.get(req.getQuestionId());
            List<EvaluationAssignment> relevantAssignments = competencyToAssignmentsMap.get(question.getCompetency().getId());

            if (relevantAssignments != null) {
                for (EvaluationAssignment assignment : relevantAssignments) {
                    newAnswersToSave.add(Answer.builder()
                        .assignment(assignment)
                        .question(question)
                        .score(req.getScore())
                        .answerText(req.getComment() == null ? "" : req.getComment())
                        .build());
                }
            }
        }
        answerRepository.saveAll(newAnswersToSave);

        return ApiResponse.success(null, messageHandler.getMessage("evaluation.submit.success"));
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
