package com.batuhan.feedback360.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class QuestionRequest {
    @NotBlank(message = "Question text cannot be blank")
    private String questionText;
}
