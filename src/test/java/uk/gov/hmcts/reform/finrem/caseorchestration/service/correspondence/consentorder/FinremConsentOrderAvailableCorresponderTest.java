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
public class FinremConsentOrderAvailableCorresponderTest {

    FinremConsentOrderAvailableCorresponder consentOrderAvailableCorresponder;

    @Mock
    NotificationService notificationService;

    private FinremCaseDetails caseDetails;

    @Before
    public void setUp() throws Exception {
        consentOrderAvailableCorresponder = new FinremConsentOrderAvailableCorresponder(notificationService);
        caseDetails = FinremCaseDetails.builder().build();
    }

    @Test
    public void shouldEmailApplicantSolicitor() {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        consentOrderAvailableCorresponder.sendCorrespondence(caseDetails);
        verify(notificationService).sendConsentOrderAvailableEmailToApplicantSolicitor(caseDetails);
    }

    @Test
    public void shouldEmailRespondentSolicitor() {
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        consentOrderAvailableCorresponder.sendCorrespondence(caseDetails);
        verify(notificationService).sendConsentOrderAvailableEmailToRespondentSolicitor(caseDetails);
    }

}