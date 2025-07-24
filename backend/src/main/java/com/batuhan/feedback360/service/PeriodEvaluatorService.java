package com.batuhan.feedback360.service;

import com.batuhan.feedback360.config.AuthenticationPrincipalResolver;
import com.batuhan.feedback360.model.converter.EvaluatorConverter;
import com.batuhan.feedback360.model.entitiy.Evaluator;
import com.batuhan.feedback360.model.enums.EvaluatorType;
import com.batuhan.feedback360.model.request.EvaluatorRequest;
import com.batuhan.feedback360.model.response.ApiResponse;
import com.batuhan.feedback360.model.response.EvaluatorResponse;
import com.batuhan.feedback360.repository.EvaluationPeriodRepository;
import com.batuhan.feedback360.repository.EvaluatorRepository;
import com.batuhan.feedback360.util.MessageHandler;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PeriodEvaluatorService {

    private final EvaluationPeriodRepository evaluationPeriodRepository;
    private final EvaluatorConverter evaluatorConverter;
    private final MessageHandler messageHandler;
    private final AuthenticationPrincipalResolver principalResolver;
    private final EvaluatorRepository evaluatorRepository;

    public ApiResponse<List<EvaluatorResponse>> getEvaluators(Integer periodId) {
        List<Evaluator> evaluators = evaluatorRepository.findAllByPeriod_Id(periodId);
        List<EvaluatorResponse> evaluatorResponses = evaluators.stream()
            .map(evaluatorConverter::toEvaluatorResponse).toList();

        return ApiResponse.success(evaluatorResponses,
            messageHandler.getMessage("evaluation-period.evaluators.success"));
    }

    public ApiResponse<List<EvaluatorResponse>> setEvaluators(Integer periodId, List<EvaluatorRequest> request) {
        if (!evaluationPeriodRepository.existsByIdAndCompanyId(periodId, principalResolver.getCompanyId())) {
            return ApiResponse.failure(messageHandler.getMessage("evaluation-period.no-permission-update"));
        }
        List<Evaluator> existingEvaluators = evaluatorRepository.findAllByPeriod_Id(periodId);
        if (existingEvaluators.isEmpty()) {
            return ApiResponse.failure(messageHandler.getMessage("evaluation-period.evaluators.not-found"));
        }
        Map<EvaluatorType, Evaluator> evaluatorMap = existingEvaluators.stream()
            .collect(Collectors.toMap(Evaluator::getEvaluatorType, evaluator -> evaluator));

        request.forEach(req -> {
            if (req.getEvaluatorType() != null) {
                Evaluator evaluatorToUpdate = evaluatorMap.get(req.getEvaluatorType());
                if (evaluatorToUpdate != null) {
                    evaluatorToUpdate.setName(req.getName());
                }
            }
        });
        List<Evaluator> updatedEvaluators = evaluatorRepository.saveAll(evaluatorMap.values());
        List<EvaluatorResponse> updatedEvaluatorsResponse = updatedEvaluators.stream()
            .map(evaluatorConverter::toEvaluatorResponse)
            .collect(Collectors.toList());

        return ApiResponse.success(updatedEvaluatorsResponse, messageHandler.getMessage("evaluation-period.evaluators.update.success"));
    }
}
