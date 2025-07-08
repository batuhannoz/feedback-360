package com.batuhan.feedback360.controller;

import com.batuhan.feedback360.model.request.EvaluationTemplateRequest;
import com.batuhan.feedback360.model.request.QuestionRequest;
import com.batuhan.feedback360.model.request.SetTemplateVisibilityRequest;
import com.batuhan.feedback360.model.response.EvaluationTemplateResponse;
import com.batuhan.feedback360.service.EvaluationTemplateService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/evaluation/template")
public class EvaluationTemplateController {

    private final EvaluationTemplateService templateService;

    @GetMapping
    @PreAuthorize("hasAnyRole('COMPANY', 'ADMIN')")
    public ResponseEntity<List<EvaluationTemplateResponse>> listTemplates() {
        List<EvaluationTemplateResponse> templates = templateService.listTemplates();
        return ResponseEntity.ok(templates);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('COMPANY', 'ADMIN')")
    public ResponseEntity<EvaluationTemplateResponse> createTemplate(@RequestBody EvaluationTemplateRequest request) {
        EvaluationTemplateResponse createdTemplate = templateService.createTemplate(request);
        return new ResponseEntity<>(createdTemplate, HttpStatus.CREATED);
    }

    @PutMapping("/{templateId}")
    @PreAuthorize("hasAnyRole('COMPANY', 'ADMIN')")
    public ResponseEntity<EvaluationTemplateResponse> updateTemplate(@PathVariable Integer templateId, @RequestBody EvaluationTemplateRequest request) {
        EvaluationTemplateResponse updatedTemplate = templateService.updateTemplate(templateId, request);
        return ResponseEntity.ok(updatedTemplate);
    }

    @PostMapping("/{templateId}/question")
    @PreAuthorize("hasAnyRole('COMPANY', 'ADMIN')")
    public ResponseEntity<EvaluationTemplateResponse> addQuestionToTemplate(@PathVariable Integer templateId, @RequestBody QuestionRequest request) {
        EvaluationTemplateResponse updatedTemplate = templateService.addQuestion(templateId, request);
        return ResponseEntity.ok(updatedTemplate);
    }

    @DeleteMapping("/{templateId}/question/{questionId}")
    @PreAuthorize("hasAnyRole('COMPANY', 'ADMIN')")
    public ResponseEntity<Void> removeQuestionFromTemplate(@PathVariable Integer templateId, @PathVariable Integer questionId) {
        templateService.removeQuestion(templateId, questionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{templateId}/visibility")
    @PreAuthorize("hasAnyRole('COMPANY', 'ADMIN')")
    public ResponseEntity<?> setTemplateVisibility(@PathVariable Integer templateId, @RequestBody List<SetTemplateVisibilityRequest> request) {
        templateService.setVisibility(templateId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{templateId}")
    @PreAuthorize("hasAnyRole('COMPANY', 'ADMIN')")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Integer templateId) {
        templateService.deleteTemplate(templateId);
        return ResponseEntity.noContent().build();
    }
}
