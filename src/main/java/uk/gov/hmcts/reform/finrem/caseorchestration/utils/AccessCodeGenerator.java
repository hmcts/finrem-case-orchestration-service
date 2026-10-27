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

public final class AccessCodeGenerator {

    private static final String ALLOWED_CHARS = "ABCDEFGHJKLMNPRSTVWXYZ23456789";

    private AccessCodeGenerator() {
    }

    public static String generateAccessCode() {
        return RandomStringUtils.random(8, 0, ALLOWED_CHARS.length(), false, false, ALLOWED_CHARS.toCharArray(), new SecureRandom());
    }

    public static void setAccessCode(FinremCaseData caseData, boolean alwaysGenerateCUIAccessCodes) {

        boolean shouldGenerateApplicantCode = !caseData.isApplicantRepresentedByASolicitor() || alwaysGenerateCUIAccessCodes;
        if (shouldGenerateApplicantCode) {
            ensureAccessCodePresent(
                caseData::getApplicantAccessCodes,

                caseData::setApplicantAccessCodes
            );
        }

        boolean shouldGenerateRespondentCode = !caseData.isRespondentRepresentedByASolicitor() || alwaysGenerateCUIAccessCodes;
        if (shouldGenerateRespondentCode) {
            ensureAccessCodePresent(
                caseData::getRespondentAccessCodes,
                caseData::setRespondentAccessCodes
            );
        }
    }

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
