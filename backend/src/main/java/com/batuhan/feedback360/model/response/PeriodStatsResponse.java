package com.batuhan.feedback360.model.response;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PeriodStatsResponse {
    private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private long completedAssignments;
    private long totalAssignments;
}
