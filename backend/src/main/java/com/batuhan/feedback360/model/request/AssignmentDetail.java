package com.batuhan.feedback360.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AssignmentDetail {
    @NotNull(message = "Evaluator User ID cannot be null.")
    private Integer evaluatorUserId;

    @NotNull(message = "Evaluator (Type) ID cannot be null.")
    private Integer evaluatorId;
}