package uk.gov.hmcts.reform.finrem.caseorchestration.utils;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AccessCodeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AccessCodeEntry;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Generates Citizen UI access codes and appends them to a recipient's history.
 *
 * <p>New codes are added only for unrepresented applicants or respondents
 * without an active or previously activated code. Existing history is preserved.
 *
 * <p>Changes are made in memory. The caller is responsible for checking the
 * case type and feature toggle, and persisting changes to CCD.
 */
public final class AccessCodeGenerator {

    private static final String ALLOWED_CHARS = "ABCDEFGHJKLMNPRSTVWXYZ23456789";

    private AccessCodeGenerator() {
    }

    /**
     * Generates a random eight-character access code using a secure random generator.
     *
     * @return the generated access code
     */
    public static String generateAccessCode() {
        return RandomStringUtils.random(8, 0, ALLOWED_CHARS.length(), false, false, ALLOWED_CHARS.toCharArray(), new SecureRandom());
    }

    /**
     * Checks whether the recipient requires a new access code without modifying case data.
     *
     * <p>Returns true only for an unrepresented applicant or respondent whose
     * history contains no active or previously activated code.
     *
     * @param caseData the case data containing representation details and access-code history
     * @param recipient the recipient to check
     * @return true if a new code is required; false otherwise, including for unsupported recipients
     */
    public static boolean doesRecipientNeedNewAccessCode(FinremCaseData caseData,
                                                         PaperNotificationRecipient recipient) {

        return switch (recipient) {
            case APPLICANT -> !caseData.isApplicantRepresentedByASolicitor()
                && !hasActiveOrActivatedCode(caseData.getApplicantAccessCodes());
            case RESPONDENT -> !caseData.isRespondentRepresentedByASolicitor()
                && !hasActiveOrActivatedCode(caseData.getRespondentAccessCodes());
            default -> false;
        };
    }

    /**
     * Generates and appends an access code if the recipient requires one.
     *
     * <p>Updates the recipient's access-code collection in memory, preserving
     * existing history. The caller is responsible for persisting the change.
     *
     * @param caseData the case data to update
     * @param recipient the recipient for whom to generate a code
     * @return true if a new code was added; false if no change was required
     */
    public static boolean generateAccessCodeIfNeeded(
        FinremCaseData caseData,
        PaperNotificationRecipient recipient) {

        if (!doesRecipientNeedNewAccessCode(caseData, recipient)) {
            return false;
        }

        switch (recipient) {
            case APPLICANT -> caseData.setApplicantAccessCodes(
                appendNewAccessCode(caseData.getApplicantAccessCodes())
            );
            case RESPONDENT -> caseData.setRespondentAccessCodes(
                appendNewAccessCode(caseData.getRespondentAccessCodes())
            );
            default -> {
                return false;
            }
        }

        return true;
    }

    /**
     * Creates a collection containing the existing entries followed by a new valid code.
     *
     * <p>The new entry records its creation time in UTC. The supplied list is not modified.
     *
     * @param existing the existing access-code collection, or null if none exists
     * @return a new list containing the existing entries and the generated code
     */
    private static List<AccessCodeCollection> appendNewAccessCode(
        List<AccessCodeCollection> existing) {

        List<AccessCodeCollection> accessCodes = existing == null
            ? new ArrayList<>()
            : new ArrayList<>(existing);

        AccessCodeEntry entry = AccessCodeEntry.builder()
            .accessCode(generateAccessCode())
            .isValid(YesOrNo.YES)
            .createdAt(LocalDateTime.now(ZoneOffset.UTC))
            .build();

        accessCodes.add(
            AccessCodeCollection.builder()
                .value(entry)
                .build()
        );

        return accessCodes;
    }

    /**
     * Checks whether the history contains an active or previously activated code.
     *
     * <p>A code is active when its validity flag is YES. It is considered previously
     * activated when its usage timestamp is populated or its user IDAM ID is nonblank.
     * Null collection items and null entry values are ignored.
     *
     * @param accessCodes the access-code history, or null if none exists
     * @return true if a matching entry exists; false for null or empty collections
     *         or when no entry matches
     */
    private static boolean hasActiveOrActivatedCode(
        List<AccessCodeCollection> accessCodes) {

        if (accessCodes == null) {
            return false;
        }

        return accessCodes.stream()
            .filter(Objects::nonNull)
            .map(AccessCodeCollection::getValue)
            .filter(Objects::nonNull)
            .anyMatch(entry ->
                entry.getUsedAt() != null
                    || StringUtils.isNotBlank(entry.getUserIdamID())
                    || YesOrNo.YES.equals(entry.getIsValid())
            );
    }
}
