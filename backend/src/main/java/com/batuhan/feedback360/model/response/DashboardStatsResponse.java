package com.batuhan.feedback360.model.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private long totalUsers;
    private long activePeriods;
    private long completedAssignments;
    private long totalAssignments;
    private List<PeriodStatsResponse> activePeriodStats;
}
