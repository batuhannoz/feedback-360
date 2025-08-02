package com.batuhan.feedback360.service;

import com.batuhan.feedback360.config.AuthenticationPrincipalResolver;
import com.batuhan.feedback360.model.entitiy.Company;
import com.batuhan.feedback360.model.entitiy.User;
import com.batuhan.feedback360.model.request.CompanySignUpRequest;
import com.batuhan.feedback360.model.request.ForgotPasswordRequest;
import com.batuhan.feedback360.model.request.RefreshTokenRequest;
import com.batuhan.feedback360.model.request.ResetPasswordRequest;
import com.batuhan.feedback360.model.request.SignInRequest;
import com.batuhan.feedback360.model.request.UserSignUpRequest;
import com.batuhan.feedback360.model.response.ApiResponse;
import com.batuhan.feedback360.model.response.JwtAuthenticationResponse;
import com.batuhan.feedback360.repository.CompanyRepository;
import com.batuhan.feedback360.repository.UserRepository;
import com.batuhan.feedback360.util.MessageHandler;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final MessageHandler messageHandler;
    private final EmailService emailService;
    private final AuthenticationPrincipalResolver principalResolver;

    public ApiResponse<Company> companySignUp(CompanySignUpRequest request) {
        if (companyRepository.findByEmail(request.getEmail()).isPresent()) {
            return ApiResponse.failure(messageHandler.getMessage("auth.company.signup.email-exists"));
        }
        Company company = Company.builder()
            .name(request.getCompanyName())
            .email(request.getEmail())
            .build();
        Company savedCompany = companyRepository.save(company);

        String token = UUID.randomUUID().toString();
        User adminUser = User.builder()
            .firstName(request.getName())
            .lastName(request.getSurname())
            .email(savedCompany.getEmail())
            .passwordHash(null)
            .company(savedCompany)
            .invitationToken(token)
            .invitationValidityDate(LocalDateTime.now().plusDays(7))
            .isAdmin(true)
            .isActive(true)
            .build();

        userRepository.save(adminUser);
        emailService.sendInvitationEmail(savedCompany.getEmail(), token);
        return ApiResponse.success(savedCompany, messageHandler.getMessage("auth.company.signup.success"));
    }

    public ApiResponse<JwtAuthenticationResponse> signIn(SignInRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user == null || !user.getIsActive() || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            return ApiResponse.failure(messageHandler.getMessage("auth.sign-in.invalid-credentials"));
        }
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        JwtAuthenticationResponse jwtResponse = JwtAuthenticationResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .build();
        return ApiResponse.success(jwtResponse, messageHandler.getMessage("auth.sign-in.success"));
    }

    public ApiResponse<Void> completeEmployeeInvitation(UserSignUpRequest request) {
        User user = userRepository.findByInvitationToken(request.getInvitationToken()).orElse(null);
        if (user == null) {
            return ApiResponse.failure(messageHandler.getMessage("auth.signup.invalid-token"));
        }
        if (user.getInvitationValidityDate().isBefore(LocalDateTime.now())) {
            return ApiResponse.failure(messageHandler.getMessage("auth.signup.expired-token"));
        }

        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setInvitationToken(null);
        user.setInvitationValidityDate(null);
        userRepository.save(user);
        return ApiResponse.success(null, messageHandler.getMessage("auth.employee.signup.success"));
    }

    public ApiResponse<JwtAuthenticationResponse> refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        if (!"REFRESH".equals(jwtService.extractType(refreshToken))) {
            return ApiResponse.failure(messageHandler.getMessage("auth.refresh.invalidType"));
        }
        String userEmail = jwtService.extractUserName(refreshToken);
        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) {
            return ApiResponse.failure(messageHandler.getMessage("auth.refresh.invalid-credentials"));
        }

        if (jwtService.isTokenValid(refreshToken, user)) {
            String newAccessToken = jwtService.generateAccessToken(user);
            String newRefreshToken = jwtService.generateRefreshToken(user);
            JwtAuthenticationResponse jwtResponse = JwtAuthenticationResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
            return ApiResponse.success(jwtResponse, messageHandler.getMessage("auth.refresh.success"));
        }
        return ApiResponse.failure(messageHandler.getMessage("auth.refresh.invalid-token"));
    }

    public ApiResponse<Void> forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            user.setInvitationToken(token);
            user.setInvitationValidityDate(LocalDateTime.now().plusHours(1));
            userRepository.save(user);
            emailService.sendPasswordResetEmail(user, token);
        });
        return ApiResponse.success(null, messageHandler.getMessage("auth.forgot-password.success"));
    }

    public ApiResponse<Void> resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByInvitationToken(request.getToken()).orElse(null);
        if (user == null || user.getInvitationValidityDate().isBefore(LocalDateTime.now())) {
            return ApiResponse.failure(messageHandler.getMessage("auth.au.invalid-or-expiredToken"));
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setInvitationToken(null);
        user.setInvitationValidityDate(null);
        userRepository.save(user);
        return ApiResponse.success(null, messageHandler.getMessage("auth.reset-password.success"));
    }
}
