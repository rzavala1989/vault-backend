package com.vault.controller;

import com.vault.dto.*;
import com.vault.entity.UserDocument;
import com.vault.entity.UserEmployment;
import com.vault.entity.UserTaxForm;
import com.vault.repository.CountryRepository;
import com.vault.security.UserPrincipal;
import com.vault.service.FileStorageService;
import com.vault.service.OnboardingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/u")
@RequiredArgsConstructor
public class OnboardingController {

    private final OnboardingService onboardingService;
    private final FileStorageService fileStorageService;
    private final CountryRepository countryRepository;

    @PostMapping("/save-personal-info")
    public ResponseEntity<Map<String, Object>> savePersonalInfo(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody PersonalInfoRequest request) {
        return ResponseEntity.ok(onboardingService.savePersonalInfo(principal.getId(), request));
    }

    @PostMapping("/save-financial-bands")
    public ResponseEntity<Map<String, Object>> saveFinancialBands(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody FinancialBandsRequest request) {
        return ResponseEntity.ok(onboardingService.saveFinancialBands(principal.getId(), request));
    }

    @PostMapping("/save-citizenship-and-tax")
    public ResponseEntity<Map<String, Object>> saveCitizenshipAndTax(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody CitizenshipTaxRequest request) {
        return ResponseEntity.ok(onboardingService.saveCitizenshipAndTax(principal.getId(), request));
    }

    @PostMapping("/save-employment")
    public ResponseEntity<Map<String, Object>> saveEmployment(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody EmploymentRequest request) {
        return ResponseEntity.ok(onboardingService.saveEmployment(principal.getId(), request));
    }

    @PostMapping("/save-legal-agreements")
    public ResponseEntity<Map<String, Object>> saveLegalAgreements(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody LegalAgreementsRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(
                onboardingService.saveLegalAgreements(principal.getId(), request, httpRequest.getRemoteAddr()));
    }

    @PostMapping("/start-kyc")
    public ResponseEntity<Map<String, Object>> startKyc(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(onboardingService.startKyc(principal.getId()));
    }

    @PostMapping("/upload-identity-document")
    public ResponseEntity<Map<String, Object>> uploadIdentityDocument(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "doc_type", defaultValue = "identity") String docType) {
        String path = fileStorageService.store(file, "identity-docs");
        return ResponseEntity.ok(
                onboardingService.saveDocument(principal.getId(), docType, path, file.getContentType()));
    }

    @PostMapping("/upload-tax-form")
    public ResponseEntity<Map<String, Object>> uploadTaxForm(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "form_type", defaultValue = "W-9") String formType) {
        String path = fileStorageService.store(file, "tax-forms");
        return ResponseEntity.ok(onboardingService.saveTaxForm(principal.getId(), formType, path));
    }

    @GetMapping("/get-onboarding-status")
    public ResponseEntity<Map<String, Object>> getOnboardingStatus(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(onboardingService.getOnboardingStatus(principal.getId()));
    }

    @GetMapping("/get-identity-info")
    public ResponseEntity<Map<String, Object>> getIdentityInfo(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(onboardingService.getIdentityInfo(principal.getId()));
    }

    @GetMapping("/get-identity-documents")
    public ResponseEntity<List<UserDocument>> getIdentityDocuments(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(onboardingService.getIdentityDocuments(principal.getId()));
    }

    @GetMapping("/get-verification-status")
    public ResponseEntity<Map<String, Object>> getVerificationStatus(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(onboardingService.getVerificationStatus(principal.getId()));
    }

    @GetMapping("/get-employment")
    public ResponseEntity<UserEmployment> getEmployment(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(onboardingService.getEmployment(principal.getId()));
    }

    @GetMapping("/get-tax-forms")
    public ResponseEntity<List<UserTaxForm>> getTaxForms(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(onboardingService.getTaxForms(principal.getId()));
    }

    @GetMapping("/get-supported-countries")
    public ResponseEntity<?> getSupportedCountries() {
        return ResponseEntity.ok(countryRepository.findAll());
    }
}
