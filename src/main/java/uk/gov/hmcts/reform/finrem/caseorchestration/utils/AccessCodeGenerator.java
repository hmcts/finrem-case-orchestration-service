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
     * Generates and adds a new PIN to the party's access code collection if required.
     *
     * <p>PIN generation is skipped if any previous code has been activated
     * or an active PIN already exists. Otherwise, a new PIN with a UTC
     * creation timestamp is appended to the existing collection.
     *
     * @param getter retrieves the party's existing access codes
     * @param setter updates the party's access code collection
     * @return true if a new PIN was added, otherwise false
     */
    private static boolean ensureAccessCodePresent(
        Supplier<List<AccessCodeCollection>> getter,
        Consumer<List<AccessCodeCollection>> setter) {

        List<AccessCodeCollection> existing = getter.get();

        List<AccessCodeCollection> accessCodes = existing == null
            ? new ArrayList<>()
            : new ArrayList<>(existing);

        boolean previouslyActivated = accessCodes.stream()
            .map(AccessCodeCollection::getValue)
            .filter(Objects::nonNull)
            .anyMatch(entry ->
                entry.getUsedAt() != null
                    || StringUtils.isNotBlank(entry.getUserIdamID())
            );

        if (previouslyActivated) {
            return false;
        }

        boolean hasActivePin = accessCodes.stream()
            .map(AccessCodeCollection::getValue)
            .filter(Objects::nonNull)
            .anyMatch(entry ->
                YesOrNo.YES.equals(entry.getIsValid())
            );

        if (hasActivePin) {
            return false;
        }

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

}
