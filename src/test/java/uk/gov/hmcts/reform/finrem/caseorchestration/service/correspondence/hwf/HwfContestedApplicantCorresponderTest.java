package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hwf;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HwfContestedApplicantCorresponderTest {

    HwfContestedApplicantCorresponder hwfContestedApplicantCorresponder;

    @Mock
    NotificationService notificationService;

    @Before
    public void setUp() throws Exception {
        hwfContestedApplicantCorresponder = new HwfContestedApplicantCorresponder(notificationService);
    }

    @Test
    public void shouldEmailApplicant() {
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().build();
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        hwfContestedApplicantCorresponder.sendCorrespondence(caseDetails);
        verify(notificationService).isApplicantSolicitorDigitalAndEmailPopulated(caseDetails);
        verify(notificationService).sendContestedHwfSuccessfulConfirmationEmail(caseDetails);
    }

    @Test
    public void shouldNotEmailApplicant() {
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().build();
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        hwfContestedApplicantCorresponder.sendCorrespondence(caseDetails);
        verify(notificationService).isApplicantSolicitorDigitalAndEmailPopulated(caseDetails);
        verifyNoMoreInteractions(notificationService);
    }
}