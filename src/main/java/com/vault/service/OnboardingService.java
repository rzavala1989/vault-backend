package com.vault.service;

import com.vault.dto.*;
import com.vault.entity.*;
import com.vault.exception.ApiException;
import com.vault.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserAddressRepository userAddressRepository;
    private final UserFinancialRepository userFinancialRepository;
    private final UserEmploymentRepository userEmploymentRepository;
    private final UserIdentityRepository userIdentityRepository;
    private final UserDocumentRepository userDocumentRepository;
    private final UserAgreementRepository userAgreementRepository;
    private final UserTaxFormRepository userTaxFormRepository;

    @Transactional
    public Map<String, Object> savePersonalInfo(Long userId, PersonalInfoRequest req) {
        User user = getUser(userId);

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElse(UserProfile.builder().user(user).build());

        profile.setFirstName(req.getFirstName());
        profile.setLastName(req.getLastName());
        profile.setPhone(req.getPhone());
        profile.setNationality(req.getNationality());
        profile.setTaxIdEncrypted(req.getTaxId());

        if (req.getDateOfBirth() != null) {
            profile.setDateOfBirth(LocalDate.parse(req.getDateOfBirth()));
        }

        userProfileRepository.save(profile);

        if (req.getStreet() != null) {
            UserAddress address = userAddressRepository.findByUserId(userId)
                    .orElse(UserAddress.builder().user(user).build());
            address.setStreet(req.getStreet());
            address.setCity(req.getCity());
            address.setState(req.getState());
            address.setZip(req.getZip());
            address.setCountry(req.getCountry());
            userAddressRepository.save(address);
        }

        advanceOnboarding(user, OnboardingStatus.PERSONAL_INFO);

        return Map.of("success", true);
    }

    @Transactional
    public Map<String, Object> saveFinancialBands(Long userId, FinancialBandsRequest req) {
        User user = getUser(userId);

        UserFinancial financial = userFinancialRepository.findByUserId(userId)
                .orElse(UserFinancial.builder().user(user).build());

        financial.setAnnualIncomeMin(req.getAnnualIncomeMin());
        financial.setAnnualIncomeMax(req.getAnnualIncomeMax());
        financial.setLiquidNetWorthMin(req.getLiquidNetWorthMin());
        financial.setLiquidNetWorthMax(req.getLiquidNetWorthMax());
        financial.setTotalNetWorthMin(req.getTotalNetWorthMin());
        financial.setTotalNetWorthMax(req.getTotalNetWorthMax());
        financial.setFundingSources(req.getFundingSources());

        userFinancialRepository.save(financial);

        advanceOnboarding(user, OnboardingStatus.FINANCIAL);

        return Map.of("success", true);
    }

    @Transactional
    public Map<String, Object> saveCitizenshipAndTax(Long userId, CitizenshipTaxRequest req) {
        User user = getUser(userId);

        UserIdentity identity = userIdentityRepository.findByUserId(userId)
                .orElse(UserIdentity.builder().user(user).build());

        identity.setIdType(req.getIdType());
        identity.setIdNumberEncrypted(req.getIdNumber());
        identity.setCountryCode(req.getCountryCode());

        userIdentityRepository.save(identity);

        advanceOnboarding(user, OnboardingStatus.CITIZENSHIP);

        return Map.of("success", true);
    }

    @Transactional
    public Map<String, Object> saveEmployment(Long userId, EmploymentRequest req) {
        User user = getUser(userId);

        UserEmployment employment = userEmploymentRepository.findByUserId(userId)
                .orElse(UserEmployment.builder().user(user).build());

        employment.setEmploymentStatus(req.getEmploymentStatus());
        employment.setEmployer(req.getEmployer());
        employment.setPosition(req.getPosition());
        employment.setEmployerAddress(req.getEmployerAddress());

        userEmploymentRepository.save(employment);

        advanceOnboarding(user, OnboardingStatus.EMPLOYMENT);

        return Map.of("success", true);
    }

    @Transactional
    public Map<String, Object> saveLegalAgreements(Long userId, LegalAgreementsRequest req, String ipAddress) {
        User user = getUser(userId);

        if (req.getAgreements() != null) {
            for (LegalAgreementsRequest.Agreement a : req.getAgreements()) {
                UserAgreement agreement = UserAgreement.builder()
                        .user(user)
                        .agreementType(a.getAgreementType())
                        .revision(a.getRevision())
                        .ipAddress(ipAddress)
                        .signedAt(Instant.now())
                        .build();
                userAgreementRepository.save(agreement);
            }
        }

        advanceOnboarding(user, OnboardingStatus.AGREEMENTS);

        return Map.of("success", true);
    }

    @Transactional
    public Map<String, Object> startKyc(Long userId) {
        User user = getUser(userId);
        advanceOnboarding(user, OnboardingStatus.KYC_PENDING);

        // Auto-approve KYC for mock
        user.setOnboardingStatus(OnboardingStatus.KYC_APPROVED);
        userRepository.save(user);

        log.info("KYC auto-approved for user {}", userId);
        return Map.of("success", true, "status", "approved");
    }

    public Map<String, Object> saveDocument(Long userId, String docType, String filePath, String mimeType) {
        User user = getUser(userId);
        UserDocument doc = UserDocument.builder()
                .user(user)
                .docType(docType)
                .filePath(filePath)
                .mimeType(mimeType)
                .uploadedAt(Instant.now())
                .build();
        userDocumentRepository.save(doc);
        return Map.of("success", true, "document_id", doc.getId());
    }

    public Map<String, Object> saveTaxForm(Long userId, String formType, String filePath) {
        User user = getUser(userId);
        UserTaxForm form = UserTaxForm.builder()
                .user(user)
                .formType(formType)
                .filePath(filePath)
                .uploadedAt(Instant.now())
                .build();
        userTaxFormRepository.save(form);
        return Map.of("success", true, "tax_form_id", form.getId());
    }

    public Map<String, Object> getOnboardingStatus(Long userId) {
        User user = getUser(userId);
        return Map.of(
                "status", user.getOnboardingStatus().name(),
                "user_id", userId
        );
    }

    public Map<String, Object> getIdentityInfo(Long userId) {
        UserIdentity identity = userIdentityRepository.findByUserId(userId)
                .orElseThrow(() -> ApiException.notFound("NOT_FOUND", "Identity info not found"));
        return Map.of(
                "id_type", nullSafe(identity.getIdType()),
                "country_code", nullSafe(identity.getCountryCode()),
                "verification_status", nullSafe(identity.getVerificationStatus())
        );
    }

    public List<UserDocument> getIdentityDocuments(Long userId) {
        return userDocumentRepository.findByUserId(userId);
    }

    public Map<String, Object> getVerificationStatus(Long userId) {
        UserIdentity identity = userIdentityRepository.findByUserId(userId).orElse(null);
        String status = identity != null ? identity.getVerificationStatus() : "not_started";
        return Map.of("verification_status", status);
    }

    public UserEmployment getEmployment(Long userId) {
        return userEmploymentRepository.findByUserId(userId)
                .orElseThrow(() -> ApiException.notFound("NOT_FOUND", "Employment info not found"));
    }

    public List<UserTaxForm> getTaxForms(Long userId) {
        return userTaxFormRepository.findByUserId(userId);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("USER_NOT_FOUND", "User not found"));
    }

    private void advanceOnboarding(User user, OnboardingStatus newStatus) {
        if (user.getOnboardingStatus().ordinal() < newStatus.ordinal()) {
            user.setOnboardingStatus(newStatus);
            userRepository.save(user);
        }
    }

    private String nullSafe(String value) {
        return value != null ? value : "";
    }
}
