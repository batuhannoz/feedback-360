package com.batuhan.feedback360.service;

import com.batuhan.feedback360.config.AuthenticationPrincipalResolver;
import com.batuhan.feedback360.model.entitiy.EvaluationAssignment;
import com.batuhan.feedback360.model.entitiy.EvaluationPeriod;
import com.batuhan.feedback360.model.enums.PeriodStatus;
import com.batuhan.feedback360.model.response.ApiResponse;
import com.batuhan.feedback360.model.response.DashboardStatsResponse;
import com.batuhan.feedback360.model.response.PeriodStatsResponse;
import com.batuhan.feedback360.repository.EvaluationAssignmentRepository;
import com.batuhan.feedback360.repository.EvaluationPeriodRepository;
import com.batuhan.feedback360.repository.UserRepository;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final EvaluationPeriodRepository evaluationPeriodRepository;
    private final EvaluationAssignmentRepository evaluationAssignmentRepository;
    private final AuthenticationPrincipalResolver principalResolver;

    public ApiResponse<DashboardStatsResponse> getDashboardStats() {
        Integer companyId = principalResolver.getCompanyId();

        long totalUsers = userRepository.countByCompany_Id(companyId);

        List<EvaluationPeriod> companyPeriods = evaluationPeriodRepository.findAllByCompany_Id(companyId);
        List<EvaluationPeriod> activePeriods = companyPeriods.stream()
            .filter(p -> p.getStatus() == PeriodStatus.IN_PROGRESS)
            .toList();

        if (activePeriods.isEmpty()) {
            return buildEmptyStats(totalUsers);
        }

        List<Integer> activePeriodIds = activePeriods.stream().map(EvaluationPeriod::getId).toList();
        List<EvaluationAssignment> allAssignmentsInActivePeriods = evaluationAssignmentRepository.findAllByPeriodParticipant_Period_IdIn(activePeriodIds);

        Map<Integer, List<EvaluationAssignment>> assignmentsByPeriodId = allAssignmentsInActivePeriods.stream()
            .collect(Collectors.groupingBy(a -> a.getPeriodParticipant().getPeriod().getId()));

        List<PeriodStatsResponse> periodStats = activePeriods.stream().map(period -> {
            List<EvaluationAssignment> periodAssignments = assignmentsByPeriodId.getOrDefault(period.getId(), Collections.emptyList());
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

        long totalAssignments = allAssignmentsInActivePeriods.size();
        long completedAssignments = allAssignmentsInActivePeriods.stream()
            .filter(a -> a.getAnswers() != null && !a.getAnswers().isEmpty()).count();

        DashboardStatsResponse stats = DashboardStatsResponse.builder()
            .totalUsers(totalUsers)
            .activePeriods(activePeriods.size())
            .completedAssignments(completedAssignments)
            .totalAssignments(totalAssignments)
            .activePeriodStats(periodStats)
            .build();

        return ApiResponse.success(stats, "dashboard.get.success");
    }

    private ApiResponse<DashboardStatsResponse> buildEmptyStats(long totalUsers) {
        DashboardStatsResponse stats = DashboardStatsResponse.builder()
            .totalUsers(totalUsers)
            .activePeriods(0)
            .completedAssignments(0L)
            .totalAssignments(0L)
            .activePeriodStats(Collections.emptyList())
            .build();
        return ApiResponse.success(stats, "dashboard.get.success");
    }
}