package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FinremConsentOrderMadeCorresponderTest {

    FinremConsentOrderMadeCorresponder consentOrderMadeCorresponder;

    @Mock
    NotificationService notificationService;

    private FinremCaseDetails caseDetails;

    @Before
    public void setUp() throws Exception {
        consentOrderMadeCorresponder = new FinremConsentOrderMadeCorresponder(notificationService);
        caseDetails = FinremCaseDetails.builder().build();
    }

    @Test
    public void shouldEmailApplicantSolicitor() {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        consentOrderMadeCorresponder.sendCorrespondence(caseDetails);
        verify(notificationService).sendConsentOrderMadeConfirmationEmailToApplicantSolicitor(caseDetails);
    }

    @Test
    public void shouldEmailRespondentSolicitor() {
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        consentOrderMadeCorresponder.sendCorrespondence(caseDetails);
        verify(notificationService).sendConsentOrderMadeConfirmationEmailToRespondentSolicitor(caseDetails);
    }
}