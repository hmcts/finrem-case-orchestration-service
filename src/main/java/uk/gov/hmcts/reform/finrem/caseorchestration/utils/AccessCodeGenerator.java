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
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Utility for generating and managing Citizen UI access codes.
 *
 * <p>Generates access codes for unrepresented applicants and respondents
 * who have not previously activated a code and do not have an active PIN.
 * Existing access code history is preserved when a new PIN is generated.
 *
 * <p>This utility updates the case data in memory. Persisting the changes
 * to CCD is the responsibility of the calling service or handler.
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
     * Ensures an eligible applicant or respondent has an active Citizen UI PIN.
     *
     * <p>A new PIN is generated only if the party is unrepresented, has not
     * previously activated a PIN, and has no existing active PIN.
     * Other recipient types are ignored.
     *
     * @param caseData the case data containing the party's access codes
     * @param recipient the applicant or respondent requiring a PIN
     * @return true if a new PIN was generated, or false if no PIN was needed
     *         or the recipient was ineligible
     */
    public static boolean ensureAccessCodeForRecipient(FinremCaseData caseData,
        PaperNotificationRecipient recipient) {

        return switch (recipient) {
            case APPLICANT -> !caseData.isApplicantRepresentedByASolicitor()
                && ensureAccessCodePresent(
                caseData::getApplicantAccessCodes,
                caseData::setApplicantAccessCodes
            );

            case RESPONDENT -> !caseData.isRespondentRepresentedByASolicitor()
                && ensureAccessCodePresent(
                caseData::getRespondentAccessCodes,
                caseData::setRespondentAccessCodes
            );

            default -> false;
        };
    }

    /**
     * Generates and adds a new PIN if the party has no active or previously activated PIN.
     *
     * @param getter retrieves the party's existing access codes
     * @param setter updates the party's access code collection
     * @return true if a new PIN was added, otherwise false
     */
    private static boolean ensureAccessCodePresent(
        Supplier<List<AccessCodeCollection>> getter,
        Consumer<List<AccessCodeCollection>> setter) {

        List<AccessCodeCollection> existing = getter.get();

        if (!needsAccessCode(existing)) {
            return false;
        }

        List<AccessCodeCollection> accessCodes = existing == null
            ? new ArrayList<>()
            : new ArrayList<>(existing);

        AccessCodeEntry entry = AccessCodeEntry.builder()
            .accessCode(generateAccessCode())
            .isValid(YesOrNo.YES)
            .createdAt(LocalDateTime.now(ZoneOffset.UTC))
            .build();

        AccessCodeCollection collection = AccessCodeCollection.builder()
            .value(entry)
            .build();

        accessCodes.add(collection);
        setter.accept(accessCodes);

        return true;
    }

    /**
     * Checks whether an applicant or respondent requires a new Citizen UI PIN.
     *
     * <p>A PIN is needed only if the party is unrepresented, has not previously
     * activated a PIN, and has no active PIN. This method does not modify case data.
     *
     * @param caseData the case data containing the party's access codes
     * @param recipient the party to check
     * @return true if a new PIN is required, otherwise false
     */
    public static boolean needsAccessCodeForRecipient(
        FinremCaseData caseData,
        PaperNotificationRecipient recipient) {

        return switch (recipient) {
            case APPLICANT -> !caseData.isApplicantRepresentedByASolicitor()
                && needsAccessCode(caseData.getApplicantAccessCodes());

            case RESPONDENT -> !caseData.isRespondentRepresentedByASolicitor()
                && needsAccessCode(caseData.getRespondentAccessCodes());

            default -> false;
        };
    }

    /**
     * Checks whether an access code collection requires a new PIN.
     *
     * <p>A new PIN is not required if any existing code has been activated
     * or is currently valid.
     *
     * @param accessCodes the party's existing access codes
     * @return true if no active or previously activated PIN exists
     */
    private static boolean needsAccessCode(List<AccessCodeCollection> accessCodes) {

        if (accessCodes == null) {
            return true;
        }

        return accessCodes.stream()
            .map(AccessCodeCollection::getValue)
            .filter(Objects::nonNull)
            .noneMatch(entry ->
                entry.getUsedAt() != null
                    || StringUtils.isNotBlank(entry.getUserIdamID())
                    || YesOrNo.YES.equals(entry.getIsValid())
            );
    }
}
