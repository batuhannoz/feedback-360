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
import com.batuhan.feedback360.model.request.ShareReportRequest;
import com.batuhan.feedback360.model.response.ApiResponse;
import com.batuhan.feedback360.model.response.CompetencyScoreDetailResponse;
import com.batuhan.feedback360.model.response.QuestionScoreDetailResponse;
import com.batuhan.feedback360.model.response.ScoreByEvaluatorResponse;
import com.batuhan.feedback360.model.response.ScoreByEvaluatorTypeResponse;
import com.batuhan.feedback360.model.response.UserPeriodReportResponse;
import com.batuhan.feedback360.repository.AnswerRepository;
import com.batuhan.feedback360.repository.CompetencyEvaluatorPermissionRepository;
import com.batuhan.feedback360.repository.EvaluationPeriodRepository;
import com.batuhan.feedback360.repository.PeriodCompetencyWeightRepository;
import com.batuhan.feedback360.repository.PeriodParticipantRepository;
import com.batuhan.feedback360.repository.UserRepository;
import com.batuhan.feedback360.util.MessageHandler;
import jakarta.mail.MessagingException;
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
    private final CompetencyEvaluatorPermissionRepository competencyEvaluatorPermissionRepository;
    private final UserRepository userRepository;
    private final AnswerRepository answerRepository;
    private final UserConverter userConverter;
    private final EvaluationPeriodConverter evaluationPeriodConverter;
    private final AuthenticationPrincipalResolver principalResolver;
    private final MessageHandler messageHandler;
    private final PeriodParticipantRepository periodParticipantRepository;
    private final EmailService emailService;

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

    public ApiResponse<?> shareUserPeriodReport(Integer periodId, Integer evaluatedUserId, ShareReportRequest shareRequest) {
        ApiResponse<UserPeriodReportResponse> reportApiResponse = generateUserPeriodReport(periodId, evaluatedUserId);
        if (!reportApiResponse.isSuccess()) {
            return reportApiResponse;
        }
        UserPeriodReportResponse reportData = reportApiResponse.getData();
        String userEmail = reportData.getUser().getEmail();
        String periodName = reportData.getPeriod().getEvaluationName();
        try {
            String emailBody = buildReportEmailBody(reportData, shareRequest);
            emailService.sendReportShareEmail(userEmail, periodName, emailBody);
        } catch (MessagingException e) {
            System.err.println("Failed to send report email: " + e.getMessage());
            return ApiResponse.failure("Failed to send email. Please try again later.");
        }
        return ApiResponse.success(messageHandler.getMessage("report.share.success"));
    }

    private BigDecimal calculateRawAverageScore(List<Answer> allAnswers) {
        return BigDecimal.valueOf(allAnswers.stream()
                .mapToInt(Answer::getScore)
                .average()
                .orElse(0.0))
            .setScale(2, RoundingMode.HALF_UP);
    }

    private List<QuestionScoreDetailResponse> calculateQuestionScores(List<Answer> competencyAnswers) {
        Map<Question, List<Answer>> answersByQuestion = competencyAnswers.stream()
            .collect(Collectors.groupingBy(Answer::getQuestion));

        return answersByQuestion.entrySet().stream()
            .map(entry -> {
                Question question = entry.getKey();
                List<Answer> questionAnswers = entry.getValue();

                List<ScoreByEvaluatorTypeResponse> scoresByEvaluator = questionAnswers.stream()
                    .map(answer -> ScoreByEvaluatorTypeResponse.builder()
                        .evaluatorType(answer.getAssignment().getEvaluator().getEvaluatorType())
                        .score(answer.getScore())
                        .build())
                    .collect(Collectors.toList());

                return QuestionScoreDetailResponse.builder()
                    .questionId(question.getId())
                    .questionText(question.getQuestionText())
                    .scoresByEvaluatorType(scoresByEvaluator)
                    .build();
            })
            .collect(Collectors.toList());
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
        Map<Integer, Map<EvaluatorType, BigDecimal>> evaluatorWeights
    ) {
        return answersByCompetency.entrySet().stream()
            .map(entry -> {
                Integer competencyId = entry.getKey();
                List<Answer> competencyAnswers = entry.getValue();
                Competency competency = competencyAnswers.getFirst().getQuestion().getCompetency();

                List<String> comments = competencyAnswers.stream()
                    .map(Answer::getComment)
                    .filter(comment -> comment != null && !comment.isBlank())
                    .collect(Collectors.toList());
                BigDecimal rawAverageScore = calculateRawAverageScore(competencyAnswers);

                BigDecimal finalContribution = calculateFinalContributionForCompetency(
                    competencyAnswers,
                    competencyWeights.getOrDefault(competencyId, BigDecimal.ZERO),
                    evaluatorWeights.getOrDefault(competencyId, Collections.emptyMap())
                );

                List<QuestionScoreDetailResponse> questionScores = calculateQuestionScores(competencyAnswers);

                return CompetencyScoreDetailResponse.builder()
                    .competencyId(competencyId)
                    .competencyTitle(competency.getTitle())
                    .rawAverageScore(rawAverageScore)
                    .finalWeightedScore(finalContribution)
                    .comments(comments)
                    .questionScores(questionScores)
                    .build();
            })
            .collect(Collectors.toList());
    }

    private BigDecimal calculateFinalContributionForCompetency(
        List<Answer> competencyAnswers,
        BigDecimal competencyWeight,
        Map<EvaluatorType, BigDecimal> currentEvaluatorWeights
    ) {
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

    // TODO temporary / refactor
    private String buildReportEmailBody(UserPeriodReportResponse report, ShareReportRequest shareRequest) {
        StringBuilder sb = new StringBuilder();
        String userName = report.getUser().getFirstName();
        String periodName = report.getPeriod().getEvaluationName();

        sb.append("<html><body>");
        sb.append("<p>").append(messageHandler.getMessage("email.report.share.body.header", userName, periodName)).append("</p>");

        if (shareRequest.getCustomMessage() != null && !shareRequest.getCustomMessage().isBlank()) {
            sb.append("<p><b>A message from your administrator:</b></p>");
            sb.append("<p style='background-color:#f5f5f5; border-left: 4px solid #ccc; padding: 10px;'><i>")
                .append(shareRequest.getCustomMessage()).append("</i></p>");
        }
        sb.append("<hr>");

        if (shareRequest.isIncludeRawAverageScore() || shareRequest.isIncludeCompetencyWeightedScore() || shareRequest.isIncludeFinalWeightedScore()) {
            sb.append("<h2>Overall Results</h2><ul>");
            if (shareRequest.isIncludeRawAverageScore()) {
                sb.append("<li><b>").append(messageHandler.getMessage("email.report.share.section.raw-average")).append(":</b> ").append(report.getRawAverageScore()).append("</li>");
            }
            if (shareRequest.isIncludeCompetencyWeightedScore()) {
                sb.append("<li><b>").append(messageHandler.getMessage("email.report.share.section.competency-weighted")).append(":</b> ").append(report.getCompetencyWeightedScore())
                    .append("</li>");
            }
            if (shareRequest.isIncludeFinalWeightedScore()) {
                sb.append("<li><b>").append(messageHandler.getMessage("email.report.share.section.final-weighted")).append(":</b> ").append(report.getFinalWeightedScore()).append("</li>");
            }
            sb.append("</ul>");
        }

        if (shareRequest.isIncludeScoresByEvaluator() && report.getScoresByEvaluator() != null && !report.getScoresByEvaluator().isEmpty()) {
            sb.append("<hr><h2>").append(messageHandler.getMessage("email.report.share.section.evaluator-scores")).append("</h2>");
            sb.append("<table border='1' cellpadding='5' style='border-collapse: collapse; width: 50%;'><tr><th align='left'>Evaluator Type</th><th align='left'>Average Score</th></tr>");
            report.getScoresByEvaluator().forEach(score -> sb.append("<tr><td>").append(score.getEvaluatorName()).append("</td><td>").append(score.getAverageScore()).append("</td></tr>"));
            sb.append("</table>");
        }

        if (shareRequest.isIncludeCompetencyScores() && report.getCompetencyScores() != null && !report.getCompetencyScores().isEmpty()) {
            sb.append("<hr><h2>").append(messageHandler.getMessage("email.report.share.section.competency-scores")).append("</h2>");
            for (CompetencyScoreDetailResponse compScore : report.getCompetencyScores()) {
                sb.append("<h3>").append(compScore.getCompetencyTitle()).append("</h3><ul>");
                sb.append("<li><b>Raw Average Score:</b> ").append(compScore.getRawAverageScore()).append("</li>");
                sb.append("<li><b>Final Weighted Score Contribution:</b> ").append(compScore.getFinalWeightedScore()).append("</li></ul>");

                if (compScore.getQuestionScores() != null && !compScore.getQuestionScores().isEmpty()) {
                    sb.append("<h4>Question Details</h4>");
                    sb.append("<table border='1' cellpadding='5' style='border-collapse: collapse; width: 100%;'>")
                        .append("<tr style='background-color:#f2f2f2;'><th align='left'>Question</th><th align='left'>Evaluator</th><th align='left'>Score</th></tr>");

                    for (QuestionScoreDetailResponse questionScore : compScore.getQuestionScores()) {
                        int scoreCount = questionScore.getScoresByEvaluatorType().size();
                        boolean firstRow = true;
                        for (ScoreByEvaluatorTypeResponse scoreDetail : questionScore.getScoresByEvaluatorType()) {
                            sb.append("<tr>");
                            if (firstRow) {
                                sb.append("<td rowspan='").append(scoreCount).append("'>").append(questionScore.getQuestionText()).append("</td>");
                            }
                            sb.append("<td>").append(scoreDetail.getEvaluatorType().name()).append("</td>");
                            sb.append("<td>").append(scoreDetail.getScore()).append("</td>");
                            sb.append("</tr>");
                            firstRow = false;
                        }
                    }
                    sb.append("</table>");
                }

                if (shareRequest.isIncludeComments() && compScore.getComments() != null && !compScore.getComments().isEmpty()) {
                    sb.append("<h4>").append(messageHandler.getMessage("email.report.share.section.comments")).append("</h4><ul>");
                    compScore.getComments().forEach(comment -> sb.append("<li><i>\"").append(comment).append("\"</i></li>"));
                    sb.append("</ul>");
                }
            }
        }

        sb.append("<hr><p>").append(messageHandler.getMessage("email.report.share.body.footer")).append("</p>");
        sb.append("</body></html>");
        return sb.toString();
    }
}
