package com.batuhan.feedback360.service;

import com.batuhan.feedback360.config.AuthenticationPrincipalResolver;
import com.batuhan.feedback360.model.converter.EvaluationPeriodConverter;
import com.batuhan.feedback360.model.converter.UserConverter;
import com.batuhan.feedback360.model.entitiy.Answer;
import com.batuhan.feedback360.model.entitiy.Competency;
import com.batuhan.feedback360.model.entitiy.CompetencyEvaluatorPermission;
import com.batuhan.feedback360.model.entitiy.EvaluationPeriod;
import com.batuhan.feedback360.model.entitiy.Evaluator;
import com.batuhan.feedback360.model.entitiy.PeriodCompetencyWeight;
import com.batuhan.feedback360.model.entitiy.User;
import com.batuhan.feedback360.model.enums.EvaluatorType;
import com.batuhan.feedback360.model.response.ApiResponse;
import com.batuhan.feedback360.model.response.CompetencyScoreDetailResponse;
import com.batuhan.feedback360.model.response.ScoreByEvaluatorResponse;
import com.batuhan.feedback360.model.response.UserPeriodReportResponse;
import com.batuhan.feedback360.repository.AnswerRepository;
import com.batuhan.feedback360.repository.CompetencyEvaluatorPermissionRepository;
import com.batuhan.feedback360.repository.EvaluationPeriodRepository;
import com.batuhan.feedback360.repository.PeriodCompetencyWeightRepository;
import com.batuhan.feedback360.repository.PeriodParticipantRepository;
import com.batuhan.feedback360.repository.UserRepository;
import com.batuhan.feedback360.util.MessageHandler;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class PeriodReportService {

    private final EvaluationPeriodRepository evaluationPeriodRepository;
    private final PeriodCompetencyWeightRepository periodCompetencyWeightRepository;
    private final PeriodParticipantService periodParticipantService;
    private final CompetencyEvaluatorPermissionRepository competencyEvaluatorPermissionRepository;
    private final UserRepository userRepository;
    private final AnswerRepository answerRepository;
    private final UserConverter userConverter;
    private final EvaluationPeriodConverter evaluationPeriodConverter;
    private final AuthenticationPrincipalResolver principalResolver;
    private final MessageHandler messageHandler;
    private final PeriodParticipantRepository periodParticipantRepository;


    @Transactional
    public ApiResponse<UserPeriodReportResponse> generateUserPeriodReport(Integer periodId, Integer evaluatedUserId) {
        EvaluationPeriod period = evaluationPeriodRepository.findByIdAndCompanyId(periodId, principalResolver.getCompanyId())
            .orElse(null);
        if (period == null) {
            return ApiResponse.failure(messageHandler.getMessage("evaluation-period.not-found"));
        }
        User evaluatedUser = userRepository.findByIdAndCompany(evaluatedUserId, period.getCompany())
            .orElse(null);
        if (evaluatedUser == null) {
            return ApiResponse.failure(messageHandler.getMessage("user.not-found"));
        }
        if (!periodParticipantRepository.existsByPeriod_IdAndEvaluatedUser_Id(periodId, evaluatedUserId)) {
            return ApiResponse.failure(messageHandler.getMessage("period.participant.not-found"));
        }

        List<Answer> allAnswers = answerRepository
            .findAllByAssignment_PeriodParticipant_Period_IdAndAssignment_PeriodParticipant_EvaluatedUser_Id(periodId, evaluatedUserId);

        if (allAnswers.isEmpty()) {
            return ApiResponse.failure(messageHandler.getMessage("report.no-answers-found"));
        }

        Map<Integer, BigDecimal> competencyWeights = periodCompetencyWeightRepository.findAllByPeriod_Id(periodId).stream()
            .collect(Collectors.toMap(pcw -> pcw.getCompetency().getId(), PeriodCompetencyWeight::getWeight));

        Map<Integer, Map<EvaluatorType, BigDecimal>> evaluatorWeights = competencyEvaluatorPermissionRepository.findAllByPeriodId(periodId).stream()
            .collect(Collectors.groupingBy(p -> p.getCompetency().getId(),
                Collectors.toMap(p -> p.getEvaluator().getEvaluatorType(), CompetencyEvaluatorPermission::getWeight)
            ));

        Map<Integer, List<Answer>> answersByCompetency = allAnswers.stream()
            .collect(Collectors.groupingBy(a -> a.getQuestion().getCompetency().getId()));

        List<CompetencyScoreDetailResponse> competencyScores = calculateCompetencyDetails(answersByCompetency, competencyWeights, evaluatorWeights);

        BigDecimal finalWeightedScore = competencyScores.stream()
            .map(CompetencyScoreDetailResponse::getFinalWeightedScore)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);

        BigDecimal competencyWeightedScore = competencyScores.stream()
            .map(cs -> cs.getRawAverageScore().multiply(
                competencyWeights.getOrDefault(cs.getCompetencyId(), BigDecimal.ZERO)
                    .divide(new BigDecimal(100), 4, RoundingMode.HALF_UP))
            )
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);

        UserPeriodReportResponse reportResponse = UserPeriodReportResponse.builder()
            .user(userConverter.toUserDetailResponse(evaluatedUser))
            .period(evaluationPeriodConverter.toEvaluationPeriodResponse(period))
            .rawAverageScore(calculateRawAverageScore(allAnswers))
            .scoresByEvaluator(calculateScoresByEvaluator(allAnswers))
            .competencyScores(competencyScores)
            .competencyWeightedScore(competencyWeightedScore)
            .finalWeightedScore(finalWeightedScore)
            .build();

        return ApiResponse.success(reportResponse, messageHandler.getMessage("report.generate.success"));
    }

    private BigDecimal calculateRawAverageScore(List<Answer> allAnswers) {
        return BigDecimal.valueOf(allAnswers.stream()
                .mapToInt(Answer::getScore)
                .average()
                .orElse(0.0))
            .setScale(2, RoundingMode.HALF_UP);
    }

    private List<ScoreByEvaluatorResponse> calculateScoresByEvaluator(List<Answer> allAnswers) {
        return allAnswers.stream()
            .collect(Collectors.groupingBy(a -> a.getAssignment().getEvaluator()))
            .entrySet().stream()
            .map(entry -> {
                Evaluator evaluator = entry.getKey();
                BigDecimal averageScore = BigDecimal.valueOf(entry.getValue().stream()
                        .mapToInt(Answer::getScore)
                        .average()
                        .orElse(0.0))
                    .setScale(2, RoundingMode.HALF_UP);

                return ScoreByEvaluatorResponse.builder()
                    .evaluatorName(evaluator.getName())
                    .evaluatorType(evaluator.getEvaluatorType())
                    .averageScore(averageScore)
                    .build();
            })
            .collect(Collectors.toList());
    }

    private List<CompetencyScoreDetailResponse> calculateCompetencyDetails(
        Map<Integer, List<Answer>> answersByCompetency,
        Map<Integer, BigDecimal> competencyWeights,
        Map<Integer, Map<EvaluatorType, BigDecimal>> evaluatorWeights) {

        return answersByCompetency.entrySet().stream()
            .map(entry -> {
                Integer competencyId = entry.getKey();
                List<Answer> competencyAnswers = entry.getValue();
                Competency competency = competencyAnswers.getFirst().getQuestion().getCompetency();

                BigDecimal rawAverageScore = calculateRawAverageScore(competencyAnswers);

                BigDecimal finalContribution = calculateFinalContributionForCompetency(
                    competencyAnswers,
                    competencyWeights.getOrDefault(competencyId, BigDecimal.ZERO),
                    evaluatorWeights.getOrDefault(competencyId, Collections.emptyMap())
                );

                return CompetencyScoreDetailResponse.builder()
                    .competencyId(competencyId)
                    .competencyTitle(competency.getTitle())
                    .rawAverageScore(rawAverageScore)
                    .finalWeightedScore(finalContribution)
                    .build();
            })
            .collect(Collectors.toList());
    }

    private BigDecimal calculateFinalContributionForCompetency(
        List<Answer> competencyAnswers,
        BigDecimal competencyWeight,
        Map<EvaluatorType, BigDecimal> currentEvaluatorWeights) {

        BigDecimal weightedScoreWithinCompetency = competencyAnswers.stream()
            .collect(Collectors.groupingBy(a -> a.getAssignment().getEvaluator().getEvaluatorType()))
            .entrySet().stream()
            .map(evaluatorEntry -> {
                EvaluatorType type = evaluatorEntry.getKey();
                BigDecimal evaluatorWeight = currentEvaluatorWeights.getOrDefault(type, BigDecimal.ZERO);

                BigDecimal avgScoreFromEvaluator = BigDecimal.valueOf(evaluatorEntry.getValue().stream()
                    .mapToInt(Answer::getScore)
                    .average().orElse(0.0));

                return avgScoreFromEvaluator.multiply(evaluatorWeight.divide(new BigDecimal(100), 4, RoundingMode.HALF_UP));
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return weightedScoreWithinCompetency.multiply(competencyWeight.divide(new BigDecimal(100), 4, RoundingMode.HALF_UP));
    }
}
