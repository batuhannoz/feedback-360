package com.batuhan.feedback360.controller;

import com.batuhan.feedback360.model.request.EmployeeRequest;
import com.batuhan.feedback360.model.request.SubmitAnswersRequest;
import com.batuhan.feedback360.model.response.EmployeeDetailResponse;
import com.batuhan.feedback360.model.response.EvaluationDetailResponse;
import com.batuhan.feedback360.model.response.EvaluationPeriodResponse;
import com.batuhan.feedback360.model.response.EvaluationTaskResponse;
import com.batuhan.feedback360.service.EmployeeService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PutMapping
    @PreAuthorize("hasAnyRole('COMPANY', 'ADMIN')")
    public ResponseEntity<?> createEmployee(@RequestBody EmployeeRequest request) {
        return ResponseEntity.ok(employeeService.createEmployee(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('COMPANY', 'ADMIN')")
    public ResponseEntity<EmployeeDetailResponse> updateEmployee(@PathVariable Integer id, @RequestBody EmployeeRequest request) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('COMPANY', 'ADMIN')")
    public ResponseEntity<List<EmployeeDetailResponse>> listEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('COMPANY', 'ADMIN')")
    public ResponseEntity<EmployeeDetailResponse> getEmployeeDetails(@PathVariable Integer id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @PostMapping("/{employeeId}/role/{roleId}")
    @PreAuthorize("hasAnyRole('COMPANY', 'ADMIN')")
    public ResponseEntity<EmployeeDetailResponse> assignRoleToEmployee(@PathVariable Integer employeeId, @PathVariable Integer roleId) {
        return ResponseEntity.ok(employeeService.assignRoleToEmployee(employeeId, roleId));
    }

    @DeleteMapping("/{employeeId}/role/{roleId}")
    @PreAuthorize("hasAnyRole('COMPANY', 'ADMIN')")
    public ResponseEntity<EmployeeDetailResponse> removeRoleFromEmployee(@PathVariable Integer employeeId, @PathVariable Integer roleId) {
        return ResponseEntity.ok(employeeService.removeRoleFromEmployee(employeeId, roleId));
    }

    @GetMapping("/period")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public ResponseEntity<List<EvaluationPeriodResponse>> getEmployeePeriods() {
        return ResponseEntity.ok(employeeService.getEmployeePeriods());
    }

    @GetMapping("/period/{periodId}/tasks")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public ResponseEntity<List<EvaluationTaskResponse>> getEvaluationTasksForPeriod(@PathVariable Integer periodId) {
        return ResponseEntity.ok(employeeService.getEvaluationTasksForPeriod(periodId));
    }

    @GetMapping("/evaluation/{evaluationId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public ResponseEntity<EvaluationDetailResponse> getEvaluationDetails(@PathVariable Integer evaluationId) {
        EvaluationDetailResponse response = employeeService.startOrGetEvaluation(evaluationId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/evaluation/{evaluationId}/answers")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public ResponseEntity<EvaluationDetailResponse> submitAnswers(@PathVariable Integer evaluationId, @RequestBody SubmitAnswersRequest request) {
        EvaluationDetailResponse response = employeeService.submitAnswers(evaluationId, request);
        return ResponseEntity.ok(response);
    }
}
