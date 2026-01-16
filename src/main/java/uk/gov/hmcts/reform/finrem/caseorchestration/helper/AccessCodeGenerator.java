package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import org.apache.commons.lang3.RandomStringUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AccessCodeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AccessCodeEntry;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public final class AccessCodeGenerator {


    private static final String ALLOWED_CHARS = "ABCDEFGHJKLMNPRSTVWXYZ23456789";

    private AccessCodeGenerator() {
    }

    public static String generateAccessCode() {
        return RandomStringUtils.random(8, 0, ALLOWED_CHARS.length(), false, false, ALLOWED_CHARS.toCharArray(), new SecureRandom());
    }

    private static LocalDateTime setExpiryDate(LocalDateTime createdAt) {
        return createdAt.plusDays(90);
    }

    private static boolean isAccessCodeValid(LocalDateTime validUntil) {
        LocalDateTime today = LocalDateTime.now();
        return today.isBefore(validUntil);
    }

    public static void setAccessCode(FinremCaseData finremCaseData) {
        AccessCodeCollection newApplicantAccessCodes = new AccessCodeCollection();
        AccessCodeCollection oldApplicantAccessCodes = new AccessCodeCollection();
        AccessCodeCollection newRespondentAccessCodes = new AccessCodeCollection();
        AccessCodeCollection oldRespondentAccessCodes = new AccessCodeCollection();

        newApplicantAccessCodes.setValue(getAccessCodeEntry(generateAccessCode(), LocalDateTime.now(), setExpiryDate(LocalDateTime.now())));
        oldApplicantAccessCodes.setValue(getAccessCodeEntry(generateAccessCode(), LocalDateTime.now().minusDays(180), LocalDateTime.now().minusDays(90)));
        newRespondentAccessCodes.setValue(getAccessCodeEntry(generateAccessCode(), LocalDateTime.now(), AccessCodeGenerator.setExpiryDate(LocalDateTime.now())));
        oldRespondentAccessCodes.setValue(getAccessCodeEntry(generateAccessCode(), LocalDateTime.now().minusDays(180), LocalDateTime.now().minusDays(90)));
        finremCaseData.setApplicantAccessCodes(List.of(newApplicantAccessCodes, oldApplicantAccessCodes));
        finremCaseData.setRespondentAccessCodes(List.of(newRespondentAccessCodes, oldRespondentAccessCodes));
    }

    private static AccessCodeEntry getAccessCodeEntry(String accessCode, LocalDateTime createdAt, LocalDateTime validUntil) {
        AccessCodeEntry accessCodeEntry = new AccessCodeEntry();
        accessCodeEntry.setAccessCode(accessCode);
        accessCodeEntry.setCreatedAt(createdAt);
        accessCodeEntry.setValidUntil(validUntil);
        return accessCodeEntry;
    }

    public static Optional<String> getValidAccessCode(List<AccessCodeCollection> accessCodeCollections) {
        return accessCodeCollections.stream()
            .filter(accessCodeCollection -> isAccessCodeValid(accessCodeCollection.getValue().getValidUntil()))
            .map(accessCodeCollection -> accessCodeCollection.getValue().getAccessCode())
            .findFirst();
    }
}
