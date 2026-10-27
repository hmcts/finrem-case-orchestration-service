package uk.gov.hmcts.reform.finrem.caseorchestration.service.citizenui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;

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

        boolean generated = service.ensurePinForRecipient(caseDetails, APPLICANT);

        assertTrue(generated);
        assertEquals(1, data.getApplicantAccessCodes().size());
        assertNull(data.getRespondentAccessCodes());
    }

    @Test
    void shouldGeneratePinForContestedRespondent() {
        FinremCaseData data = new FinremCaseData();

        when(caseDetails.getCaseType()).thenReturn(CaseType.CONTESTED);
        when(caseDetails.getData()).thenReturn(data);

        boolean generated = service.ensurePinForRecipient(caseDetails, RESPONDENT);

        assertTrue(generated);
        assertEquals(1, data.getRespondentAccessCodes().size());
        assertNull(data.getApplicantAccessCodes());
    }

    @Test
    void shouldNotGeneratePinForConsentedCase() {
        when(caseDetails.getCaseType()).thenReturn(CaseType.CONSENTED);

        boolean generated = service.ensurePinForRecipient(caseDetails, APPLICANT);

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

        boolean generated = service.ensurePinForRecipient(caseDetails, APPLICANT);

        assertFalse(generated);
        assertNull(data.getApplicantAccessCodes());
    }
}
