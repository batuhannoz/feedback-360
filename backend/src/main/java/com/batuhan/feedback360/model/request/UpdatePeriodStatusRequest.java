package com.batuhan.feedback360.model.request;

import com.batuhan.feedback360.model.enums.PeriodStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdatePeriodStatusRequest {

    @NotNull(message = "Status cannot be null")
    private PeriodStatus status;
}
