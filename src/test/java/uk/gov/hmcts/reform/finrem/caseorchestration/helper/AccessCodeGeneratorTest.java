package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AccessCodeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AccessCodeEntry;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.AccessCodeGenerator;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.INTERVENER_ONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.RESPONDENT;

class AccessCodeGeneratorTest {

    @Test
    void generateAccessCode_shouldReturn8CharString() {
        String code = AccessCodeGenerator.generateAccessCode();

        assertNotNull(code);
        assertEquals(8, code.length());
        assertTrue(code.matches("[A-Z2-9]+")); // allowed chars in class
    }

    @Test
    void setAccessCode_shouldCreateApplicantAndRespondentAccessCodesWhenEmpty() {
        FinremCaseData data = new FinremCaseData();

        boolean alwaysGenerateCUIAccessCodes = false;
        AccessCodeGenerator.setAccessCode(data, alwaysGenerateCUIAccessCodes);

        assertNotNull(data.getApplicantAccessCodes());
        assertNotNull(data.getRespondentAccessCodes());
        assertEquals(1, data.getApplicantAccessCodes().size());
        assertEquals(1, data.getRespondentAccessCodes().size());

        AccessCodeEntry appEntry = data.getApplicantAccessCodes().getFirst().getValue();
        AccessCodeEntry respEntry = data.getRespondentAccessCodes().getFirst().getValue();

        assertValidEntry(appEntry);
        assertValidEntry(respEntry);
    }

    @Test
    void setAccessCode_shouldNotOverwriteExistingCodes() {
        AccessCodeEntry existingEntry = AccessCodeEntry.builder()
            .accessCode("ABCDEFGH")
            .isValid(YesOrNo.YES)
            .build();

        AccessCodeCollection existingCollection =
            AccessCodeCollection.builder().value(existingEntry).build();

        FinremCaseData data = new FinremCaseData();
        data.setApplicantAccessCodes(List.of(existingCollection));
        data.setRespondentAccessCodes(List.of(existingCollection));

        boolean alwaysGenerateCUIAccessCodes = false;
        AccessCodeGenerator.setAccessCode(data, alwaysGenerateCUIAccessCodes);

        // Ensure no new entries added
        assertEquals(1, data.getApplicantAccessCodes().size());
        assertEquals(1, data.getRespondentAccessCodes().size());
        assertEquals("ABCDEFGH", data.getApplicantAccessCodes().getFirst().getValue().getAccessCode());
    }

    @Test
    void setAccessCode_shouldNotCreateCodesForRepresentedParties() {
        FinremCaseData data = new FinremCaseData();
        data.setContactDetailsWrapper(ContactDetailsWrapper.builder()
            .applicantRepresented(YesOrNo.YES)
            .contestedRespondentRepresented(YesOrNo.YES)
            .build());

        boolean alwaysGenerateCUIAccessCodes = false;
        AccessCodeGenerator.setAccessCode(data, alwaysGenerateCUIAccessCodes);

        assertNull(data.getApplicantAccessCodes());
        assertNull(data.getRespondentAccessCodes());
    }

    @Test
    void setAccessCode_shouldCreateCodeForUnrepresentedPartyInMixedScenario() {
        FinremCaseData data = new FinremCaseData();
        data.setContactDetailsWrapper(ContactDetailsWrapper.builder()
            .applicantRepresented(YesOrNo.YES)
            .contestedRespondentRepresented(YesOrNo.NO)
            .build());

        boolean alwaysGenerateCUIAccessCodes = false;
        AccessCodeGenerator.setAccessCode(data, alwaysGenerateCUIAccessCodes);

        assertNull(data.getApplicantAccessCodes());
        assertNotNull(data.getRespondentAccessCodes());
        assertEquals(1, data.getRespondentAccessCodes().size());
        assertValidEntry(data.getRespondentAccessCodes().getFirst().getValue());
    }

    @Test
    void setAccessCode_shouldAlwaysCreateCodes_when_alwaysGenerateCUIAccessCodesTrue() {
        FinremCaseData representedCase = FinremCaseData.builder()
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .applicantRepresented(YesOrNo.YES)
                .contestedRespondentRepresented(YesOrNo.YES)
                .build())
            .build();

        FinremCaseData notRepresentedCase = new FinremCaseData();

        boolean alwaysGenerateCUIAccessCodes = true;
        AccessCodeGenerator.setAccessCode(representedCase, alwaysGenerateCUIAccessCodes);
        AccessCodeGenerator.setAccessCode(notRepresentedCase, alwaysGenerateCUIAccessCodes);

        assertNotNull(representedCase.getApplicantAccessCodes());
        assertNotNull(representedCase.getRespondentAccessCodes());
        assertEquals(1, representedCase.getApplicantAccessCodes().size());
        assertEquals(1, representedCase.getRespondentAccessCodes().size());

        assertValidEntry(representedCase.getApplicantAccessCodes().getFirst().getValue());
        assertValidEntry(representedCase.getRespondentAccessCodes().getFirst().getValue());

        assertNotNull(notRepresentedCase.getApplicantAccessCodes());
        assertNotNull(notRepresentedCase.getRespondentAccessCodes());
        assertEquals(1, notRepresentedCase.getApplicantAccessCodes().size());
        assertEquals(1, notRepresentedCase.getRespondentAccessCodes().size());

