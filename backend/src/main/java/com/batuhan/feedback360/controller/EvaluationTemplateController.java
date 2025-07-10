package com.batuhan.feedback360.controller;

import com.batuhan.feedback360.model.request.EvaluationTemplateRequest;
import com.batuhan.feedback360.model.request.QuestionRequest;
import com.batuhan.feedback360.model.request.SetTemplateVisibilityRequest;
import com.batuhan.feedback360.model.response.ApiResponse;
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
@PreAuthorize("hasAnyRole('COMPANY', 'ADMIN')")
@RequestMapping("/api/v1/evaluation/template")
public class EvaluationTemplateController {

    private final EvaluationTemplateService templateService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<EvaluationTemplateResponse>>> listTemplates() {
        return ResponseEntity.ok(templateService.listTemplates());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EvaluationTemplateResponse>> createTemplate(@RequestBody EvaluationTemplateRequest request) {
        return new ResponseEntity<>(templateService.createTemplate(request), HttpStatus.CREATED);
    }

    @PutMapping("/{templateId}")
    public ResponseEntity<ApiResponse<EvaluationTemplateResponse>> updateTemplate(@PathVariable Integer templateId, @RequestBody EvaluationTemplateRequest request) {
        return ResponseEntity.ok(templateService.updateTemplate(templateId, request));
    }

    @PostMapping("/{templateId}/question")
    public ResponseEntity<ApiResponse<EvaluationTemplateResponse>> addQuestionToTemplate(@PathVariable Integer templateId, @RequestBody QuestionRequest request) {
        return ResponseEntity.ok(templateService.addQuestion(templateId, request));
    }

    @DeleteMapping("/{templateId}/question/{questionId}")
    public ResponseEntity<Void> removeQuestionFromTemplate(@PathVariable Integer templateId, @PathVariable Integer questionId) {
        templateService.removeQuestion(templateId, questionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{templateId}/visibility")
    public ResponseEntity<ApiResponse<Void>> setTemplateVisibility(@PathVariable Integer templateId, @RequestBody List<SetTemplateVisibilityRequest> request) {
        return ResponseEntity.ok(templateService.setVisibility(templateId, request));
    }

    @DeleteMapping("/{templateId}")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(@PathVariable Integer templateId) {
        return ResponseEntity.ok(templateService.deleteTemplate(templateId));
    }
}
