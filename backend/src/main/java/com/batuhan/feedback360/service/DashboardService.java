package com.batuhan.feedback360.service;

import com.batuhan.feedback360.model.entitiy.EvaluationAssignment;
import com.batuhan.feedback360.model.entitiy.EvaluationPeriod;
import com.batuhan.feedback360.model.enums.PeriodStatus;
import com.batuhan.feedback360.model.response.ApiResponse;
import com.batuhan.feedback360.model.response.DashboardStatsResponse;
import com.batuhan.feedback360.model.response.PeriodStatsResponse;
import com.batuhan.feedback360.repository.EvaluationAssignmentRepository;
import com.batuhan.feedback360.repository.EvaluationPeriodRepository;
import com.batuhan.feedback360.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final EvaluationPeriodRepository evaluationPeriodRepository;
    private final EvaluationAssignmentRepository evaluationAssignmentRepository;

    public ApiResponse<DashboardStatsResponse> getDashboardStats() {
        long totalUsers = userRepository.count();
        List<EvaluationPeriod> activePeriods = evaluationPeriodRepository.findAll().stream()
            .filter(p -> p.getStatus() == PeriodStatus.IN_PROGRESS)
            .toList();

        List<EvaluationAssignment> allAssignments = evaluationAssignmentRepository.findAll();

        List<PeriodStatsResponse> periodStats = activePeriods.stream().map(period -> {
            List<EvaluationAssignment> periodAssignments = allAssignments.stream()
                .filter(a -> a.getPeriodParticipant().getPeriod().getId().equals(period.getId()))
                .toList();
            long completed = periodAssignments.stream().filter(a -> a.getAnswers() != null && !a.getAnswers().isEmpty()).count();
            return PeriodStatsResponse.builder()
                .id(period.getId().longValue())
                .name(period.getPeriodName())
                .startDate(period.getStartDate().toLocalDate())
                .endDate(period.getEndDate().toLocalDate())
                .totalAssignments(periodAssignments.size())
                .completedAssignments(completed)
                .build();
        }).collect(Collectors.toList());

        long totalAssignments = allAssignments.size();
        long completedAssignments = allAssignments.stream()
            .filter(a -> a.getAnswers() != null && !a.getAnswers().isEmpty()).count();

        DashboardStatsResponse stats = DashboardStatsResponse.builder()
            .totalUsers(totalUsers)
            .activePeriods(activePeriods.size())
            .completedAssignments(completedAssignments)
            .totalAssignments(totalAssignments)
            .activePeriodStats(periodStats)
            .build();

        return ApiResponse.success(stats, "Dashboard stats fetched successfully");
    }
}
