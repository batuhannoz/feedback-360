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
import com.batuhan.feedback360.model.response.EvaluationPeriodResponse;
import com.batuhan.feedback360.repository.CompanyRepository;
import com.batuhan.feedback360.repository.EmployeeRepository;
import com.batuhan.feedback360.repository.EvaluationPeriodRepository;
import com.batuhan.feedback360.repository.EvaluationRepository;
import com.batuhan.feedback360.repository.EvaluationTemplateRepository;
import com.batuhan.feedback360.repository.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
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
    private final RoleRepository roleRepository;
    private final EvaluationRepository evaluationRepository;
    private final CompanyRepository companyRepository;

    @Transactional
    public EvaluationPeriodResponse createPeriod(EvaluationPeriodRequest request) {
        Company company = companyRepository.getReferenceById(principalResolver.getCompanyId());

        Set<EvaluationTemplate> templatesForPeriod = new HashSet<>(
            evaluationTemplateRepository.findAllById(request.getTemplateIds())
        );
        if (templatesForPeriod.isEmpty()) {
            throw new IllegalArgumentException("Değerlendirme dönemi en az bir şablon içermelidir.");
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
        return evaluationPeriodConverter.toEvaluationPeriodResponse(evaluationPeriod);
    }

    public List<EvaluationPeriodResponse> findAllPeriodsByCompany() {
        Integer companyId = principalResolver.getCompanyId();
        List<EvaluationPeriod> periods = evaluationPeriodRepository.findByCompanyId(companyId);
        return periods.stream()
            .map(evaluationPeriodConverter::toEvaluationPeriodResponse)
            .collect(Collectors.toList());
    }

    public EvaluationPeriodResponse findPeriodById(Integer periodId) {
        Integer companyId = principalResolver.getCompanyId();
        EvaluationPeriod period = evaluationPeriodRepository.findByIdAndCompanyId(periodId, companyId)
            .orElseThrow(() -> new EntityNotFoundException("Evaluation period not found with id: " + periodId));
        return evaluationPeriodConverter.toEvaluationPeriodResponse(period);
    }

    @Transactional
    public EvaluationPeriodResponse updatePeriod(Integer periodId, EvaluationPeriodRequest evaluationPeriodRequest) {
        Integer companyId = principalResolver.getCompanyId();

        EvaluationPeriod existingPeriod = evaluationPeriodRepository.findByIdAndCompanyId(periodId, companyId)
            .orElseThrow(() -> new EntityNotFoundException("Evaluation period not found with id: " + periodId));

        existingPeriod.setName(evaluationPeriodRequest.getName());
        existingPeriod.setStartDate(evaluationPeriodRequest.getStartDate());
        existingPeriod.setEndDate(evaluationPeriodRequest.getEndDate());

        evaluationPeriodRepository.save(existingPeriod);
        return evaluationPeriodConverter.toEvaluationPeriodResponse(existingPeriod);
    }

    @Transactional
    public void deletePeriod(Integer periodId) {
        Integer companyId = principalResolver.getCompanyId();

        if (!evaluationPeriodRepository.existsByIdAndCompanyId(periodId, companyId)) {
            throw new EntityNotFoundException("Evaluation period not found with id: " + periodId);
        }

        // TODO: check before deleting it

        evaluationPeriodRepository.deleteById(periodId);
    }
}
