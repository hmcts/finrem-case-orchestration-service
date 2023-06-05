package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.SolicitorCaseDataKeysWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_EMAIL;

@RunWith(MockitoJUnitRunner.class)
public class ContestedSendOrderCorresponderTest {

    ContestedSendOrderCorresponder contestedSendOrderCorresponder;

    @Mock
    NotificationService notificationService;
    @Mock
    FinremCaseDetailsMapper finremCaseDetailsMapper;

    private CaseDetails caseDetails;

    @Before
    public void setUp() throws Exception {
        contestedSendOrderCorresponder = new ContestedSendOrderCorresponder(notificationService, finremCaseDetailsMapper);
        caseDetails = CaseDetails.builder().data(new HashMap<>()).build();
    }

    @Test
    public void shouldEmailApplicantSolicitor() {
        when(notificationService.isApplicantSolicitorAgreeToReceiveEmails(caseDetails)).thenReturn(true);
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        contestedSendOrderCorresponder.sendCorrespondence(caseDetails);
        verify(notificationService).sendContestOrderApprovedEmailApplicant(caseDetails);
    }

    @Test
    public void shouldEmailRespondentSolicitor() {
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(caseDetails.getData())).thenReturn(true);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        contestedSendOrderCorresponder.sendCorrespondence(caseDetails);
        verify(notificationService).sendContestOrderApprovedEmailRespondent(caseDetails);
    }

    @Test
    public void shouldEmailIntervenerSolicitor() {
        String intervenerEmailKey = "intervener1SolEmail";
        caseDetails.getData().put(intervenerEmailKey, TEST_SOLICITOR_EMAIL);
        SolicitorCaseDataKeysWrapper dataKeysWrapper = SolicitorCaseDataKeysWrapper.builder().build();
        when(notificationService.getCaseDataKeysForIntervenerSolicitor(any(IntervenerWrapper.class))).thenReturn(dataKeysWrapper);
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(IntervenerOneWrapper.builder().build(),
            caseDetails)).thenReturn(true);
        contestedSendOrderCorresponder.sendCorrespondence(caseDetails);
        verify(notificationService).sendContestOrderApprovedEmailIntervener(caseDetails,
            dataKeysWrapper);
    }
}