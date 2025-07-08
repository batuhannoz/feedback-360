package com.batuhan.feedback360.service;

import com.batuhan.feedback360.model.entitiy.Company;
import com.batuhan.feedback360.model.entitiy.Employee;
import com.batuhan.feedback360.model.request.EmployeeSignUpRequest;
import com.batuhan.feedback360.model.response.ApiResponse;
import com.batuhan.feedback360.model.response.JwtAuthenticationResponse;
import com.batuhan.feedback360.model.request.RefreshTokenRequest;
import com.batuhan.feedback360.model.request.SignInRequest;
import com.batuhan.feedback360.model.request.SignUpRequest;
import com.batuhan.feedback360.repository.CompanyRepository;
import com.batuhan.feedback360.repository.EmployeeRepository;
import com.batuhan.feedback360.util.MessageHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final MessageHandler messageHandler;

    public ApiResponse<Company> companySignUp(SignUpRequest request) {
        Company company = Company.builder()
            .name(request.getCompanyName())
            .email(request.getEmail())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .build();
        Company savedCompany = companyRepository.save(company);

        return ApiResponse.success(savedCompany, messageHandler.getMessage("auth.company.signup.success"));
    }

    public ApiResponse<JwtAuthenticationResponse> signIn(SignInRequest request) {
        final var authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        UserDetails user = (UserDetails) authentication.getPrincipal();
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        JwtAuthenticationResponse jwtResponse = JwtAuthenticationResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .build();

        return ApiResponse.success(jwtResponse, messageHandler.getMessage("auth.signin.success"));
    }

    public ApiResponse<Void> completeEmployeeInvitation(EmployeeSignUpRequest request) {
        Employee employee = employeeRepository.findByInvitationToken(request.getInvitationToken())
            .orElse(null);

        if (employee == null) {
            return ApiResponse.failure(messageHandler.getMessage("auth.employee.signup.invalidToken"));
        }

        if (employee.getInvitationValidityDate().isBefore(LocalDateTime.now())) {
            return ApiResponse.failure(messageHandler.getMessage("auth.employee.signup.expiredToken"));
        }

        employee.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        employee.setInvitationToken(null);
        employee.setInvitationValidityDate(null);
        employeeRepository.save(employee);

        return ApiResponse.success(null, messageHandler.getMessage("auth.employee.signup.success"));
    }

    public ApiResponse<JwtAuthenticationResponse> refreshToken(RefreshTokenRequest request) {
        final String refreshToken = request.getRefreshToken();

        if (!"REFRESH".equals(jwtService.extractType(refreshToken))) {
            return ApiResponse.failure(messageHandler.getMessage("auth.refresh.invalidType"));
        }

        final String userEmail = jwtService.extractUserName(refreshToken);

        UserDetails userDetails = companyRepository.findByEmail(userEmail)
            .map(u -> (UserDetails) u)
            .orElseGet(() -> employeeRepository.findByEmail(userEmail)
                .orElse(null));

        if (userDetails == null) {
            return ApiResponse.failure(messageHandler.getMessage("auth.refresh.userNotFound"));
        }

        if (jwtService.isTokenValid(refreshToken, userDetails)) {
            var newAccessToken = jwtService.generateAccessToken(userDetails);
            var newRefreshToken = jwtService.generateRefreshToken(userDetails);
            JwtAuthenticationResponse jwtResponse = JwtAuthenticationResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
            return ApiResponse.success(jwtResponse, messageHandler.getMessage("auth.refresh.success"));
        }

        return ApiResponse.failure(messageHandler.getMessage("auth.refresh.invalidToken"));
    }
}