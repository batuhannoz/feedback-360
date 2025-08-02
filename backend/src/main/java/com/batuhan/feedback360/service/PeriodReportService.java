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
import com.batuhan.feedback360.model.entitiy.Question;
import com.batuhan.feedback360.model.entitiy.User;
import com.batuhan.feedback360.model.enums.EvaluatorType;
import com.batuhan.feedback360.model.request.ReportDisplaySettings;
import com.batuhan.feedback360.model.request.ReportGenerationRequest;
import com.batuhan.feedback360.model.response.ApiResponse;
import com.batuhan.feedback360.model.response.AverageScoreByEvaluatorTypeResponse;
import com.batuhan.feedback360.model.response.CommentResponse;
import com.batuhan.feedback360.model.response.CompetencyScoreDetailResponse;
import com.batuhan.feedback360.model.response.OverallScores;
import com.batuhan.feedback360.model.response.QuestionScoreDetailResponse;
import com.batuhan.feedback360.model.response.ScoresByEachEvaluatorType;
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
import java.util.ArrayList;
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
    private final CompetencyEvaluatorPermissionRepository competencyEvaluatorPermissionRepository;
    private final UserRepository userRepository;
    private final AnswerRepository answerRepository;
    private final UserConverter userConverter;
    private final EvaluationPeriodConverter evaluationPeriodConverter;
    private final AuthenticationPrincipalResolver principalResolver;
    private final MessageHandler messageHandler;
    private final PeriodParticipantRepository periodParticipantRepository;

    public static ReportDisplaySettings createDefaultReportSettings() {
        return ReportDisplaySettings.builder()
            .competencyDefinitionTitle("Yetkinlik Nedir?")
            .competencyDefinitionText("Bilgi, beceri, tutum ve davranışlarla...")
            .competencySourceWeightsTitle("Yetkinlik Bazlı Kaynak Ağırlıkları")
            .competencySourceWeightsText("Değerlendirme yapan kaynaklarınızın...")
            .sourcesTitle("Kaynaklar")
            .sourcesText("Aşağıdaki tablo sizi değerlendiren...")
            .showCompetencyFormWeights(true)
            .showCompetencySourceWeights(true)
            .showSources(true)
            .showOverallResults(true)
            .showSourceBasedCompetencyScores(true)
            .showDetailedCompetencyScores(true)
            .showCommentsSection(true)
            .showOpinionsSection(true)
            .showOverallRawScore(true)
            .showOverallWeightedScore(true)
            .showDetailedGraph(true)
            .showDetailedQuestions(true)
            .showDetailedQuestionDescription(true)
            .showDetailedCompetencyRawScore(true)
            .showDetailedCompetencyWeightedScore(true)
            .showCommentSourceName(true)
            .showSelfColumn(true)
            .showPeerColumn(true)
            .showManagerColumn(true)
            .showSubordinateColumn(true)
            .showOtherColumn(true)
            .showAverageColumn(true)
            .build();
    }
    @Transactional
    public ApiResponse<UserPeriodReportResponse> generateUserPeriodReport(ReportGenerationRequest request) {
        ReportDisplaySettings settings = request.getSettings();
        if (settings == null) {
            settings = createDefaultReportSettings();
        }
        // TODO refactor
        settings = createDefaultReportSettings();

        EvaluationPeriod period = evaluationPeriodRepository.findByIdAndCompanyId(request.getPeriodId(), principalResolver.getCompanyId())
            .orElse(null);
        if (period == null) {
            return ApiResponse.failure(messageHandler.getMessage("evaluation-period.not-found"));
        }

        User evaluatedUser = userRepository.findByIdAndCompany(request.getEvaluatedUserId(), period.getCompany())
            .orElse(null);
        if (evaluatedUser == null) {
            return ApiResponse.failure(messageHandler.getMessage("user.not-found"));
        }

        List<Answer> allAnswers = answerRepository
            .findAllByAssignment_PeriodParticipant_Period_IdAndAssignment_PeriodParticipant_EvaluatedUser_Id(request.getPeriodId(), request.getEvaluatedUserId());

        if (allAnswers.isEmpty()) {
            return ApiResponse.failure(messageHandler.getMessage("report.no-answers-found"));
        }

        Map<Integer, BigDecimal> competencyWeights = periodCompetencyWeightRepository.findAllByPeriod_Id(request.getPeriodId()).stream()
            .collect(Collectors.toMap(pcw -> pcw.getCompetency().getId(), PeriodCompetencyWeight::getWeight));

        Map<Integer, Map<EvaluatorType, BigDecimal>> evaluatorWeights = competencyEvaluatorPermissionRepository.findAllByPeriodId(request.getPeriodId()).stream()
            .collect(Collectors.groupingBy(p -> p.getCompetency().getId(),
                Collectors.toMap(p -> p.getEvaluator().getEvaluatorType(), CompetencyEvaluatorPermission::getWeight)
            ));

        Map<Integer, List<Answer>> answersByCompetency = allAnswers.stream()
            .collect(Collectors.groupingBy(a -> a.getQuestion().getCompetency().getId()));

        UserPeriodReportResponse.UserPeriodReportResponseBuilder reportBuilder = UserPeriodReportResponse.builder()
            .user(userConverter.toUserDetailResponse(evaluatedUser))
            .period(evaluationPeriodConverter.toEvaluationPeriodResponse(period));

        populateCustomTexts(reportBuilder, settings);

        if (settings.isShowOverallResults()) {
            reportBuilder.overallScores(calculateOverallScores(allAnswers, answersByCompetency, competencyWeights, evaluatorWeights, settings));
        }

        if (settings.isShowSourceBasedCompetencyScores() && settings.isShowDetailedGraph()) {
            reportBuilder.scoresByEvaluatorType(calculateScoresByEvaluatorType(allAnswers, settings));
        }

        if (settings.isShowDetailedCompetencyScores()) {
            reportBuilder.competencyScores(calculateCompetencyDetails(answersByCompetency, competencyWeights, evaluatorWeights, settings));
        }

        if (settings.isShowCommentsSection() || settings.isShowOpinionsSection()) {
            reportBuilder.comments(collectComments(allAnswers, settings));
        }

        return ApiResponse.success(reportBuilder.build(), messageHandler.getMessage("report.generate.success"));
    }
    private void populateCustomTexts(UserPeriodReportResponse.UserPeriodReportResponseBuilder builder, ReportDisplaySettings settings) {
        builder.competencyDefinitionTitle(settings.getCompetencyDefinitionTitle())
            .competencyDefinitionText(settings.getCompetencyDefinitionText())
            .competencySourceWeightsTitle(settings.getCompetencySourceWeightsTitle())
            .competencySourceWeightsText(settings.getCompetencySourceWeightsText())
            .sourcesTitle(settings.getSourcesTitle())
            .sourcesText(settings.getSourcesText());
    }
    private OverallScores calculateOverallScores(List<Answer> allAnswers, Map<Integer, List<Answer>> answersByCompetency, Map<Integer, BigDecimal> competencyWeights,
                                                 Map<Integer, Map<EvaluatorType, BigDecimal>> evaluatorWeights, ReportDisplaySettings settings) {
        OverallScores.OverallScoresBuilder builder = OverallScores.builder();

        if (settings.isShowOverallRawScore()) {
            BigDecimal rawAvg = BigDecimal.valueOf(allAnswers.stream().mapToInt(Answer::getScore).average().orElse(0.0))
                .setScale(2, RoundingMode.HALF_UP);
            builder.rawAverageScore(rawAvg);
        }

        if (settings.isShowOverallWeightedScore()) {
            BigDecimal finalWeightedScore = answersByCompetency.entrySet().stream()
                .map(entry -> calculateFinalContributionForCompetency(
                    entry.getValue(),
                    competencyWeights.getOrDefault(entry.getKey(), BigDecimal.ZERO),
                    evaluatorWeights.getOrDefault(entry.getKey(), Collections.emptyMap())
                ))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
            builder.finalWeightedScore(finalWeightedScore);
        }

        return builder.build();
    }
    private List<AverageScoreByEvaluatorTypeResponse> calculateScoresByEvaluatorType(List<Answer> allAnswers, ReportDisplaySettings settings) {
        Map<Evaluator, Double> averageScoresByEvaluator = allAnswers.stream()
            .collect(Collectors.groupingBy(
                a -> a.getAssignment().getEvaluator(),
                Collectors.averagingInt(Answer::getScore)
            ));

        List<AverageScoreByEvaluatorTypeResponse> responseList = new ArrayList<>();

        averageScoresByEvaluator.forEach((evaluator, score) -> {
            EvaluatorType type = evaluator.getEvaluatorType();

            boolean shouldInclude = switch (type) {
                case SELF -> settings.isShowSelfColumn();
                case PEER -> settings.isShowPeerColumn();
                case MANAGER -> settings.isShowManagerColumn();
                case SUBORDINATE -> settings.isShowSubordinateColumn();
                case OTHER -> settings.isShowOtherColumn();
            };

            if (shouldInclude) {
                responseList.add(AverageScoreByEvaluatorTypeResponse.builder()
                    .evaluatorType(type)
                    .evaluatorName(evaluator.getName())
                    .averageScore(BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP))
                    .build());
            }
        });

        return responseList;
    }
    private List<CompetencyScoreDetailResponse> calculateCompetencyDetails(
        Map<Integer, List<Answer>> answersByCompetency,
        Map<Integer, BigDecimal> competencyWeights,
        Map<Integer, Map<EvaluatorType, BigDecimal>> evaluatorWeights,
        ReportDisplaySettings settings
    ) {
        return answersByCompetency.entrySet().stream()
            .map(entry -> {
                Integer competencyId = entry.getKey();
                List<Answer> competencyAnswers = entry.getValue();
                Competency competency = competencyAnswers.getFirst().getQuestion().getCompetency();
                CompetencyScoreDetailResponse.CompetencyScoreDetailResponseBuilder builder = CompetencyScoreDetailResponse.builder()
                    .competencyId(competencyId)
                    .competencyTitle(competency.getTitle());

                if (settings.isShowDetailedCompetencyRawScore()) {
                    builder.rawAverageScore(BigDecimal.valueOf(competencyAnswers.stream().mapToInt(Answer::getScore).average().orElse(0.0))
                        .setScale(2, RoundingMode.HALF_UP));
                }

                if (settings.isShowDetailedCompetencyWeightedScore()) {
                    BigDecimal weightedScore = calculateWeightedScoreForCompetency(competencyAnswers, evaluatorWeights.getOrDefault(competencyId, Collections.emptyMap()));
                    builder.weightedScore(weightedScore);
                }

                if (settings.isShowDetailedQuestions()) {
                    builder.questionScores(calculateQuestionScores(competencyAnswers, settings));
                }

                return builder.build();
            })
            .collect(Collectors.toList());
    }
    private List<QuestionScoreDetailResponse> calculateQuestionScores(List<Answer> competencyAnswers, ReportDisplaySettings settings) {
        Map<Question, List<Answer>> answersByQuestion = competencyAnswers.stream()
            .collect(Collectors.groupingBy(Answer::getQuestion));

        return answersByQuestion.entrySet().stream().map(entry -> {
            Question question = entry.getKey();
            List<Answer> questionAnswers = entry.getValue();

            Map<EvaluatorType, Double> avgScoresByType = questionAnswers.stream()
                .collect(Collectors.groupingBy(a -> a.getAssignment().getEvaluator().getEvaluatorType(),
                    Collectors.averagingInt(Answer::getScore)));

            ScoresByEachEvaluatorType.ScoresByEachEvaluatorTypeBuilder scoresBuilder = ScoresByEachEvaluatorType.builder();

            if (settings.isShowSelfColumn()) {
                scoresBuilder.selfScore(toBigDecimal(avgScoresByType.get(EvaluatorType.SELF)));
            }
            if (settings.isShowPeerColumn()) {
                scoresBuilder.peerScore(toBigDecimal(avgScoresByType.get(EvaluatorType.PEER)));
            }
            if (settings.isShowManagerColumn()) {
                scoresBuilder.managerScore(toBigDecimal(avgScoresByType.get(EvaluatorType.MANAGER)));
            }
            if (settings.isShowSubordinateColumn()) {
                scoresBuilder.subordinateScore(toBigDecimal(avgScoresByType.get(EvaluatorType.SUBORDINATE)));
            }
            if (settings.isShowOtherColumn()) {
                scoresBuilder.otherScore(toBigDecimal(avgScoresByType.get(EvaluatorType.OTHER)));
            }
            if (settings.isShowAverageColumn()) {
                scoresBuilder.averageScore(toBigDecimal(questionAnswers.stream().mapToInt(Answer::getScore).average().orElse(0.0)));
            }

            return QuestionScoreDetailResponse.builder()
                .questionId(question.getId())
                .questionText(settings.isShowDetailedQuestionDescription() ? question.getQuestionText() : null)
                .scores(scoresBuilder.build())
                .build();
        }).collect(Collectors.toList());
    }
    private List<CommentResponse> collectComments(List<Answer> allAnswers, ReportDisplaySettings settings) {
        return allAnswers.stream()
            .filter(a -> a.getComment() != null && !a.getComment().isBlank())
            .map(a -> CommentResponse.builder()
                .evaluatorName(settings.isShowCommentSourceName() ? a.getAssignment().getEvaluator().getName() : null)
                .evaluatorType(a.getAssignment().getEvaluator().getEvaluatorType())
                .comment(a.getComment())
                .build())
            .collect(Collectors.toList());
    }
    private BigDecimal toBigDecimal(Double value) {
        return value == null ? null : BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }
    private BigDecimal calculateWeightedScoreForCompetency(List<Answer> competencyAnswers, Map<EvaluatorType, BigDecimal> currentEvaluatorWeights) {
        return competencyAnswers.stream()
            .collect(Collectors.groupingBy(a -> a.getAssignment().getEvaluator().getEvaluatorType()))
            .entrySet().stream()
            .map(evaluatorEntry -> {
                EvaluatorType type = evaluatorEntry.getKey();
                BigDecimal evaluatorWeight = currentEvaluatorWeights.getOrDefault(type, BigDecimal.ZERO);
                BigDecimal avgScoreFromEvaluator = BigDecimal.valueOf(evaluatorEntry.getValue().stream()
                    .mapToInt(Answer::getScore).average().orElse(0.0));
                return avgScoreFromEvaluator.multiply(evaluatorWeight.divide(new BigDecimal(100), 4, RoundingMode.HALF_UP));
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);
    }
    private BigDecimal calculateFinalContributionForCompetency(
        List<Answer> competencyAnswers,
        BigDecimal competencyWeight,
        Map<EvaluatorType, BigDecimal> currentEvaluatorWeights
    ) {
        BigDecimal weightedScoreWithinCompetency = calculateWeightedScoreForCompetency(competencyAnswers, currentEvaluatorWeights);
        return weightedScoreWithinCompetency.multiply(competencyWeight.divide(new BigDecimal(100), 4, RoundingMode.HALF_UP));
    }
}