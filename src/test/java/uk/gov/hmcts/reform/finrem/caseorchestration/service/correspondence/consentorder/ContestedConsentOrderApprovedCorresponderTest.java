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
public class ContestedConsentOrderApprovedCorresponderTest {

    ContestedConsentOrderApprovedCorresponder contestedConsentOrderApprovedCorresponder;

    @Mock
    NotificationService notificationService;

    private CaseDetails caseDetails;

    @Before
    public void setUp() throws Exception {
        contestedConsentOrderApprovedCorresponder = new ContestedConsentOrderApprovedCorresponder(notificationService);
        caseDetails = CaseDetails.builder().build();
    }

    @Test
    public void shouldEmailApplicantSolicitor() {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        contestedConsentOrderApprovedCorresponder.sendCorrespondence(caseDetails);
        verify(notificationService).sendContestedConsentOrderApprovedEmailToApplicantSolicitor(caseDetails);
    }

    @Test
    public void shouldEmailRespondentSolicitor() {
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        contestedConsentOrderApprovedCorresponder.sendCorrespondence(caseDetails);
        verify(notificationService).sendContestedConsentOrderApprovedEmailToRespondentSolicitor(caseDetails);
    }
}