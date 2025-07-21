package com.batuhan.feedback360.service;

import com.batuhan.feedback360.config.AuthenticationPrincipalResolver;
import com.batuhan.feedback360.model.converter.QuestionConverter;
import com.batuhan.feedback360.model.entitiy.Competency;
import com.batuhan.feedback360.model.entitiy.PeriodCompetencyWeight;
import com.batuhan.feedback360.model.entitiy.Question;
import com.batuhan.feedback360.model.request.QuestionRequest;
import com.batuhan.feedback360.model.response.ApiResponse;
import com.batuhan.feedback360.model.response.QuestionResponse;
import com.batuhan.feedback360.repository.EvaluationPeriodRepository;
import com.batuhan.feedback360.repository.PeriodCompetencyWeightRepository;
import com.batuhan.feedback360.repository.QuestionRepository;
import com.batuhan.feedback360.util.MessageHandler;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class CompetencyQuestionService {

    private final QuestionRepository questionRepository;
    private final QuestionConverter questionConverter;
    private final MessageHandler messageHandler;
    private final AuthenticationPrincipalResolver principalResolver;
    private final PeriodCompetencyWeightRepository periodCompetencyWeightRepository;
    private final EvaluationPeriodRepository evaluationPeriodRepository;

    public ApiResponse<List<QuestionResponse>> getQuestionsForCompetency(Integer periodId, Integer competencyId) {
        Optional<Competency> competencyOpt = validatePeriodAndCompetency(periodId, competencyId);
        if (competencyOpt.isEmpty()) {
            return ApiResponse.failure(messageHandler.getMessage("competency.not-found-in-period", competencyId));
        }
        List<Question> questions = questionRepository.findByCompetency_Id(competencyId);
        List<QuestionResponse> response = questions.stream()
            .map(questionConverter::toQuestionResponse)
            .collect(Collectors.toList());

        return ApiResponse.success(response, messageHandler.getMessage("question.list.success"));
    }

    public ApiResponse<QuestionResponse> addQuestionToCompetency(Integer periodId, Integer competencyId, QuestionRequest request) {
        Optional<Competency> competencyOpt = validatePeriodAndCompetency(periodId, competencyId);
        if (competencyOpt.isEmpty()) {
            return ApiResponse.failure(messageHandler.getMessage("competency.not-found-in-period", competencyId));
        }
        Competency competency = competencyOpt.get();

        Question newQuestion = Question.builder()
            .questionText(request.getQuestionText())
            .competency(competency)
            .company(competency.getCompany())
            .build();

        Question savedQuestion = questionRepository.save(newQuestion);
        return ApiResponse.success(
            questionConverter.toQuestionResponse(savedQuestion),
            messageHandler.getMessage("question.add.success")
        );
    }

    @Transactional
    public ApiResponse<QuestionResponse> updateQuestion(Integer periodId, Integer competencyId, Integer questionId, QuestionRequest request) {
        if (validatePeriodAndCompetency(periodId, competencyId).isEmpty()) {
            return ApiResponse.failure(messageHandler.getMessage("competency.not-found-in-period", competencyId));
        }
        return questionRepository.findByIdAndCompetency_Id(questionId, competencyId)
            .map(questionToUpdate -> {
                questionToUpdate.setQuestionText(request.getQuestionText());
                Question updatedQuestion = questionRepository.save(questionToUpdate);
                return ApiResponse.success(
                    questionConverter.toQuestionResponse(updatedQuestion),
                    messageHandler.getMessage("question.update.success")
                );
            })
            .orElse(ApiResponse.failure(messageHandler.getMessage("question.not-found")));
    }

    @Transactional
    public ApiResponse<Object> deleteQuestion(Integer periodId, Integer competencyId, Integer questionId) {
        if (validatePeriodAndCompetency(periodId, competencyId).isEmpty()) {
            return ApiResponse.failure(messageHandler.getMessage("competency.not-found-in-period", competencyId));
        }
        return questionRepository.findByIdAndCompetency_Id(questionId, competencyId)
            .map(question -> {
                questionRepository.delete(question);
                return ApiResponse.success(null, messageHandler.getMessage("question.delete.success"));
            })
            .orElse(ApiResponse.failure(messageHandler.getMessage("question.not-found")));
    }

    private Optional<Competency> validatePeriodAndCompetency(Integer periodId, Integer competencyId) {
        if (!evaluationPeriodRepository.existsByIdAndCompanyId(periodId, principalResolver.getCompanyId())) {
            return Optional.empty();
        }
        return periodCompetencyWeightRepository.findByPeriod_IdAndCompetency_Id(periodId, competencyId)
            .map(PeriodCompetencyWeight::getCompetency);
    }
}
