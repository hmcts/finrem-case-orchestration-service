package uk.gov.hmcts.reform.finrem.caseorchestration.service.citizenui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AccessCodeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AccessCodeEntry;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.RESPONDENT;

@ExtendWith(MockitoExtension.class)
class CitizenPinInPostServiceTest {

    @Mock
    private FinremCaseDetails caseDetails;

    private CitizenPinInPostService service;

    @BeforeEach
    void setUp() {
        service = new CitizenPinInPostService();
    }

    @Test
    void shouldGeneratePinForContestedApplicant() {
        FinremCaseData data = new FinremCaseData();

        when(caseDetails.getCaseType()).thenReturn(CaseType.CONTESTED);
        when(caseDetails.getData()).thenReturn(data);

        boolean generated = service.generatePinIfNeeded(caseDetails, APPLICANT);

        assertTrue(generated);
        assertEquals(1, data.getApplicantAccessCodes().size());
        assertNull(data.getRespondentAccessCodes());
    }

    @Test
    void shouldGeneratePinForContestedRespondent() {
        FinremCaseData data = new FinremCaseData();

        when(caseDetails.getCaseType()).thenReturn(CaseType.CONTESTED);
        when(caseDetails.getData()).thenReturn(data);

        boolean generated = service.generatePinIfNeeded(caseDetails, RESPONDENT);

        assertTrue(generated);
        assertEquals(1, data.getRespondentAccessCodes().size());
        assertNull(data.getApplicantAccessCodes());
    }

    @Test
    void shouldNotGeneratePinForConsentedCase() {
        when(caseDetails.getCaseType()).thenReturn(CaseType.CONSENTED);

        boolean generated = service.generatePinIfNeeded(caseDetails, APPLICANT);

        assertFalse(generated);
        verify(caseDetails, never()).getData();
    }

    @Test
    void shouldNotGeneratePinForRepresentedParty() {
        FinremCaseData data = new FinremCaseData();
        data.setContactDetailsWrapper(ContactDetailsWrapper.builder()
            .applicantRepresented(YesOrNo.YES)
            .build());

        when(caseDetails.getCaseType()).thenReturn(CaseType.CONTESTED);
        when(caseDetails.getData()).thenReturn(data);

        boolean generated = service.generatePinIfNeeded(caseDetails, APPLICANT);

        assertFalse(generated);
        assertNull(data.getApplicantAccessCodes());
    }

    @Nested
    class FindAccessCodeForRecipient {

        @Test
        void shouldReadEachRecipientsOwnCode() {
            FinremCaseData caseData = new FinremCaseData();
            caseData.setApplicantAccessCodes(List.of(entry("APP23456", YesOrNo.YES)));
            caseData.setRespondentAccessCodes(List.of(entry("RESP2345", YesOrNo.YES)));

            assertEquals(Optional.of("APP23456"), service.findAccessCodeForRecipient(caseData, APPLICANT));
            assertEquals(Optional.of("RESP2345"), service.findAccessCodeForRecipient(caseData, RESPONDENT));
        }

        @ParameterizedTest
        @EnumSource(value = DocumentHelper.PaperNotificationRecipient.class,
            names = {"APPLICANT", "RESPONDENT"})
        void shouldReturnEmptyWhenCollectionIsMissing(DocumentHelper.PaperNotificationRecipient recipient) {
            assertEquals(Optional.empty(),
                service.findAccessCodeForRecipient(new FinremCaseData(), recipient));
        }

        @Test
        void shouldReturnEmptyWhenCollectionIsEmpty() {
            FinremCaseData caseData = applicantWithCodes(List.of());

            assertEquals(Optional.empty(), service.findAccessCodeForRecipient(caseData, APPLICANT));
        }

        @ParameterizedTest
        @EnumSource(value = DocumentHelper.PaperNotificationRecipient.class,
            names = {"APPLICANT", "RESPONDENT"}, mode = EnumSource.Mode.EXCLUDE)
        void shouldReturnEmptyForUnsupportedRecipient(DocumentHelper.PaperNotificationRecipient recipient) {
            FinremCaseData caseData = applicantWithCodes(List.of(entry("APP23456", YesOrNo.YES)));

            assertEquals(Optional.empty(), service.findAccessCodeForRecipient(caseData, recipient));
        }

