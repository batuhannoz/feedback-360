package com.batuhan.feedback360.controller;

import com.batuhan.feedback360.model.response.ApiResponse;
import com.batuhan.feedback360.model.response.ParticipantResponse;
import com.batuhan.feedback360.service.PeriodParticipantService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/period/{periodId}/participant")
@RequiredArgsConstructor
public class PeriodParticipantController {

    private final PeriodParticipantService periodParticipantService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ParticipantResponse>>> getParticipantsByPeriod(@PathVariable Integer periodId) {
        return ResponseEntity.ok(periodParticipantService.getParticipantsByPeriod(periodId));
    }

    @PostMapping("/{userId}")
    public ResponseEntity<ApiResponse<ParticipantResponse>> addParticipantToPeriod(
        @PathVariable Integer periodId,
        @PathVariable Integer userId
    ) {
        return ResponseEntity.ok(periodParticipantService.addParticipantToPeriod(periodId, userId));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<?>> deleteParticipantFromPeriod(@PathVariable Integer periodId, @PathVariable Integer userId) {
        return ResponseEntity.ok(periodParticipantService.deleteParticipantFromPeriod(periodId, userId));
    }
}