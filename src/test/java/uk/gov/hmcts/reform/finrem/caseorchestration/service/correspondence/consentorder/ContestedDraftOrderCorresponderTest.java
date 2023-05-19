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
public class ContestedDraftOrderCorresponderTest {

    ContestedDraftOrderCorresponder contestedDraftOrderCorresponder;

    @Mock
    NotificationService notificationService;

    private CaseDetails caseDetails;

    @Before
    public void setUp() throws Exception {
        contestedDraftOrderCorresponder = new ContestedDraftOrderCorresponder(notificationService);
        caseDetails = CaseDetails.builder().build();
    }

    @Test
    public void shouldEmailApplicantSolicitor() {
        when(notificationService.isApplicantSolicitorResponsibleToDraftOrder(caseDetails.getData())).thenReturn(true);
        when(notificationService.isApplicantSolicitorAgreeToReceiveEmails(caseDetails)).thenReturn(true);
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        contestedDraftOrderCorresponder.sendCorrespondence(caseDetails);
        verify(notificationService).sendSolicitorToDraftOrderEmailApplicant(caseDetails);
    }

    @Test
    public void shouldEmailRespondentSolicitor() {
        when(notificationService.isRespondentSolicitorResponsibleToDraftOrder(caseDetails.getData())).thenReturn(true);
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(caseDetails.getData())).thenReturn(true);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        contestedDraftOrderCorresponder.sendCorrespondence(caseDetails);
        verify(notificationService).sendSolicitorToDraftOrderEmailRespondent(caseDetails);
    }
}