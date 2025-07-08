package com.batuhan.feedback360.service;

import com.batuhan.feedback360.config.AuthenticationPrincipalResolver;
import com.batuhan.feedback360.model.converter.EvaluationTemplateConverter;
import com.batuhan.feedback360.model.entitiy.Company;
import com.batuhan.feedback360.model.entitiy.EvaluationTemplate;
import com.batuhan.feedback360.model.entitiy.Question;
import com.batuhan.feedback360.model.entitiy.Role;
import com.batuhan.feedback360.model.request.EvaluationTemplateRequest;
import com.batuhan.feedback360.model.request.QuestionRequest;
import com.batuhan.feedback360.model.request.SetTemplateVisibilityRequest;
import com.batuhan.feedback360.model.response.EvaluationTemplateResponse;
import com.batuhan.feedback360.repository.CompanyRepository;
import com.batuhan.feedback360.repository.EvaluationTemplateRepository;
import com.batuhan.feedback360.repository.QuestionRepository;
import com.batuhan.feedback360.repository.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EvaluationTemplateService {

    private final EvaluationTemplateRepository templateRepository;
    private final RoleRepository roleRepository;
    private final QuestionRepository questionRepository;
    private final AuthenticationPrincipalResolver principalResolver;
    private final EvaluationTemplateConverter templateConverter;
    private final CompanyRepository companyRepository;

    public List<EvaluationTemplateResponse> listTemplates() {
        Company company = companyRepository.getReferenceById(principalResolver.getCompanyId());

        List<EvaluationTemplate> templates = templateRepository.findAllByCompany(company);

        return templates.stream()
            .map(templateConverter::toTemplateResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public EvaluationTemplateResponse createTemplate(EvaluationTemplateRequest request) {
        Integer companyId = principalResolver.getCompanyId();

        Role targetRole = roleRepository.findByIdAndCompanyId(request.getTargetRoleId(), companyId)
            .orElseThrow(() -> new EntityNotFoundException("Target role not found with id: " + request.getTargetRoleId()));

        EvaluationTemplate template = EvaluationTemplate.builder()
            .name(request.getName())
            .description(request.getDescription())
            .targetRole(targetRole)
            .company(new Company(companyId))
            .build();

        templateRepository.save(template);

        return templateConverter.toTemplateResponse(template);
    }

    public EvaluationTemplateResponse addQuestion(Integer templateId, QuestionRequest request) {
        Integer companyId = principalResolver.getCompanyId();

        EvaluationTemplate template = templateRepository.findByIdAndCompanyId(templateId, companyId)
            .orElseThrow(() -> new EntityNotFoundException("Template not found with id: " + templateId));

        Question question = Question.builder()
            .question(request.getQuestion())
            .type(request.getType())
            .evaluationTemplates(template)
            .company(new Company(companyId))
            .build();

        question = questionRepository.save(question);

        template.getQuestions().add(question);
        templateRepository.save(template);

        return templateConverter.toTemplateResponse(template);
    }

    @Transactional
    public void removeQuestion(Integer templateId, Integer questionId) {
        Integer companyId = principalResolver.getCompanyId();

        EvaluationTemplate template = templateRepository.findByIdAndCompanyId(templateId, companyId)
            .orElseThrow(() -> new EntityNotFoundException("Template not found with id: " + templateId));

        boolean removed = template.getQuestions().removeIf(question -> question.getId().equals(questionId));
        if (!removed) {
            System.out.println("Question " + questionId + " not found in template " + templateId);
        }

        templateRepository.save(template);
    }

    @Transactional
    public void setVisibility(Integer templateId, List<SetTemplateVisibilityRequest> requests) {
        Integer companyId = principalResolver.getCompanyId();

        EvaluationTemplate template = templateRepository.findByIdAndCompanyId(templateId, companyId)
            .orElseThrow(() -> new EntityNotFoundException("Template not found with id: " + templateId));

        Set<Integer> evaluatorRoleIds = requests.stream()
            .map(SetTemplateVisibilityRequest::getEvaluatorRoleId)
            .collect(Collectors.toSet());

        if (evaluatorRoleIds.isEmpty()) {
            template.getEvaluatorRoles().clear();
            templateRepository.save(template);
            return;
        }

        Set<Role> evaluatorRoles = roleRepository.findAllByIdInAndCompanyId(evaluatorRoleIds, companyId);

        if (evaluatorRoles.size() != evaluatorRoleIds.size()) {
            throw new EntityNotFoundException("One or more evaluator roles not found or do not belong to the company.");
        }
        template.setEvaluatorRoles(evaluatorRoles);
        templateRepository.save(template);
    }

    @Transactional
    public EvaluationTemplateResponse updateTemplate(Integer templateId, EvaluationTemplateRequest request) {
        Integer companyId = principalResolver.getCompanyId();

        EvaluationTemplate template = templateRepository.findByIdAndCompanyId(templateId, companyId)
            .orElseThrow(() -> new EntityNotFoundException("Template not found with id: " + templateId));

        Role targetRole = roleRepository.findByIdAndCompanyId(request.getTargetRoleId(), companyId)
            .orElseThrow(() -> new EntityNotFoundException("Target role not found with id: " + request.getTargetRoleId()));

        template.setName(request.getName());
        template.setDescription(request.getDescription());
        template.setTargetRole(targetRole);

        templateRepository.save(template);

        return templateConverter.toTemplateResponse(template);
    }

    @Transactional
    public void deleteTemplate(Integer templateId) {
        Integer companyId = principalResolver.getCompanyId();

        EvaluationTemplate template = templateRepository.findByIdAndCompanyId(templateId, companyId)
            .orElseThrow(() -> new EntityNotFoundException("Template not found with id: " + templateId));

        templateRepository.delete(template);
    }
}