package com.batuhan.feedback360.model.response;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserAssignmentsResponse {
    private UserResponse user;
    private List<AssignmentResponse> evaluationsMade;
    private List<AssignmentResponse> evaluationsReceived;
}
