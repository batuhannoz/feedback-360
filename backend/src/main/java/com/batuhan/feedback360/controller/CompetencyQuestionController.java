package com.batuhan.feedback360.controller;

import com.batuhan.feedback360.model.request.QuestionRequest;
import com.batuhan.feedback360.model.response.ApiResponse;
import com.batuhan.feedback360.model.response.QuestionResponse;
import com.batuhan.feedback360.service.CompetencyQuestionService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/v1/period/{periodId}/competency/{competencyId}/question")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CompetencyQuestionController {

    private final CompetencyQuestionService competencyQuestionService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<QuestionResponse>>> getQuestionsForCompetency(
        @PathVariable Integer periodId,
        @PathVariable Integer competencyId
    ) {
        return ResponseEntity.ok(competencyQuestionService.getQuestionsForCompetency(periodId, competencyId));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<QuestionResponse>> addQuestionToCompetency(
        @PathVariable Integer periodId,
        @PathVariable Integer competencyId,
        @Valid @RequestBody QuestionRequest request
    ) {
        return ResponseEntity.ok(competencyQuestionService.addQuestionToCompetency(periodId, competencyId, request));
    }

    @PutMapping("/{questionId}")
    public ResponseEntity<ApiResponse<QuestionResponse>> updateQuestion(
        @PathVariable Integer periodId,
        @PathVariable Integer competencyId,
        @PathVariable Integer questionId,
        @Valid @RequestBody QuestionRequest request
    ) {
        return ResponseEntity.ok(competencyQuestionService.updateQuestion(periodId, competencyId, questionId, request));
    }

    @DeleteMapping("/{questionId}")
    public ResponseEntity<ApiResponse<Object>> deleteQuestion(
        @PathVariable Integer periodId,
        @PathVariable Integer competencyId,
        @PathVariable Integer questionId
    ) {
        return ResponseEntity.ok(competencyQuestionService.deleteQuestion(periodId, competencyId, questionId));
    }
}
