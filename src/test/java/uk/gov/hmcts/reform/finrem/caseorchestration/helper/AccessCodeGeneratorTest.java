package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import org.junit.jupiter.api.Nested;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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

    @Nested
    class TestGenerateAccessCodeIfNeeded {
        @Test
        void ensureAccessCodeForRecipient_shouldGenerateApplicantCodeOnly() {
            FinremCaseData data = new FinremCaseData();

            boolean generated = AccessCodeGenerator.generateAccessCodeIfNeeded(data, APPLICANT);

            assertTrue(generated);
            assertEquals(1, data.getApplicantAccessCodes().size());
            assertValidEntry(data.getApplicantAccessCodes().getFirst().getValue());
            assertNull(data.getRespondentAccessCodes());
        }

        @Test
        void ensureAccessCodeForRecipient_shouldGenerateRespondentCodeOnly() {
            FinremCaseData data = new FinremCaseData();

            boolean generated = AccessCodeGenerator.generateAccessCodeIfNeeded(data, RESPONDENT);

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

            assertFalse(AccessCodeGenerator.generateAccessCodeIfNeeded(data, APPLICANT));
            assertFalse(AccessCodeGenerator.generateAccessCodeIfNeeded(data, RESPONDENT));

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

            boolean generated = AccessCodeGenerator.generateAccessCodeIfNeeded(data, APPLICANT);

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

            boolean generated = AccessCodeGenerator.generateAccessCodeIfNeeded(data, APPLICANT);

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

            boolean generated = AccessCodeGenerator.generateAccessCodeIfNeeded(data, APPLICANT);

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

            boolean generated = AccessCodeGenerator.generateAccessCodeIfNeeded(data, APPLICANT);

            assertTrue(generated);
            assertEquals(2, data.getApplicantAccessCodes().size());
            assertSame(existing, data.getApplicantAccessCodes().getFirst());
            assertValidEntry(data.getApplicantAccessCodes().get(1).getValue());
        }

        @Test
        void ensureAccessCodeForRecipient_shouldNotGenerateForIntervener() {
            FinremCaseData data = new FinremCaseData();

            boolean generated = AccessCodeGenerator.generateAccessCodeIfNeeded(data, INTERVENER_ONE);

            assertFalse(generated);
            assertNull(data.getApplicantAccessCodes());
            assertNull(data.getRespondentAccessCodes());
        }
    }

    @Nested
    class TestDoesRecipientNeedNewAccessCode {
        @Test
        void shouldReturnTrueWhenApplicantHasNoAccessCodes() {
            FinremCaseData caseData = mock(FinremCaseData.class);

            boolean result = AccessCodeGenerator.doesRecipientNeedNewAccessCode(
                caseData, APPLICANT
            );

            assertTrue(result);
            verify(caseData, never()).setApplicantAccessCodes(any());
        }

        @Test
        void shouldReturnTrueWhenRespondentHasNoAccessCodes() {
            FinremCaseData caseData = mock(FinremCaseData.class);

            boolean result = AccessCodeGenerator.doesRecipientNeedNewAccessCode(
                caseData, RESPONDENT
            );

            assertTrue(result);
            verify(caseData, never()).setRespondentAccessCodes(any());
        }

        @Test
        void shouldReturnFalseWhenApplicantIsRepresented() {
            FinremCaseData caseData = mock(FinremCaseData.class);
            when(caseData.isApplicantRepresentedByASolicitor()).thenReturn(true);

            boolean result = AccessCodeGenerator.doesRecipientNeedNewAccessCode(
                caseData, APPLICANT
            );

            assertFalse(result);
        }

        @Test
        void shouldReturnFalseWhenRespondentIsRepresented() {
            FinremCaseData caseData = mock(FinremCaseData.class);
            when(caseData.isRespondentRepresentedByASolicitor()).thenReturn(true);

            boolean result = AccessCodeGenerator.doesRecipientNeedNewAccessCode(
                caseData, RESPONDENT
            );

            assertFalse(result);
        }

        @Test
        void shouldReturnFalseWhenApplicantHasActivePin() {
            FinremCaseData caseData = mock(FinremCaseData.class);

            AccessCodeEntry entry = AccessCodeEntry.builder()
                .isValid(YesOrNo.YES)
                .build();

            when(caseData.getApplicantAccessCodes()).thenReturn(
                List.of(AccessCodeCollection.builder().value(entry).build())
            );

            boolean result = AccessCodeGenerator.doesRecipientNeedNewAccessCode(
                caseData, APPLICANT
            );

            assertFalse(result);
            verify(caseData, never()).setApplicantAccessCodes(any());
        }

        @Test
        void shouldReturnFalseWhenRespondentPreviouslyActivatedPin() {
            FinremCaseData caseData = mock(FinremCaseData.class);

            AccessCodeEntry entry = AccessCodeEntry.builder()
                .userIdamID("test-user-id")
                .isValid(YesOrNo.NO)
                .build();

            when(caseData.getRespondentAccessCodes()).thenReturn(
                List.of(AccessCodeCollection.builder().value(entry).build())
            );

            boolean result = AccessCodeGenerator.doesRecipientNeedNewAccessCode(
                caseData, RESPONDENT
            );

            assertFalse(result);
        }

        @Test
        void shouldReturnTrueWhenApplicantHasInvalidUnusedPin() {
            FinremCaseData caseData = mock(FinremCaseData.class);

            AccessCodeEntry entry = AccessCodeEntry.builder()
                .isValid(YesOrNo.NO)
                .build();

            List<AccessCodeCollection> existingCodes = List.of(
                AccessCodeCollection.builder().value(entry).build()
            );

            when(caseData.getApplicantAccessCodes()).thenReturn(existingCodes);

            boolean result = AccessCodeGenerator.doesRecipientNeedNewAccessCode(
                caseData, APPLICANT
            );

            assertTrue(result);
            assertSame(existingCodes, caseData.getApplicantAccessCodes());
            verify(caseData, never()).setApplicantAccessCodes(any());
        }

    }

    private void assertValidEntry(AccessCodeEntry entry) {
        assertNotNull(entry.getAccessCode());
        assertEquals(8, entry.getAccessCode().length());
        assertEquals(YesOrNo.YES, entry.getIsValid());
        assertNotNull(entry.getCreatedAt());
    }
}
