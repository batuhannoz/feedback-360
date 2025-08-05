package com.batuhan.feedback360.controller;

import com.batuhan.feedback360.model.request.EvaluationScaleRequest;
import com.batuhan.feedback360.model.response.ApiResponse;
import com.batuhan.feedback360.model.response.EvaluationScaleResponse;
import com.batuhan.feedback360.service.EvaluationScaleService;
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
@RequestMapping("/api/v1/scales")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class EvaluationScaleController {

    private final EvaluationScaleService evaluationScaleService;

    @PostMapping
    public ResponseEntity<ApiResponse<EvaluationScaleResponse>> createScale(@Valid @RequestBody EvaluationScaleRequest request) {
        return ResponseEntity.ok(evaluationScaleService.createScale(request));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EvaluationScaleResponse>>> getAllScales() {
        return ResponseEntity.ok(evaluationScaleService.getAllScalesForCompany());
    }

    @GetMapping("/{scaleId}")
    public ResponseEntity<ApiResponse<EvaluationScaleResponse>> getScaleById(@PathVariable Integer scaleId) {
        return ResponseEntity.ok(evaluationScaleService.getScaleById(scaleId));
    }

    @PutMapping("/{scaleId}")
    public ResponseEntity<ApiResponse<EvaluationScaleResponse>> updateScale(
        @PathVariable Integer scaleId,
        @Valid @RequestBody EvaluationScaleRequest request
    ) {
        return ResponseEntity.ok(evaluationScaleService.updateScale(scaleId, request));
    }

    @DeleteMapping("/{scaleId}")
    public ResponseEntity<ApiResponse<Object>> deleteScale(@PathVariable Integer scaleId) {
        return ResponseEntity.ok(evaluationScaleService.deleteScale(scaleId));
    }
}
