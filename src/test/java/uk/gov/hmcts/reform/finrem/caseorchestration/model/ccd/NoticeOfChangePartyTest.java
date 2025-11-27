package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NoticeOfChangeParty.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NoticeOfChangeParty.RESPONDENT;

class NoticeOfChangePartyTest {

    @Test
    void givenApplicantNocParty_whenInvoked_thenReturnTrue() {
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .contactDetailsWrapper(ContactDetailsWrapper.builder().nocParty(APPLICANT).build())
            .build();
        assertTrue(NoticeOfChangeParty.isApplicantForRepresentationChange(finremCaseData));
    }

    @Test
    void givenNonApplicantNocParty_whenInvoked_thenReturnFalse() {
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .contactDetailsWrapper(ContactDetailsWrapper.builder().nocParty(RESPONDENT).build())
            .build();
        assertFalse(NoticeOfChangeParty.isApplicantForRepresentationChange(finremCaseData));

        finremCaseData = FinremCaseData.builder()
            .contactDetailsWrapper(ContactDetailsWrapper.builder().nocParty(mock(NoticeOfChangeParty.class)).build())
            .build();
        assertFalse(NoticeOfChangeParty.isApplicantForRepresentationChange(finremCaseData));

        finremCaseData = FinremCaseData.builder()
            .contactDetailsWrapper(ContactDetailsWrapper.builder().nocParty(null).build())
            .build();
        assertFalse(NoticeOfChangeParty.isApplicantForRepresentationChange(finremCaseData));
    }

    @Test
    void givenRespondentNocParty_whenInvoked_thenReturnTrue() {
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .contactDetailsWrapper(ContactDetailsWrapper.builder().nocParty(RESPONDENT).build())
            .build();
        assertTrue(NoticeOfChangeParty.isRespondentForRepresentationChange(finremCaseData));
    }

    @Test
    void givenNonRespondentNocParty_whenInvoked_thenReturnFalse() {
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .contactDetailsWrapper(ContactDetailsWrapper.builder().nocParty(APPLICANT).build())
            .build();
        assertFalse(NoticeOfChangeParty.isRespondentForRepresentationChange(finremCaseData));

        finremCaseData = FinremCaseData.builder()
            .contactDetailsWrapper(ContactDetailsWrapper.builder().nocParty(mock()).build())
            .build();
        assertFalse(NoticeOfChangeParty.isRespondentForRepresentationChange(finremCaseData));

        finremCaseData = FinremCaseData.builder()
            .contactDetailsWrapper(ContactDetailsWrapper.builder().nocParty(null).build())
            .build();
        assertFalse(NoticeOfChangeParty.isRespondentForRepresentationChange(finremCaseData));
    }
}
