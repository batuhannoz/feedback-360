package com.batuhan.feedback360.model.response;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class EvaluationPeriodDetailResponse {
    private Long id;
    private String periodName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private List<ParticipantDetailResponse> participants;
}
