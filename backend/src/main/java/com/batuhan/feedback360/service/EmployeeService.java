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
import com.batuhan.feedback360.model.response.EmployeeDetailResponse;
import com.batuhan.feedback360.model.response.EmployeeSimpleResponse;
import com.batuhan.feedback360.model.response.EvaluationDetailResponse;
import com.batuhan.feedback360.model.response.EvaluationPeriodResponse;
import com.batuhan.feedback360.model.response.EvaluationTaskResponse;
import com.batuhan.feedback360.repository.EmployeeRepository;
import com.batuhan.feedback360.repository.EvaluationPeriodRepository;
import com.batuhan.feedback360.repository.EvaluationRepository;
import com.batuhan.feedback360.repository.RoleRepository;
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

    public Employee createEmployee(EmployeeRequest request) {
        Company company = new Company();
        company.setId(principalResolver.getCompanyId());

        var employee = Employee.builder()
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .email(request.getEmail())
            .company(company)
            .isAdmin(request.isAdmin())
            .invitationToken(UUID.randomUUID().toString())
            .invitationValidityDate(LocalDateTime.now().plusDays(7))
            .build();

        // TODO: Bu aşamada çalışana davet maili gönderilir.
        // Mail içeriğinde `invitationToken` bulunur.

        return employeeRepository.save(employee);
    }

    @Transactional
    public List<EmployeeDetailResponse> getAllEmployees() {
        Company company = new Company();
        company.setId(principalResolver.getCompanyId());

        return employeeRepository.findByCompanyId(company.getId())
            .stream()
            .map(employeeConverter::toEmployeeDetailResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public EmployeeDetailResponse getEmployeeById(Integer employeeId) {
        Company company = new Company();
        company.setId(principalResolver.getCompanyId());

        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new RuntimeException("Employee not found with id: " + employeeId));

        if (!employee.getCompany().getId().equals(company.getId())) {
            throw new SecurityException("You do not have permission to view this employee.");
        }

        return employeeConverter.toEmployeeDetailResponse(employee);
    }

    @Transactional
    public EmployeeDetailResponse assignRoleToEmployee(Integer employeeId, Integer roleId) {
        Company company = new Company();
        company.setId(principalResolver.getCompanyId());

        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new RuntimeException("Employee not found with id: " + employeeId));

        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId));

        if (!employee.getCompany().getId().equals(company.getId()) || !role.getCompany().getId().equals(company.getId())) {
            throw new SecurityException("You do not have permission to perform this assignment.");
        }

        employee.getRoles().add(role);
        Employee updatedEmployee = employeeRepository.save(employee);

        return employeeConverter.toEmployeeDetailResponse(updatedEmployee);
    }

    @Transactional
    public EmployeeDetailResponse removeRoleFromEmployee(Integer employeeId, Integer roleId) {
        Company company = new Company();
        company.setId(principalResolver.getCompanyId());

        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new RuntimeException("Employee not found with id: " + employeeId));
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId));

        if (!employee.getCompany().equals(company) || !role.getCompany().equals(company)) {
            throw new SecurityException("You do not have permission to perform this action.");
        }

        if (!employee.getRoles().contains(role)) {
            throw new IllegalArgumentException("Employee does not have the specified role.");
        }

        employee.getRoles().remove(role);
        Employee updatedEmployee = employeeRepository.save(employee);
        return employeeConverter.toEmployeeDetailResponse(updatedEmployee);
    }

    @Transactional
    public EmployeeDetailResponse updateEmployee(Integer employeeId, EmployeeRequest request) {
        Integer companyId = principalResolver.getCompanyId();

        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new RuntimeException("Employee not found with id: " + employeeId));

        if (!employee.getCompany().getId().equals(companyId)) {
            throw new SecurityException("You do not have permission to update this employee.");
        }

        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setIsAdmin(request.isAdmin());

        Employee updatedEmployee = employeeRepository.save(employee);
        return employeeConverter.toEmployeeDetailResponse(updatedEmployee);
    }

    public List<EvaluationPeriodResponse> getEmployeePeriods() {
        Integer currentEmployeeId = principalResolver.getUserId();

        Employee currentUser = employeeRepository.findById(currentEmployeeId)
            .orElseThrow(() -> new EntityNotFoundException("Mevcut çalışan bulunamadı. ID: " + currentEmployeeId));

        List<Evaluation> userInvolvedEvaluations = evaluationRepository.findByEvaluatorOrEvaluated(currentUser, currentUser);

        List<EvaluationPeriod> distinctPeriods = userInvolvedEvaluations.stream()
            .map(Evaluation::getPeriod).distinct().toList();

        return distinctPeriods.stream()
            .map(evaluationPeriodConverter::toEvaluationPeriodResponse)
            .collect(Collectors.toList());
    }

    public List<EvaluationTaskResponse> getEvaluationTasksForPeriod(Integer periodId) {
        Integer currentEmployeeId = principalResolver.getUserId();
        Employee currentUser = employeeRepository.findById(currentEmployeeId)
            .orElseThrow(() -> new EntityNotFoundException("Mevcut çalışan bulunamadı. ID: " + currentEmployeeId));

        EvaluationPeriod period = evaluationPeriodRepository.findById(periodId)
            .orElseThrow(() -> new EntityNotFoundException("Değerlendirme dönemi bulunamadı. ID: " + periodId));

        List<Evaluation> evaluations = evaluationRepository.findByEvaluatorAndPeriod(currentUser, period);

        return evaluations.stream()
            .map(this::convertToTaskResponse)
            .collect(Collectors.toList());
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
    public EvaluationDetailResponse startOrGetEvaluation(Integer evaluationId) {
        Integer currentEmployeeId = principalResolver.getUserId();

        Evaluation evaluation = evaluationRepository.findByIdWithAnswersAndQuestions(evaluationId)
            .orElseThrow(() -> new EntityNotFoundException("Değerlendirme bulunamadı: " + evaluationId));

        if (!evaluation.getEvaluator().getId().equals(currentEmployeeId)) {
            throw new AccessDeniedException("Bu değerlendirmeye erişim yetkiniz yok.");
        }
        boolean isPeriodOver = LocalDate.now().isAfter(evaluation.getPeriod().getEndDate());

        if (evaluation.getStatus() == EvaluationStatus.NOT_STARTED && !isPeriodOver) {
            evaluation.setStatus(EvaluationStatus.IN_PROGRESS);
            evaluationRepository.save(evaluation);
        }
        return evaluationConverter.ToDetailResponse(evaluation);
    }

    @Transactional
    public EvaluationDetailResponse submitAnswers(Integer evaluationId, SubmitAnswersRequest request) {
        Integer currentEmployeeId = principalResolver.getUserId();

        Evaluation evaluation = evaluationRepository.findByIdWithAnswersAndQuestions(evaluationId)
            .orElseThrow(() -> new EntityNotFoundException("Değerlendirme bulunamadı: " + evaluationId));

        if (LocalDate.now().isAfter(evaluation.getPeriod().getEndDate())) {
            throw new IllegalStateException("Değerlendirme dönemi sona erdiği için cevap gönderemezsiniz.");
        }
        if (!evaluation.getEvaluator().getId().equals(currentEmployeeId)) {
            throw new AccessDeniedException("Bu değerlendirmeye cevap gönderemezsiniz.");
        }
        if (evaluation.getStatus() == EvaluationStatus.COMPLETED) {
            throw new IllegalStateException("Bu değerlendirme zaten tamamlanmış ve değiştirilemez.");
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
        return evaluationConverter.ToDetailResponse(updatedEvaluation);
    }
}
