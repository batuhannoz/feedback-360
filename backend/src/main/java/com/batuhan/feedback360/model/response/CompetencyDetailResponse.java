package com.batuhan.feedback360.model.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@Data
public class CompetencyDetailResponse {
    private Integer id;
    private String title;
    private String description;
    private LocalDateTime createdAt;
    private List<QuestionResponse> questions;
}
