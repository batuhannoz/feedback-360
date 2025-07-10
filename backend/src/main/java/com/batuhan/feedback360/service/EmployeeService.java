package com.batuhan.feedback360.service;

import com.batuhan.feedback360.config.AuthenticationPrincipalResolver;
import com.batuhan.feedback360.model.converter.EmployeeConverter;
import com.batuhan.feedback360.model.converter.EvaluationConverter;
import com.batuhan.feedback360.model.converter.EvaluationPeriodConverter;
import com.batuhan.feedback360.model.entitiy.Company;
import com.batuhan.feedback360.model.entitiy.Employee;
import com.batuhan.feedback360.model.entitiy.Evaluation;
import com.batuhan.feedback360.model.entitiy.EvaluationPeriod;
import com.batuhan.feedback360.model.entitiy.Role;
import com.batuhan.feedback360.model.enums.EvaluationStatus;
import com.batuhan.feedback360.model.request.AnswerPayload;
import com.batuhan.feedback360.model.request.EmployeeRequest;
import com.batuhan.feedback360.model.request.SubmitAnswersRequest;
import com.batuhan.feedback360.model.response.ApiResponse;
import com.batuhan.feedback360.model.response.EmployeeDetailResponse;
import com.batuhan.feedback360.model.response.EmployeeSimpleResponse;
import com.batuhan.feedback360.model.response.EvaluationDetailResponse;
import com.batuhan.feedback360.model.response.EvaluationPeriodResponse;
import com.batuhan.feedback360.model.response.EvaluationTaskResponse;
import com.batuhan.feedback360.repository.EmployeeRepository;
import com.batuhan.feedback360.repository.EvaluationPeriodRepository;
import com.batuhan.feedback360.repository.EvaluationRepository;
import com.batuhan.feedback360.repository.RoleRepository;
import com.batuhan.feedback360.util.MessageHandler;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
@AllArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final AuthenticationPrincipalResolver principalResolver;
    private final RoleRepository roleRepository;
    private final EmployeeConverter employeeConverter;
    private final EvaluationRepository evaluationRepository;
    private final EvaluationPeriodConverter evaluationPeriodConverter;
    private final EvaluationPeriodRepository evaluationPeriodRepository;
    private final EvaluationConverter evaluationConverter;
    private final MessageHandler messageHandler;
    private final EmailService emailService;

    @Transactional
    public ApiResponse<Employee> createEmployee(EmployeeRequest request) {
        if (employeeRepository.findByEmail(request.getEmail()).isEmpty()) {
            return ApiResponse.failure(messageHandler.getMessage("error.employee.emailExists", request.getEmail()));
        }
        Company company = new Company();
        company.setId(principalResolver.getCompanyId());

        Employee employee = Employee.builder()
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .email(request.getEmail())
            .company(company)
            .isAdmin(request.getIsAdmin())
            .invitationToken(UUID.randomUUID().toString())
            .isActive(true)
            .invitationValidityDate(LocalDateTime.now().plusDays(7))
            .build();
        emailService.sendInvitationEmail(employee.getEmail(), employee.getInvitationToken());
        return ApiResponse.success(employeeRepository.save(employee), "");
    }

    @Transactional
    public ApiResponse<List<EmployeeDetailResponse>> getAllEmployees() {
        Company company = new Company();
        company.setId(principalResolver.getCompanyId());

        return ApiResponse.success(employeeRepository.findByCompanyId(company.getId())
            .stream()
            .map(employeeConverter::toEmployeeDetailResponse)
            .collect(Collectors.toList()), "");
    }

    @Transactional
    public ApiResponse<EmployeeDetailResponse> getEmployeeById(Integer employeeId) {
        Company company = new Company();
        company.setId(principalResolver.getCompanyId());

        Employee employee = employeeRepository.findById(employeeId).orElse(null);
        if (employee == null) {
            return ApiResponse.failure(messageHandler.getMessage("error.employee.notFound", employeeId));
        }
        if (!employee.getCompany().getId().equals(company.getId())) {
            return ApiResponse.failure(messageHandler.getMessage("error.employee.noPermissionView"));
        }
        return ApiResponse.success(employeeConverter.toEmployeeDetailResponse(employee), "");
    }

    @Transactional
    public ApiResponse<EmployeeDetailResponse> assignRoleToEmployee(Integer employeeId, Integer roleId) {
        Company company = new Company();
        company.setId(principalResolver.getCompanyId());

        Employee employee = employeeRepository.findById(employeeId).orElse(null);
        if (employee == null) {
            return ApiResponse.failure(messageHandler.getMessage("error.employee.notFound", employeeId));
        }
        Role role = roleRepository.findById(roleId).orElse(null);
        if (role == null) {
            return ApiResponse.failure(messageHandler.getMessage("error.role.notFound", roleId));
        }

        if (!employee.getCompany().getId().equals(company.getId()) || !role.getCompany().getId().equals(company.getId())) {
            return ApiResponse.failure(messageHandler.getMessage("error.role.assignmentPermission"));
        }

        employee.getRoles().add(role);
        Employee updatedEmployee = employeeRepository.save(employee);
        return ApiResponse.success(employeeConverter.toEmployeeDetailResponse(updatedEmployee), "");
    }

    @Transactional
    public ApiResponse<EmployeeDetailResponse> removeRoleFromEmployee(Integer employeeId, Integer roleId) {
        Company company = new Company();
        company.setId(principalResolver.getCompanyId());

        Employee employee = employeeRepository.findById(employeeId).orElse(null);
        if (employee == null) {
            return ApiResponse.failure(messageHandler.getMessage("error.employee.notFound", employeeId));
        }
        Role role = roleRepository.findById(roleId).orElse(null);
        if (role == null) {
            return ApiResponse.failure(messageHandler.getMessage("error.role.notFound", roleId));
        }

        if (!employee.getCompany().equals(company) || !role.getCompany().equals(company)) {
            return ApiResponse.failure(messageHandler.getMessage("error.role.actionPermission"));
        }

        if (!employee.getRoles().contains(role)) {
            return ApiResponse.failure(messageHandler.getMessage("error.role.employeeNotHave"));
        }

        employee.getRoles().remove(role);
        Employee updatedEmployee = employeeRepository.save(employee);
        return ApiResponse.success(employeeConverter.toEmployeeDetailResponse(updatedEmployee), "");
    }

    @Transactional
    public ApiResponse<EmployeeDetailResponse> updateEmployee(Integer employeeId, EmployeeRequest request) {
        Integer companyId = principalResolver.getCompanyId();

        Employee employee = employeeRepository.findById(employeeId).orElse(null);
        if (employee == null) {
            return ApiResponse.failure(messageHandler.getMessage("error.employee.notFound", employeeId));
        }

        if (!employee.getCompany().getId().equals(companyId)) {
            return ApiResponse.failure(messageHandler.getMessage("error.employee.noPermissionUpdate"));
        }

        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        // TODO if email changed send new mail with invitation token
        employee.setEmail(request.getEmail());
        employee.setIsAdmin(request.getIsAdmin());
        employee.setIsActive(request.getIsActive());

        Employee updatedEmployee = employeeRepository.save(employee);
        return ApiResponse.success(employeeConverter.toEmployeeDetailResponse(updatedEmployee), "");
    }

    public ApiResponse<List<EvaluationPeriodResponse>> getEmployeePeriods() {
        Integer currentEmployeeId = principalResolver.getUserId();

        Employee currentEmployee = employeeRepository.findById(currentEmployeeId).orElse(null);
        if (currentEmployee == null) {
            return ApiResponse.failure(messageHandler.getMessage("error.employee.notFound", currentEmployeeId));
        }

        List<Evaluation> userInvolvedEvaluations = evaluationRepository.findByEvaluatorOrEvaluated(currentEmployee, currentEmployee);

        List<EvaluationPeriod> distinctPeriods = userInvolvedEvaluations.stream()
            .map(Evaluation::getPeriod).distinct().toList();

        return ApiResponse.success(
            distinctPeriods.stream()
                .map(evaluationPeriodConverter::toEvaluationPeriodResponse)
                .collect(Collectors.toList()),
            "");
    }

    public ApiResponse<List<EvaluationTaskResponse>> getEvaluationTasksForPeriod(Integer periodId) {
        Integer currentEmployeeId = principalResolver.getUserId();
        Employee currentEmployee = employeeRepository.findById(currentEmployeeId).orElse(null);
        if (currentEmployee == null) {
            return ApiResponse.failure(messageHandler.getMessage("error.employee.notFound", currentEmployeeId));
        }

        EvaluationPeriod period = evaluationPeriodRepository.findById(periodId).orElse(null);
        if (period == null) {
            return ApiResponse.failure(messageHandler.getMessage("error.period.notFound", periodId));
        }

        List<Evaluation> evaluations = evaluationRepository.findByEvaluatorAndPeriod(currentEmployee, period);

        return ApiResponse.success(
            evaluations.stream().map(this::convertToTaskResponse)
                .collect(Collectors.toList()),
            "");
    }

    private EvaluationTaskResponse convertToTaskResponse(Evaluation evaluation) {
        Employee evaluatedPerson = evaluation.getEvaluated();

        EmployeeSimpleResponse employeeDto = EmployeeSimpleResponse.builder()
            .id(evaluatedPerson.getId())
            .fullName(evaluatedPerson.getFullName())
            .build();

        return EvaluationTaskResponse.builder()
            .evaluationId(evaluation.getId())
            .evaluatedEmployee(employeeDto)
            .status(evaluation.getStatus())
            .submissionDate(evaluation.getDeliveryDate())
            .build();
    }

    @Transactional
    public ApiResponse<EvaluationDetailResponse> startOrGetEvaluation(Integer evaluationId) {
        Integer currentEmployeeId = principalResolver.getUserId();

        Evaluation evaluation = evaluationRepository.findByIdWithAnswersAndQuestions(evaluationId).orElse(null);
        if (evaluation == null) {
            return ApiResponse.failure(messageHandler.getMessage("error.evaluation.notFound", evaluationId));
        }

        if (!evaluation.getEvaluator().getId().equals(currentEmployeeId)) {
            return ApiResponse.failure(messageHandler.getMessage("error.evaluation.accessDenied"));
        }
        boolean isPeriodOver = LocalDate.now().isAfter(evaluation.getPeriod().getEndDate());

        if (evaluation.getStatus() == EvaluationStatus.NOT_STARTED && !isPeriodOver) {
            evaluation.setStatus(EvaluationStatus.IN_PROGRESS);
            evaluationRepository.save(evaluation);
        }
        return ApiResponse.success(evaluationConverter.ToDetailResponse(evaluation), "");
    }

    @Transactional
    public ApiResponse<EvaluationDetailResponse> submitAnswers(Integer evaluationId, SubmitAnswersRequest request) {
        Integer currentEmployeeId = principalResolver.getUserId();

        Evaluation evaluation = evaluationRepository.findByIdWithAnswersAndQuestions(evaluationId).orElse(null);
        if (evaluation == null) {
            return ApiResponse.failure(messageHandler.getMessage("error.evaluation.notFound", evaluationId));
        }

        if (LocalDate.now().isAfter(evaluation.getPeriod().getEndDate())) {
            return ApiResponse.failure(messageHandler.getMessage("error.evaluation.periodOver"));
        }
        if (!evaluation.getEvaluator().getId().equals(currentEmployeeId)) {
            return ApiResponse.failure(messageHandler.getMessage("error.evaluation.submitAccessDenied"));
        }
        if (evaluation.getStatus() == EvaluationStatus.COMPLETED) {
            return ApiResponse.failure(messageHandler.getMessage("error.evaluation.alreadyCompleted"));
        }

        Map<Integer, String> submittedAnswersMap = request.getAnswers().stream()
            .collect(Collectors.toMap(AnswerPayload::getAnswerId, AnswerPayload::getValue));

        evaluation.getAnswers().forEach(answer -> {
            if (submittedAnswersMap.containsKey(answer.getId())) {
                answer.setAnswer(submittedAnswersMap.get(answer.getId()));
            }
        });
        boolean allQuestionsAnswered = evaluation.getAnswers().stream()
            .allMatch(answer -> answer.getAnswer() != null && !answer.getAnswer().isBlank());

        if (allQuestionsAnswered) {
            evaluation.setStatus(EvaluationStatus.COMPLETED);
            evaluation.setDeliveryDate(LocalDateTime.now());
        }
        Evaluation updatedEvaluation = evaluationRepository.save(evaluation);
        return ApiResponse.success(evaluationConverter.ToDetailResponse(updatedEvaluation), "");
    }
}
