package com.batuhan.feedback360.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ParticipantResponse {
    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
    private Boolean isActive;
}