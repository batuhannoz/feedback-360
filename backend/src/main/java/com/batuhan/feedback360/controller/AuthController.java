package com.batuhan.feedback360.controller;

import com.batuhan.feedback360.model.request.CompanySignUpRequest;
import com.batuhan.feedback360.model.request.ForgotPasswordRequest;
import com.batuhan.feedback360.model.request.RefreshTokenRequest;
import com.batuhan.feedback360.model.request.ResetPasswordRequest;
import com.batuhan.feedback360.model.request.SignInRequest;
import com.batuhan.feedback360.model.request.UserSignUpRequest;
import com.batuhan.feedback360.model.response.ApiResponse;
import com.batuhan.feedback360.model.response.JwtAuthenticationResponse;
import com.batuhan.feedback360.service.AuthService;
import jakarta.validation.Valid;
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
    public ResponseEntity<?> companySignUp(@RequestBody CompanySignUpRequest request) {
        return ResponseEntity.ok(authService.companySignUp(request));
    }

    @PostMapping("/invitation")
    public ResponseEntity<ApiResponse<?>> completeInvitation(@Valid @RequestBody UserSignUpRequest request) {
        return ResponseEntity.ok(authService.completeEmployeeInvitation(request));
    }

    @PostMapping("/sign-in")
    public ResponseEntity<ApiResponse<JwtAuthenticationResponse>> signIn(@RequestBody SignInRequest request) {
        return ResponseEntity.ok(authService.signIn(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<JwtAuthenticationResponse>> refreshToken(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.forgotPassword(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }
}