        @Test
        void shouldPreferUsedCodeOverEarlierValidCode() {
            AccessCodeCollection activated = AccessCodeCollection.builder()
                .value(AccessCodeEntry.builder()
                    .accessCode("USED2345")
                    .isValid(YesOrNo.NO)
                    .usedAt(LocalDateTime.of(2026, 10, 1, 12, 0))
                    .build())
                .build();

            FinremCaseData caseData = applicantWithCodes(List.of(
                entry("VALID234", YesOrNo.YES),
                activated
            ));

            assertEquals(Optional.of("USED2345"), service.findAccessCodeForRecipient(caseData, APPLICANT));
        }

        @Test
        void shouldRecogniseActivationFromIdamIdWithoutUsedAt() {
            AccessCodeCollection activated = AccessCodeCollection.builder()
                .value(AccessCodeEntry.builder()
                    .accessCode("LINK2345")
                    .isValid(YesOrNo.NO)
                    .userIdamID("user-123")
                    .build())
                .build();

            FinremCaseData caseData = applicantWithCodes(List.of(
                entry("VALID234", YesOrNo.YES),
                activated
            ));

            assertEquals(Optional.of("LINK2345"), service.findAccessCodeForRecipient(caseData, APPLICANT));
        }

        @Test
        void shouldReturnFirstActivatedCodeInCollectionOrder() {
            AccessCodeCollection first = AccessCodeCollection.builder()
                .value(AccessCodeEntry.builder()
                    .accessCode("FIRST234")
                    .usedAt(LocalDateTime.of(2026, 10, 1, 12, 0))
                    .build())
                .build();

            AccessCodeCollection second = AccessCodeCollection.builder()
                .value(AccessCodeEntry.builder()
                    .accessCode("NEXT2345")
                    .usedAt(LocalDateTime.of(2026, 10, 2, 12, 0))
                    .build())
                .build();

            FinremCaseData caseData = applicantWithCodes(List.of(first, second));

            assertEquals(Optional.of("FIRST234"), service.findAccessCodeForRecipient(caseData, APPLICANT));
        }

        @Test
        void shouldReturnFirstValidCodeWhenNoneIsActivated() {
            FinremCaseData caseData = applicantWithCodes(List.of(
                entry("INVALID2", YesOrNo.NO),
                entry("FIRST234", YesOrNo.YES),
                entry("NEXT2345", YesOrNo.YES)
            ));

            assertEquals(Optional.of("FIRST234"), service.findAccessCodeForRecipient(caseData, APPLICANT));
        }

        @Test
        void shouldIgnoreNullEntriesAndBlankCodesBeforeSelecting() {
            AccessCodeCollection blankActivated = AccessCodeCollection.builder()
                .value(AccessCodeEntry.builder()
                    .accessCode(" ")
                    .usedAt(LocalDateTime.of(2026, 10, 1, 12, 0))
                    .build())
                .build();

            FinremCaseData caseData = applicantWithCodes(Arrays.asList(
                null,
                AccessCodeCollection.builder().build(),
                entry(null, YesOrNo.YES),
                entry("", YesOrNo.YES),
                blankActivated,
                entry("VALID234", YesOrNo.YES)
            ));

            assertEquals(Optional.of("VALID234"), service.findAccessCodeForRecipient(caseData, APPLICANT));
        }

        @Test
        void shouldReturnEmptyWhenNoCodeIsValidOrActivated() {
            FinremCaseData caseData = applicantWithCodes(List.of(
                entry("INVALID2", YesOrNo.NO),
                entry("UNKNOWN2", null)
            ));

            assertEquals(Optional.empty(), service.findAccessCodeForRecipient(caseData, APPLICANT));
        }

        private FinremCaseData applicantWithCodes(List<AccessCodeCollection> codes) {
            FinremCaseData caseData = new FinremCaseData();
            caseData.setApplicantAccessCodes(codes);
            return caseData;
        }

        private AccessCodeCollection entry(String code, YesOrNo valid) {
            return AccessCodeCollection.builder()
                .value(AccessCodeEntry.builder()
                    .accessCode(code)
                    .isValid(valid)
                    .build())
                .build();
        }
    }
}
