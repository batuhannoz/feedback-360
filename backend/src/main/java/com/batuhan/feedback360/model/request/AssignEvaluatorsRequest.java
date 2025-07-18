package com.batuhan.feedback360.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssignEvaluatorsRequest {
    @Valid
    @NotNull
    private List<AssignmentDetail> assignments;
}