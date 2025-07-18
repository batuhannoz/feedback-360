package com.batuhan.feedback360.controller;

import com.batuhan.feedback360.model.entitiy.User;
import com.batuhan.feedback360.model.request.UserRequest;
import com.batuhan.feedback360.model.response.ApiResponse;
import com.batuhan.feedback360.model.response.UserDetailResponse;
import com.batuhan.feedback360.service.UserService;
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
    public ResponseEntity<ApiResponse<User>> createEmployee(@RequestBody UserRequest request) {
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
}
