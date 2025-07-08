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
import com.batuhan.feedback360.model.response.ApiResponse;
import com.batuhan.feedback360.model.response.EvaluationTemplateResponse;
import com.batuhan.feedback360.repository.CompanyRepository;
import com.batuhan.feedback360.repository.EvaluationTemplateRepository;
import com.batuhan.feedback360.repository.QuestionRepository;
import com.batuhan.feedback360.repository.RoleRepository;
import com.batuhan.feedback360.util.MessageHandler;
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
    private final MessageHandler messageHandler;

    public ApiResponse<List<EvaluationTemplateResponse>> listTemplates() {
        Company company = companyRepository.getReferenceById(principalResolver.getCompanyId());
        List<EvaluationTemplate> templates = templateRepository.findAllByCompany(company);
        List<EvaluationTemplateResponse> responseData = templates.stream()
            .map(templateConverter::toTemplateResponse)
            .collect(Collectors.toList());
        return ApiResponse.success(responseData, messageHandler.getMessage("success.templates.retrieved"));
    }

    @Transactional
    public ApiResponse<EvaluationTemplateResponse> createTemplate(EvaluationTemplateRequest request) {
        Integer companyId = principalResolver.getCompanyId();

        Role targetRole = roleRepository.findByIdAndCompanyId(request.getTargetRoleId(), companyId).orElse(null);
        if (targetRole == null) {
            return ApiResponse.failure(messageHandler.getMessage("error.role.notFound", request.getTargetRoleId()));
        }

        EvaluationTemplate template = EvaluationTemplate.builder()
            .name(request.getName())
            .description(request.getDescription())
            .targetRole(targetRole)
            .company(new Company(companyId))
            .build();

        EvaluationTemplate savedTemplate = templateRepository.save(template);
        return ApiResponse.success(
            templateConverter.toTemplateResponse(savedTemplate),
            messageHandler.getMessage("success.template.created", savedTemplate.getName())
        );
    }

    @Transactional
    public ApiResponse<EvaluationTemplateResponse> addQuestion(Integer templateId, QuestionRequest request) {
        Integer companyId = principalResolver.getCompanyId();

        EvaluationTemplate template = templateRepository.findByIdAndCompanyId(templateId, companyId).orElse(null);
        if (template == null) {
            return ApiResponse.failure(messageHandler.getMessage("error.template.notFound", templateId));
        }

        Question question = Question.builder()
            .question(request.getQuestion())
            .type(request.getType())
            .company(new Company(companyId))
            .build();

        // The association should be managed by the owning side (template)
        template.getQuestions().add(question);
        EvaluationTemplate updatedTemplate = templateRepository.save(template);

        return ApiResponse.success(
            templateConverter.toTemplateResponse(updatedTemplate),
            messageHandler.getMessage("success.question.added", templateId)
        );
    }

    @Transactional
    public ApiResponse<Void> removeQuestion(Integer templateId, Integer questionId) {
        Integer companyId = principalResolver.getCompanyId();

        EvaluationTemplate template = templateRepository.findByIdAndCompanyId(templateId, companyId).orElse(null);
        if (template == null) {
            return ApiResponse.failure(messageHandler.getMessage("error.template.notFound", templateId));
        }

        boolean removed = template.getQuestions().removeIf(question -> question.getId().equals(questionId));
        if (!removed) {
            return ApiResponse.failure(messageHandler.getMessage("error.question.notFoundInTemplate", questionId, templateId));
        }

        templateRepository.save(template);
        return ApiResponse.success(null, messageHandler.getMessage("success.question.removed", questionId, templateId));
    }

    @Transactional
    public ApiResponse<Void> setVisibility(Integer templateId, List<SetTemplateVisibilityRequest> requests) {
        Integer companyId = principalResolver.getCompanyId();

        EvaluationTemplate template = templateRepository.findByIdAndCompanyId(templateId, companyId).orElse(null);
        if (template == null) {
            return ApiResponse.failure(messageHandler.getMessage("error.template.notFound", templateId));
        }

        Set<Integer> evaluatorRoleIds = requests.stream()
            .map(SetTemplateVisibilityRequest::getEvaluatorRoleId)
            .collect(Collectors.toSet());

        if (evaluatorRoleIds.isEmpty()) {
            template.getEvaluatorRoles().clear();
        } else {
            Set<Role> evaluatorRoles = roleRepository.findAllByIdInAndCompanyId(evaluatorRoleIds, companyId);
            if (evaluatorRoles.size() != evaluatorRoleIds.size()) {
                return ApiResponse.failure(messageHandler.getMessage("error.role.someNotFoundInCompany"));
            }
            template.setEvaluatorRoles(evaluatorRoles);
        }

        templateRepository.save(template);
        return ApiResponse.success(null, messageHandler.getMessage("success.template.visibilityUpdated", templateId));
    }

    @Transactional
    public ApiResponse<EvaluationTemplateResponse> updateTemplate(Integer templateId, EvaluationTemplateRequest request) {
        Integer companyId = principalResolver.getCompanyId();

        EvaluationTemplate template = templateRepository.findByIdAndCompanyId(templateId, companyId).orElse(null);
        if (template == null) {
            return ApiResponse.failure(messageHandler.getMessage("error.template.notFound", templateId));
        }

        Role targetRole = roleRepository.findByIdAndCompanyId(request.getTargetRoleId(), companyId).orElse(null);
        if (targetRole == null) {
            return ApiResponse.failure(messageHandler.getMessage("error.role.notFound", request.getTargetRoleId()));
        }

        template.setName(request.getName());
        template.setDescription(request.getDescription());
        template.setTargetRole(targetRole);

        EvaluationTemplate updatedTemplate = templateRepository.save(template);
        return ApiResponse.success(
            templateConverter.toTemplateResponse(updatedTemplate),
            messageHandler.getMessage("success.template.updated", templateId)
        );
    }

    @Transactional
    public ApiResponse<Void> deleteTemplate(Integer templateId) {
        Integer companyId = principalResolver.getCompanyId();

        EvaluationTemplate template = templateRepository.findByIdAndCompanyId(templateId, companyId).orElse(null);
        if (template == null) {
            return ApiResponse.failure(messageHandler.getMessage("error.template.notFound", templateId));
        }

        // TODO check before delete

        templateRepository.deleteById(templateId);
        return ApiResponse.success(null, messageHandler.getMessage("success.template.deleted", templateId));
    }
}
