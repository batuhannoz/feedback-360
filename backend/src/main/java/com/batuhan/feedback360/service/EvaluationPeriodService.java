package com.batuhan.feedback360.service;

import com.batuhan.feedback360.config.AuthenticationPrincipalResolver;
import com.batuhan.feedback360.model.converter.EvaluationPeriodConverter;
import com.batuhan.feedback360.model.entitiy.Answer;
import com.batuhan.feedback360.model.entitiy.Company;
import com.batuhan.feedback360.model.entitiy.Employee;
import com.batuhan.feedback360.model.entitiy.Evaluation;
import com.batuhan.feedback360.model.entitiy.EvaluationPeriod;
import com.batuhan.feedback360.model.entitiy.EvaluationTemplate;
import com.batuhan.feedback360.model.entitiy.Question;
import com.batuhan.feedback360.model.entitiy.Role;
import com.batuhan.feedback360.model.enums.EvaluationStatus;
import com.batuhan.feedback360.model.request.EvaluationPeriodRequest;
import com.batuhan.feedback360.model.response.ApiResponse;
import com.batuhan.feedback360.model.response.EvaluationPeriodResponse;
import com.batuhan.feedback360.repository.CompanyRepository;
import com.batuhan.feedback360.repository.EmployeeRepository;
import com.batuhan.feedback360.repository.EvaluationPeriodRepository;
import com.batuhan.feedback360.repository.EvaluationRepository;
import com.batuhan.feedback360.repository.EvaluationTemplateRepository;
import com.batuhan.feedback360.util.MessageHandler;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EvaluationPeriodService {

    private final EvaluationPeriodRepository evaluationPeriodRepository;
    private final AuthenticationPrincipalResolver principalResolver;
    private final EvaluationPeriodConverter evaluationPeriodConverter;
    private final EvaluationTemplateRepository evaluationTemplateRepository;
    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final EvaluationRepository evaluationRepository;
    private final MessageHandler messageHandler;


    @Transactional
    public ApiResponse<EvaluationPeriodResponse> createPeriod(EvaluationPeriodRequest request) {
        Company company = companyRepository.getReferenceById(principalResolver.getCompanyId());

        Set<EvaluationTemplate> templatesForPeriod = new HashSet<>(
            evaluationTemplateRepository.findAllById(request.getTemplateIds())
        );

        if (templatesForPeriod.isEmpty()) {
            return ApiResponse.failure(messageHandler.getMessage("error.period.noTemplates"));
        }

        EvaluationPeriod evaluationPeriod = EvaluationPeriod.builder()
            .name(request.getName())
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .company(company)
            .templates(templatesForPeriod)
            .build();
        evaluationPeriodRepository.save(evaluationPeriod);

        List<Evaluation> evaluationsToCreate = new ArrayList<>();
        List<Employee> allEmployeesInCompany = employeeRepository.findAllByCompany(company);

        for (EvaluationTemplate template : templatesForPeriod) {
            Role targetRole = template.getTargetRole();
            Set<Role> evaluatorRoles = template.getEvaluatorRoles();

            List<Employee> evaluatedEmployees = allEmployeesInCompany.stream()
                .filter(e -> e.getRoles().contains(targetRole))
                .toList();

            List<Employee> evaluatorEmployees = allEmployeesInCompany.stream()
                .filter(e -> e.getRoles().stream().anyMatch(evaluatorRoles::contains))
                .toList();

            Set<Question> questionsForTemplate = template.getQuestions();

            for (Employee evaluated : evaluatedEmployees) {
                for (Employee evaluator : evaluatorEmployees) {
                    if (evaluated.getId().equals(evaluator.getId())) {
                        continue;
                    }

                    Evaluation evaluation = Evaluation.builder()
                        .period(evaluationPeriod)
                        .evaluated(evaluated)
                        .evaluator(evaluator)
                        .status(EvaluationStatus.NOT_STARTED)
                        .build();

                    Set<Answer> answers = questionsForTemplate.stream()
                        .map(question -> Answer.builder()
                            .evaluation(evaluation)
                            .question(question)
                            .answer(null)
                            .build())
                        .collect(Collectors.toSet());

                    evaluation.setAnswers(answers);
                    evaluationsToCreate.add(evaluation);
                }
            }
        }
        if (!evaluationsToCreate.isEmpty()) {
            evaluationRepository.saveAll(evaluationsToCreate);
        }
        return ApiResponse.success(
            evaluationPeriodConverter.toEvaluationPeriodResponse(evaluationPeriod),
            messageHandler.getMessage("success.period.created", evaluationPeriod.getName())
        );
    }

    public ApiResponse<List<EvaluationPeriodResponse>> findAllPeriodsByCompany() {
        Integer companyId = principalResolver.getCompanyId();
        List<EvaluationPeriod> periods = evaluationPeriodRepository.findByCompanyId(companyId);
        List<EvaluationPeriodResponse> responseData = periods.stream()
            .map(evaluationPeriodConverter::toEvaluationPeriodResponse)
            .collect(Collectors.toList());
        return ApiResponse.success(responseData, messageHandler.getMessage("success.periods.retrieved"));
    }

    public ApiResponse<EvaluationPeriodResponse> findPeriodById(Integer periodId) {
        Integer companyId = principalResolver.getCompanyId();
        return evaluationPeriodRepository.findByIdAndCompanyId(periodId, companyId)
            .map(period -> ApiResponse.success(
                evaluationPeriodConverter.toEvaluationPeriodResponse(period),
                messageHandler.getMessage("success.period.retrieved", periodId)
            ))
            .orElseGet(() -> ApiResponse.failure(
                messageHandler.getMessage("error.period.notFound", periodId)
            ));
    }

    @Transactional
    public ApiResponse<EvaluationPeriodResponse> updatePeriod(Integer periodId, EvaluationPeriodRequest request) {
        Integer companyId = principalResolver.getCompanyId();

        EvaluationPeriod existingPeriod = evaluationPeriodRepository.findByIdAndCompanyId(periodId, companyId).orElse(null);

        if (existingPeriod == null) {
            return ApiResponse.failure(messageHandler.getMessage("error.period.notFound", periodId));
        }

        existingPeriod.setName(request.getName());
        existingPeriod.setStartDate(request.getStartDate());
        existingPeriod.setEndDate(request.getEndDate());

        EvaluationPeriod updatedPeriod = evaluationPeriodRepository.save(existingPeriod);
        return ApiResponse.success(
            evaluationPeriodConverter.toEvaluationPeriodResponse(updatedPeriod),
            messageHandler.getMessage("success.period.updated", periodId)
        );
    }

    @Transactional
    public ApiResponse<Void> deletePeriod(Integer periodId) {
        Integer companyId = principalResolver.getCompanyId();

        if (!evaluationPeriodRepository.existsByIdAndCompanyId(periodId, companyId)) {
            return ApiResponse.failure(messageHandler.getMessage("error.period.notFound", periodId));
        }

        // TODO check before deletion

        evaluationPeriodRepository.deleteById(periodId);
        return ApiResponse.success(null, messageHandler.getMessage("success.period.deleted", periodId));
    }
}
