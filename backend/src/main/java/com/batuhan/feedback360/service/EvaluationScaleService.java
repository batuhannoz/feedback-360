package com.batuhan.feedback360.service;

import com.batuhan.feedback360.config.AuthenticationPrincipalResolver;
import com.batuhan.feedback360.model.entitiy.Company;
import com.batuhan.feedback360.model.entitiy.EvaluationScale;
import com.batuhan.feedback360.model.entitiy.ScaleOption;
import com.batuhan.feedback360.model.request.EvaluationScaleRequest;
import com.batuhan.feedback360.model.response.ApiResponse;
import com.batuhan.feedback360.model.response.EvaluationScaleResponse;
import com.batuhan.feedback360.model.response.ScaleOptionResponse;
import com.batuhan.feedback360.repository.EvaluationScaleRepository;
import com.batuhan.feedback360.repository.QuestionRepository;
import com.batuhan.feedback360.util.MessageHandler;
import jakarta.transaction.Transactional;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EvaluationScaleService {

    private final EvaluationScaleRepository evaluationScaleRepository;
    private final QuestionRepository questionRepository;
    private final AuthenticationPrincipalResolver principalResolver;
    private final MessageHandler messageHandler;

    public ApiResponse<List<EvaluationScaleResponse>> getAllScalesForCompany() {
        Integer companyId = principalResolver.getCompanyId();
        List<EvaluationScale> scales = evaluationScaleRepository.findAllByCompanyId(companyId);
        List<EvaluationScaleResponse> response = scales.stream()
            .map(this::toResponse)
            .toList();
        return ApiResponse.success(response, messageHandler.getMessage("evaluation-scale.list.success"));
    }

    public ApiResponse<EvaluationScaleResponse> getScaleById(Integer scaleId) {
        return evaluationScaleRepository.findByIdAndCompanyId(scaleId, principalResolver.getCompanyId())
            .map(scale -> ApiResponse.success(toResponse(scale), messageHandler.getMessage("evaluation-scale.get.success")))
            .orElse(ApiResponse.failure(messageHandler.getMessage("evaluation-scale.not-found")));
    }

    @Transactional
    public ApiResponse<EvaluationScaleResponse> createScale(EvaluationScaleRequest request) {
        Company company = new Company();
        company.setId(principalResolver.getCompanyId());

        EvaluationScale newScale = EvaluationScale.builder()
            .name(request.getName())
            .company(company)
            .build();

        List<ScaleOption> options = request.getOptions().stream()
            .map(optReq -> ScaleOption.builder()
                .score(optReq.getScore())
                .label(optReq.getLabel())
                .scale(newScale) // İlişkiyi kuruyoruz
                .build())
            .toList();

        newScale.setOptions(options);
        EvaluationScale savedScale = evaluationScaleRepository.save(newScale);

        return ApiResponse.success(toResponse(savedScale), messageHandler.getMessage("evaluation-scale.create.success"));
    }

    @Transactional
    public ApiResponse<EvaluationScaleResponse> updateScale(Integer scaleId, EvaluationScaleRequest request) {
        Optional<EvaluationScale> scaleOpt = evaluationScaleRepository.findByIdAndCompanyId(scaleId, principalResolver.getCompanyId());
        if (scaleOpt.isEmpty()) {
            return ApiResponse.failure(messageHandler.getMessage("evaluation-scale.not-found"));
        }

        EvaluationScale scaleToUpdate = scaleOpt.get();
        scaleToUpdate.setName(request.getName());

        scaleToUpdate.getOptions().clear();

        List<ScaleOption> newOptions = request.getOptions().stream()
            .map(optReq -> ScaleOption.builder()
                .score(optReq.getScore())
                .label(optReq.getLabel())
                .scale(scaleToUpdate)
                .build())
            .toList();

        scaleToUpdate.getOptions().addAll(newOptions);

        EvaluationScale updatedScale = evaluationScaleRepository.save(scaleToUpdate);
        return ApiResponse.success(toResponse(updatedScale), messageHandler.getMessage("evaluation-scale.update.success"));
    }

    @Transactional
    public ApiResponse<Object> deleteScale(Integer scaleId) {
        if (!evaluationScaleRepository.existsByIdAndCompanyId(scaleId, principalResolver.getCompanyId())) {
            return ApiResponse.failure(messageHandler.getMessage("evaluation-scale.not-found"));
        }

        if (questionRepository.existsByEvaluationScaleId(scaleId)) {
            return ApiResponse.failure(messageHandler.getMessage("evaluation-scale.in-use"));
        }

        evaluationScaleRepository.deleteById(scaleId);
        return ApiResponse.success(null, messageHandler.getMessage("evaluation-scale.delete.success"));
    }

    private EvaluationScaleResponse toResponse(EvaluationScale scale) {
        return EvaluationScaleResponse.builder()
            .id(scale.getId())
            .name(scale.getName())
            .options(scale.getOptions().stream()
                .map(opt -> new ScaleOptionResponse(opt.getScore(), opt.getLabel()))
                .sorted(Comparator.comparingInt(ScaleOptionResponse::getScore))
                .toList())
            .build();
    }
}
