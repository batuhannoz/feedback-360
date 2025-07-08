package com.batuhan.feedback360.service;

import com.batuhan.feedback360.model.entitiy.Company;
import com.batuhan.feedback360.model.entitiy.Employee;
import com.batuhan.feedback360.model.request.EmployeeSignUpRequest;
import com.batuhan.feedback360.model.response.JwtAuthenticationResponse;
import com.batuhan.feedback360.model.request.RefreshTokenRequest;
import com.batuhan.feedback360.model.request.SignInRequest;
import com.batuhan.feedback360.model.request.SignUpRequest;
import com.batuhan.feedback360.repository.CompanyRepository;
import com.batuhan.feedback360.repository.EmployeeRepository;
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

    public Company companySignUp(SignUpRequest request) {
        Company company = Company.builder()
            .name(request.getCompanyName())
            .email(request.getEmail())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .build();
        return companyRepository.save(company);
    }

    public JwtAuthenticationResponse signIn(SignInRequest request) {
        final var authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        UserDetails user = (UserDetails) authentication.getPrincipal();
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return JwtAuthenticationResponse.builder().accessToken(accessToken).refreshToken(refreshToken).build();
    }

    public String completeEmployeeInvitation(EmployeeSignUpRequest request) {
        Employee employee = employeeRepository.findByInvitationToken(request.getInvitationToken())
            .orElseThrow(() -> new IllegalArgumentException("Invalid invitation token."));

        System.out.println(employee);

        if (employee.getInvitationValidityDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Invitation token has expired.");
        }

        employee.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        employee.setInvitationToken(null);
        employee.setInvitationValidityDate(null);
        employeeRepository.save(employee);
        return "Employee registration completed successfully.";
    }

    public JwtAuthenticationResponse refreshToken(RefreshTokenRequest request) {
        final String refreshToken = request.getRefreshToken();

        if (!"REFRESH".equals(jwtService.extractType(refreshToken))) {
            throw new IllegalArgumentException("Invalid token type provided for refresh.");
        }

        final String userEmail = jwtService.extractUserName(refreshToken);

        UserDetails userDetails = companyRepository.findByEmail(userEmail)
            .map(u -> (UserDetails) u)
            .orElseGet(() -> employeeRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found for refresh token.")));

        if (jwtService.isTokenValid(refreshToken, userDetails)) {
            var newAccessToken = jwtService.generateAccessToken(userDetails);
            var newRefreshToken = jwtService.generateRefreshToken(userDetails);
            return JwtAuthenticationResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
        }

        throw new IllegalArgumentException("Invalid refresh token.");
    }
}