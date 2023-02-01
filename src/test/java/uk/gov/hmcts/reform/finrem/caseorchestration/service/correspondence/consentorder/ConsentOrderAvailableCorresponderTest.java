package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConsentOrderAvailableCorresponderTest {

    ConsentOrderAvailableCorresponder consentOrderAvailableCorresponder;

    @Mock
    NotificationService notificationService;

    private CaseDetails caseDetails;

    @Before
    public void setUp() throws Exception {
        consentOrderAvailableCorresponder = new ConsentOrderAvailableCorresponder(notificationService);
        caseDetails = CaseDetails.builder().build();
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