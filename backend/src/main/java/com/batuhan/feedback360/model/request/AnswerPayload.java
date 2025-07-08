package com.batuhan.feedback360.model.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class AnswerPayload {
    private Integer answerId;
    private String value;
}
