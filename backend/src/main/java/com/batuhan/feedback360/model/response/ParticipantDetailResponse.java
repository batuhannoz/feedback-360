package com.batuhan.feedback360.model.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ParticipantDetailResponse {
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private int totalAssignments;
    private int completedAssignments;
}
