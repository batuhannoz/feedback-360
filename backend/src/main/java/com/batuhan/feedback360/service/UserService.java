package com.batuhan.feedback360.service;

import com.batuhan.feedback360.config.AuthenticationPrincipalResolver;
import com.batuhan.feedback360.model.converter.AnswerConverter;
import com.batuhan.feedback360.model.converter.AssignmentConverter;
import com.batuhan.feedback360.model.converter.UserConverter;
import com.batuhan.feedback360.model.entitiy.Answer;
import com.batuhan.feedback360.model.entitiy.Company;
import com.batuhan.feedback360.model.entitiy.EvaluationAssignment;
import com.batuhan.feedback360.model.entitiy.User;
import com.batuhan.feedback360.model.request.UserRequest;
import com.batuhan.feedback360.model.response.AnswerResponse;
import com.batuhan.feedback360.model.response.ApiResponse;
import com.batuhan.feedback360.model.response.AssignmentResponse;
import com.batuhan.feedback360.model.response.UserAssignmentsResponse;
import com.batuhan.feedback360.model.response.UserResponse;
import com.batuhan.feedback360.repository.AnswerRepository;
import com.batuhan.feedback360.repository.CompanyRepository;
import com.batuhan.feedback360.repository.EvaluationAssignmentRepository;
import com.batuhan.feedback360.repository.EvaluationPeriodRepository;
import com.batuhan.feedback360.repository.UserRepository;
import com.batuhan.feedback360.repository.specification.UserSpecification;
import com.batuhan.feedback360.util.MessageHandler;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
    private final EvaluationAssignmentRepository evaluationAssignmentRepository;
    private final AnswerRepository answerRepository;
    private final AnswerConverter answerConverter;
    private final AssignmentConverter assignmentConverter;
    private final EvaluationPeriodRepository periodRepository;

    @Transactional
    public ApiResponse<UserResponse> createUser(UserRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ApiResponse.failure(messageHandler.getMessage("error.user.email-exists", request.getEmail()));
        }

        Company company = companyRepository.getReferenceById(principalResolver.getCompanyId());
        User user = User.builder()
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .email(request.getEmail())
            .company(company)
            .isAdmin(request.getIsAdmin())
            .invitationToken(UUID.randomUUID().toString())
            .role(request.getRole())
            .isActive(true)
            .invitationValidityDate(LocalDateTime.now().plusDays(7))
            .build();
        emailService.sendInvitationEmail(user.getEmail(), user.getInvitationToken());
        userRepository.save(user);
        return ApiResponse.success(userConverter.toUserResponse(user), null);
    }

    @Transactional
    public ApiResponse<UserResponse> updateEmployee(Integer userId, UserRequest request) {
        Integer companyId = principalResolver.getCompanyId();
        Optional<User> employeeOpt = userRepository.findById(userId);
        if (employeeOpt.isEmpty()) {
            return ApiResponse.failure(messageHandler.getMessage("error.employee.not-found", userId));
        }
        User employee = employeeOpt.get();

        if (!employee.getCompany().getId().equals(companyId)) {
            return ApiResponse.failure(messageHandler.getMessage("error.employee.no-permission-update"));
        }

        if (request.getFirstName() != null) {
            employee.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            employee.setLastName(request.getLastName());
        }
        if (request.getIsAdmin() != null) {
            employee.setIsAdmin(request.getIsAdmin());
        }
        if (request.getIsActive() != null) {
            employee.setIsActive(request.getIsActive());
        }
        if (request.getRole() != null) {
            employee.setRole(request.getRole());
        }

        String newEmail = request.getEmail();
        if (newEmail != null && !newEmail.equalsIgnoreCase(employee.getEmail())) {
            if (userRepository.findByEmail(newEmail).isPresent()) {
                return ApiResponse.failure(messageHandler.getMessage("error.user.email-exists", newEmail));
            }
            employee.setEmail(newEmail);
            String newInvitationToken = UUID.randomUUID().toString();
            employee.setInvitationToken(newInvitationToken);
            employee.setInvitationValidityDate(LocalDateTime.now().plusDays(7));
            emailService.sendInvitationEmail(employee.getEmail(), employee.getInvitationToken());
        }

        User updatedUser = userRepository.save(employee);
        return ApiResponse.success(userConverter.toUserResponse(updatedUser), messageHandler.getMessage("user.update.success"));
    }

    @Transactional
    public ApiResponse<List<UserResponse>> getUsers(Boolean active, String name) {
        Company company = companyRepository.getReferenceById(principalResolver.getCompanyId());

        Specification<User> spec = UserSpecification.filterUsers(Long.valueOf(company.getId()), active, name);

        List<User> users = userRepository.findAll(spec);

        List<UserResponse> userResponses = users.stream()
            .map(userConverter::toUserResponse)
            .collect(Collectors.toList());

        return ApiResponse.success(userResponses, "");
    }

    @Transactional
    public ApiResponse<UserResponse> getUserById(Integer userId) {
        Company company = companyRepository.getReferenceById(principalResolver.getCompanyId());
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ApiResponse.failure(messageHandler.getMessage("error.employee.not-found", userId));
        }
        if (!user.getCompany().getId().equals(company.getId())) {
            return ApiResponse.failure(messageHandler.getMessage("error.employee.no-permission-view"));
        }
        return ApiResponse.success(userConverter.toUserResponse(user), "");
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
        User user = userOpt.get();

        List<EvaluationAssignment> madeAssignments = evaluationAssignmentRepository.findAllByEvaluatorUser_IdAndPeriodParticipant_Period_Id(userId, periodId);
        List<AssignmentResponse> madeResponses = madeAssignments.stream()
            .map(assignmentConverter::toAssignmentResponse)
            .toList();

        List<EvaluationAssignment> receivedAssignments = evaluationAssignmentRepository.findAllByPeriodParticipant_EvaluatedUser_IdAndPeriodParticipant_Period_Id(userId, periodId);
        List<AssignmentResponse> receivedResponses = receivedAssignments.stream()
            .map(assignmentConverter::toAssignmentResponse)
            .toList();

        UserAssignmentsResponse response = UserAssignmentsResponse.builder()
            .user(userConverter.toUserResponse(user))
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
}
