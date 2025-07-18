package com.batuhan.feedback360.model.request;

import com.batuhan.feedback360.model.enums.EvaluatorType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EvaluatorRequest {
    private EvaluatorType evaluatorType;
    private String name;
}