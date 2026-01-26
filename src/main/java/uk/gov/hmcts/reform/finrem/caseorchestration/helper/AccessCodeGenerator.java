package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import org.apache.commons.lang3.RandomStringUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AccessCodeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AccessCodeEntry;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class AccessCodeGenerator {

    private static final String ALLOWED_CHARS = "ABCDEFGHJKLMNPRSTVWXYZ23456789";

    private AccessCodeGenerator() {
    }

    public static String generateAccessCode() {
        return RandomStringUtils.random(8, 0, ALLOWED_CHARS.length(), false, false, ALLOWED_CHARS.toCharArray(), new SecureRandom());
    }

    public static void setAccessCode(FinremCaseData caseData) {
        ensureAccessCodePresent(
            caseData::getApplicantAccessCodes,
            caseData::setApplicantAccessCodes
        );

        ensureAccessCodePresent(
            caseData::getRespondentAccessCodes,
            caseData::setRespondentAccessCodes
        );
    }

    private static void ensureAccessCodePresent(
        Supplier<List<AccessCodeCollection>> getter,
        Consumer<List<AccessCodeCollection>> setter) {

        List<AccessCodeCollection> current = getter.get();
        if (current == null || current.isEmpty()) {

            AccessCodeEntry entry = AccessCodeEntry.builder()
                .accessCode(generateAccessCode())
                .createdAt(LocalDateTime.now())
                .validUntil(setValidUntilDate(LocalDateTime.now()))
                .isValid(YesOrNo.YES)
                .build();

            AccessCodeCollection collection = AccessCodeCollection.builder()
                .value(entry)
                .build();

            setter.accept(new ArrayList<>(List.of(collection)));
        }
    }

    private static LocalDateTime setValidUntilDate(LocalDateTime createdAt) {
        return createdAt.plusDays(90);
    }
}
