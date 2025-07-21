package com.batuhan.feedback360.controller;

import com.batuhan.feedback360.model.request.AnswerSubmissionRequest;
import com.batuhan.feedback360.model.request.UserRequest;
import com.batuhan.feedback360.model.response.AnswerResponse;
import com.batuhan.feedback360.model.response.ApiResponse;
import com.batuhan.feedback360.model.response.EvaluationPeriodResponse;
import com.batuhan.feedback360.model.response.QuestionResponse;
import com.batuhan.feedback360.model.response.UserAssignmentsResponse;
import com.batuhan.feedback360.model.response.UserDetailResponse;
import com.batuhan.feedback360.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<UserDetailResponse>> createEmployee(@RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.createUser(request));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserDetailResponse>> updateUser(@PathVariable Integer userId, @RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.updateEmployee(userId, request));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDetailResponse>>> getUsers(@RequestParam(required = false) Boolean active, @RequestParam(required = false) String name) {
        return ResponseEntity.ok(userService.getUsers(active, name));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserDetailResponse>> getUserDetails(@PathVariable Integer userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @GetMapping("/period/{periodId}/user/{userId}/assignment")
    public ResponseEntity<ApiResponse<UserAssignmentsResponse>> getUserAssignments(
        @PathVariable Integer periodId,
        @PathVariable Integer userId
    ) {
        return ResponseEntity.ok(userService.getUserAssignments(periodId, userId));
    }

    @GetMapping("/assignment/{assignmentId}/answer")
    public ResponseEntity<ApiResponse<List<AnswerResponse>>> getAnswersForAssignment(
        @PathVariable Integer assignmentId
    ) {
        return ResponseEntity.ok(userService.getAnswersForAssignment(assignmentId));
    }

    @GetMapping("/period")
    public ResponseEntity<ApiResponse<List<EvaluationPeriodResponse>>> getEvaluationPeriodsForUser() {
        return ResponseEntity.ok(userService.getEvaluationPeriodsForUser());
    }

    @GetMapping("/period/{periodId}/evaluation")
    public ResponseEntity<ApiResponse<List<UserDetailResponse>>> getEvaluationsForUserPeriod(
        @PathVariable Integer periodId
    ) {
        return ResponseEntity.ok(userService.getEvaluationsForUserPeriod(periodId));
    }

    @GetMapping("/period/{periodId}/evaluation/{evaluatedUserId}")
    public ResponseEntity<ApiResponse<List<QuestionResponse>>> getQuestionsForEvaluatedUser(
        @PathVariable Integer periodId,
        @PathVariable Integer evaluatedUserId
    ) {
        return ResponseEntity.ok(userService.getQuestionsForEvaluatedUser(periodId, evaluatedUserId));
    }

    @PostMapping("/period/{periodId}/evaluation/{evaluatedUserId}")
    public ResponseEntity<ApiResponse<List<AnswerResponse>>> submitAnswersForEvaluatedUser(
        @PathVariable Integer periodId,
        @PathVariable Integer evaluatedUserId,
        @Valid @RequestBody List<AnswerSubmissionRequest> request
    ) {
        return ResponseEntity.ok(userService.submitAnswersForEvaluatedUser(periodId, evaluatedUserId, request));
    }
}
