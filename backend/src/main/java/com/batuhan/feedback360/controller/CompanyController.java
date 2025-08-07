package com.batuhan.feedback360.controller;


import com.batuhan.feedback360.model.response.ApiResponse;
import com.batuhan.feedback360.model.response.CompanyInfoUpdateRequest;
import com.batuhan.feedback360.model.response.CompanySettingsResponse;
import com.batuhan.feedback360.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/company/settings")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CompanySettingsResponse>> getCompanySettings() {
        return ResponseEntity.ok(companyService.getCompanySettings());
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> updateCompanyInfo(@Valid @RequestBody CompanyInfoUpdateRequest request) {
        return ResponseEntity.ok(companyService.updateCompanyInfo(request));
    }

    @PostMapping("/logo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> updateCompanyLogo(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(companyService.updateCompanyLogo(file));
    }

    @GetMapping("/logo")
    public ResponseEntity<byte[]> getCompanyLogo() {
        return companyService.getCompanyLogo()
            .map(logoData -> ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(logoData.mimeType()))
                .body(logoData.data()))
            .orElse(ResponseEntity.notFound().build());
    }
}
