package com.batuhan.feedback360.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompetencyRequest {
    @NotBlank(message = "Title cannot be empty")
    private String title;
    private String description;
}