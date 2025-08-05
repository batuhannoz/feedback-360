package com.batuhan.feedback360.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ScaleOptionRequest {
    @NotNull
    private Integer score;
    @NotBlank
    private String label;
}