        assertValidEntry(notRepresentedCase.getApplicantAccessCodes().getFirst().getValue());
        assertValidEntry(notRepresentedCase.getRespondentAccessCodes().getFirst().getValue());
    }

    @Test
    void ensureAccessCodeForRecipient_shouldGenerateApplicantCodeOnly() {
        FinremCaseData data = new FinremCaseData();

        boolean generated = AccessCodeGenerator.ensureAccessCodeForRecipient(data, APPLICANT);

        assertTrue(generated);
        assertEquals(1, data.getApplicantAccessCodes().size());
        assertValidEntry(data.getApplicantAccessCodes().getFirst().getValue());
        assertNull(data.getRespondentAccessCodes());
    }

    @Test
    void ensureAccessCodeForRecipient_shouldGenerateRespondentCodeOnly() {
        FinremCaseData data = new FinremCaseData();

        boolean generated = AccessCodeGenerator.ensureAccessCodeForRecipient(data, RESPONDENT);

        assertTrue(generated);
        assertEquals(1, data.getRespondentAccessCodes().size());
        assertValidEntry(data.getRespondentAccessCodes().getFirst().getValue());
        assertNull(data.getApplicantAccessCodes());
    }

    @Test
    void ensureAccessCodeForRecipient_shouldNotGenerateForRepresentedParties() {
        FinremCaseData data = new FinremCaseData();
        data.setContactDetailsWrapper(ContactDetailsWrapper.builder()
            .applicantRepresented(YesOrNo.YES)
            .contestedRespondentRepresented(YesOrNo.YES)
            .build());

        assertFalse(AccessCodeGenerator.ensureAccessCodeForRecipient(data, APPLICANT));
        assertFalse(AccessCodeGenerator.ensureAccessCodeForRecipient(data, RESPONDENT));

        assertNull(data.getApplicantAccessCodes());
        assertNull(data.getRespondentAccessCodes());
    }

    @Test
    void ensureAccessCodeForRecipient_shouldNotReplaceActiveCode() {
        FinremCaseData data = new FinremCaseData();
        AccessCodeCollection existing = AccessCodeCollection.builder()
            .value(AccessCodeEntry.builder()
                .accessCode("ABCDEFGH")
                .isValid(YesOrNo.YES)
                .build())
            .build();

        data.setApplicantAccessCodes(List.of(existing));

        boolean generated = AccessCodeGenerator.ensureAccessCodeForRecipient(data, APPLICANT);

        assertFalse(generated);
        assertEquals(1, data.getApplicantAccessCodes().size());
        assertSame(existing, data.getApplicantAccessCodes().getFirst());
    }

    @Test
    void ensureAccessCodeForRecipient_shouldNotGenerateWhenPreviouslyUsed() {
        FinremCaseData data = new FinremCaseData();
        AccessCodeCollection existing = AccessCodeCollection.builder()
            .value(AccessCodeEntry.builder()
                .isValid(YesOrNo.NO)
                .usedAt(LocalDateTime.now())
                .build())
            .build();

        data.setApplicantAccessCodes(List.of(existing));

        boolean generated = AccessCodeGenerator.ensureAccessCodeForRecipient(data, APPLICANT);

        assertFalse(generated);
        assertEquals(1, data.getApplicantAccessCodes().size());
    }

    @Test
    void ensureAccessCodeForRecipient_shouldNotGenerateWhenLinkedToIdamUser() {
        FinremCaseData data = new FinremCaseData();
        AccessCodeCollection existing = AccessCodeCollection.builder()
            .value(AccessCodeEntry.builder()
                .isValid(YesOrNo.NO)
                .userIdamID("user-123")
                .build())
            .build();

        data.setApplicantAccessCodes(List.of(existing));

        boolean generated = AccessCodeGenerator.ensureAccessCodeForRecipient(data, APPLICANT);

        assertFalse(generated);
        assertEquals(1, data.getApplicantAccessCodes().size());
    }

    @Test
    void ensureAccessCodeForRecipient_shouldPreserveHistoryWhenReplacingInvalidCode() {
        FinremCaseData data = new FinremCaseData();
        AccessCodeCollection existing = AccessCodeCollection.builder()
            .value(AccessCodeEntry.builder()
                .accessCode("ABCDEFGH")
                .isValid(YesOrNo.NO)
                .build())
            .build();

        data.setApplicantAccessCodes(List.of(existing));

        boolean generated = AccessCodeGenerator.ensureAccessCodeForRecipient(data, APPLICANT);

        assertTrue(generated);
        assertEquals(2, data.getApplicantAccessCodes().size());
        assertSame(existing, data.getApplicantAccessCodes().getFirst());
        assertValidEntry(data.getApplicantAccessCodes().get(1).getValue());
    }

    @Test
    void ensureAccessCodeForRecipient_shouldNotGenerateForIntervener() {
        FinremCaseData data = new FinremCaseData();

        boolean generated = AccessCodeGenerator.ensureAccessCodeForRecipient(data, INTERVENER_ONE);

        assertFalse(generated);
        assertNull(data.getApplicantAccessCodes());
        assertNull(data.getRespondentAccessCodes());
    }

    private void assertValidEntry(AccessCodeEntry entry) {
        assertNotNull(entry.getAccessCode());
        assertEquals(8, entry.getAccessCode().length());
        assertEquals(YesOrNo.YES, entry.getIsValid());
        assertNotNull(entry.getCreatedAt());
    }
}
