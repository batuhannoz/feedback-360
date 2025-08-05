package com.batuhan.feedback360.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

@Data
public class EvaluationScaleRequest {
    @NotBlank
    private String name;

    @NotEmpty
    @Valid
    private List<ScaleOptionRequest> options;
}
