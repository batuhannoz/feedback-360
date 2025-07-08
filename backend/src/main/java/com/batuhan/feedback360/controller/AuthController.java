package com.batuhan.feedback360.controller;

import com.batuhan.feedback360.model.request.EmployeeSignUpRequest;
import com.batuhan.feedback360.model.request.RefreshTokenRequest;
import com.batuhan.feedback360.model.request.SignInRequest;
import com.batuhan.feedback360.model.request.SignUpRequest;
import com.batuhan.feedback360.model.response.JwtAuthenticationResponse;
import com.batuhan.feedback360.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/company/sign-up")
    public ResponseEntity<?> companySignUp(@RequestBody SignUpRequest request) {
        return ResponseEntity.ok(authService.companySignUp(request));
    }

    @PostMapping("/sign-in")
    public ResponseEntity<JwtAuthenticationResponse> companySignIn(@RequestBody SignInRequest request) {
        return ResponseEntity.ok(authService.signIn(request));
    }

    @PostMapping("/employee/invitation")
    public ResponseEntity<?> completeEmployeeInvitation(@RequestBody EmployeeSignUpRequest request) {
        return ResponseEntity.ok(authService.completeEmployeeInvitation(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtAuthenticationResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }
}