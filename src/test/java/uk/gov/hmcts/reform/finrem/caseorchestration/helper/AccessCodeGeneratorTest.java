package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AccessCodeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AccessCodeEntry;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.AccessCodeGenerator;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccessCodeGeneratorTest {

    @Test
    void generateAccessCode_shouldReturn8CharString() {
        String code = AccessCodeGenerator.generateAccessCode();

        assertNotNull(code);
        assertEquals(8, code.length());
        assertTrue(code.matches("[A-Z2-9]+"));
    }

    @Test
    void setApplicantAccessCode_shouldCreateApplicantAccessCodeWhenNull() {
        FinremCaseData data = new FinremCaseData();

        AccessCodeGenerator.setApplicantAccessCode(data);

        assertNotNull(data.getApplicantAccessCodes());
        assertEquals(1, data.getApplicantAccessCodes().size());

        AccessCodeEntry applicantEntry =
            data.getApplicantAccessCodes().getFirst().getValue();

        assertValidEntry(applicantEntry);
    }

    @Test
    void setRespondentAccessCode_shouldCreateRespondentAccessCodeWhenNull() {
        FinremCaseData data = new FinremCaseData();

        AccessCodeGenerator.setRespondentAccessCode(data);

        assertNotNull(data.getRespondentAccessCodes());
        assertEquals(1, data.getRespondentAccessCodes().size());

        AccessCodeEntry respondentEntry =
            data.getRespondentAccessCodes().getFirst().getValue();

        assertValidEntry(respondentEntry);
    }

    @Test
    void setApplicantAccessCode_shouldCreateApplicantAccessCodeWhenListEmpty() {
        FinremCaseData data = new FinremCaseData();
        data.setApplicantAccessCodes(List.of());

        AccessCodeGenerator.setApplicantAccessCode(data);

        assertNotNull(data.getApplicantAccessCodes());
        assertEquals(1, data.getApplicantAccessCodes().size());

        AccessCodeEntry applicantEntry =
            data.getApplicantAccessCodes().getFirst().getValue();

        assertValidEntry(applicantEntry);
    }

    @Test
    void setRespondentAccessCode_shouldCreateRespondentAccessCodeWhenListEmpty() {
        FinremCaseData data = new FinremCaseData();
        data.setRespondentAccessCodes(List.of());

        AccessCodeGenerator.setRespondentAccessCode(data);

        assertNotNull(data.getRespondentAccessCodes());
        assertEquals(1, data.getRespondentAccessCodes().size());

        AccessCodeEntry respondentEntry =
            data.getRespondentAccessCodes().getFirst().getValue();

        assertValidEntry(respondentEntry);
    }

    @Test
    void setApplicantAccessCode_shouldNotOverwriteExistingCode() {
        AccessCodeCollection existingCollection = accessCode("ABCDEFGH");

        FinremCaseData data = new FinremCaseData();
        data.setApplicantAccessCodes(List.of(existingCollection));

        AccessCodeGenerator.setApplicantAccessCode(data);

        assertEquals(1, data.getApplicantAccessCodes().size());
        assertEquals(
            "ABCDEFGH",
            data.getApplicantAccessCodes().getFirst().getValue().getAccessCode()
        );
    }

    @Test
    void setRespondentAccessCode_shouldNotOverwriteExistingCode() {
        AccessCodeCollection existingCollection = accessCode("ZXCVBNML");

        FinremCaseData data = new FinremCaseData();
        data.setRespondentAccessCodes(List.of(existingCollection));

        AccessCodeGenerator.setRespondentAccessCode(data);

        assertEquals(1, data.getRespondentAccessCodes().size());
        assertEquals(
            "ZXCVBNML",
            data.getRespondentAccessCodes().getFirst().getValue().getAccessCode()
        );
    }

    private AccessCodeCollection accessCode(String accessCode) {
        return AccessCodeCollection.builder()
            .id(UUID.randomUUID())
            .value(
                AccessCodeEntry.builder()
                    .accessCode(accessCode)
                    .isValid(YesOrNo.YES)
                    .build()
            )
            .build();
    }

    private void assertValidEntry(AccessCodeEntry entry) {
        assertNotNull(entry.getAccessCode());
        assertEquals(8, entry.getAccessCode().length());
        assertEquals(YesOrNo.YES, entry.getIsValid());
    }
}
