package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.updatefrc;

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
public class UpdateFrcEmailAllLitigantsCorresponderTest {

    UpdateFrcEmailAllLitigantsCorresponder updateFrcEmailAllLitigantsCorresponder;

    @Mock
    NotificationService notificationService;

    private CaseDetails caseDetails;

    @Before
    public void setUp() throws Exception {
        updateFrcEmailAllLitigantsCorresponder = new UpdateFrcEmailAllLitigantsCorresponder(notificationService);
        caseDetails = CaseDetails.builder().build();
    }

    @Test
    public void givenApplicantSolicitorIsDigitalshouldSendEmailApplicantSolicitor() {

        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        updateFrcEmailAllLitigantsCorresponder.sendEmails(caseDetails);
        verify(notificationService).isApplicantSolicitorDigitalAndEmailPopulated(caseDetails);
        verify(notificationService).isRespondentSolicitorDigitalAndEmailPopulated(caseDetails);
        verify(notificationService).sendUpdateFrcInformationEmailToAppSolicitor(caseDetails);
    }

    @Test
    public void givenRespondentSolicitorIsDigitalshouldSendEmailRespondentSolicitor() {

        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        updateFrcEmailAllLitigantsCorresponder.sendEmails(caseDetails);
        verify(notificationService).isRespondentSolicitorDigitalAndEmailPopulated(caseDetails);
        verify(notificationService).isApplicantSolicitorDigitalAndEmailPopulated(caseDetails);
        verify(notificationService).sendUpdateFrcInformationEmailToRespondentSolicitor(caseDetails);
    }
}