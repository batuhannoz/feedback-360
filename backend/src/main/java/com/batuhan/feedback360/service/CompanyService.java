package com.batuhan.feedback360.service;

import com.batuhan.feedback360.config.AuthenticationPrincipalResolver;
import com.batuhan.feedback360.model.converter.CompanySettingsConverter;
import com.batuhan.feedback360.model.entitiy.Company;
import com.batuhan.feedback360.model.response.ApiResponse;
import com.batuhan.feedback360.model.response.CompanyInfoUpdateRequest;
import com.batuhan.feedback360.model.response.CompanySettingsResponse;
import com.batuhan.feedback360.repository.CompanyRepository;
import com.batuhan.feedback360.util.MessageHandler;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@AllArgsConstructor
public class CompanyService {

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList("image/jpeg", "image/png", "image/gif");
    private final CompanyRepository companyRepository;
    private final AuthenticationPrincipalResolver principalResolver;
    private final CompanySettingsConverter companySettingsConverter;
    private final MessageHandler messageHandler;
    public ApiResponse<CompanySettingsResponse> getCompanySettings() {
        Company company = companyRepository.getReferenceById(principalResolver.getCompanyId());
        return ApiResponse.success(
            companySettingsConverter.toCompanySettingsResponse(company),
            messageHandler.getMessage("company-settings.get.success")
        );
    }
    @Transactional
    public ApiResponse<?> updateCompanyInfo(CompanyInfoUpdateRequest request) {
        Company company = companyRepository.getReferenceById(principalResolver.getCompanyId());

        company.setName(request.getName());
        company.setEmail(request.getEmail());
        company.setPhoneNumber(request.getPhoneNumber());
        company.setAddress(request.getAddress());
        company.setWebsite(request.getWebsite());
        company.setEmailFooter(request.getEmailFooter());

        companyRepository.save(company);

        return ApiResponse.success(messageHandler.getMessage("company-settings.info.update.success"));
    }
    @Transactional
    public ApiResponse<?> updateCompanyLogo(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ApiResponse.failure(messageHandler.getMessage("company-settings.logo.file.empty"));
        }

        if (!ALLOWED_IMAGE_TYPES.contains(file.getContentType())) {
            return ApiResponse.failure(messageHandler.getMessage("company-settings.logo.file.invalid-type"));
        }

        Company company = companyRepository.getReferenceById(principalResolver.getCompanyId());

        try {
            company.setLogo(file.getBytes());
            company.setLogoMimeType(file.getContentType());
            companyRepository.save(company);
        } catch (IOException e) {
            throw new RuntimeException("Could not read file.", e);
        }

        return ApiResponse.success(messageHandler.getMessage("company-settings.logo.update.success"));
    }
    public Optional<LogoData> getCompanyLogo() {
        Company company = companyRepository.getReferenceById(principalResolver.getCompanyId());
        if (company.getLogo() != null && company.getLogoMimeType() != null) {
            return Optional.of(new LogoData(company.getLogo(), company.getLogoMimeType()));
        }
        return Optional.empty();
    }

    public record LogoData(byte[] data, String mimeType) {
    }
}
