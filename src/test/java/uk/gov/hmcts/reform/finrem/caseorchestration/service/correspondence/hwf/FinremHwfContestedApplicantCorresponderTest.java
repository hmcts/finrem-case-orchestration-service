package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hwf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinremHwfContestedApplicantCorresponderTest {

    FinremHwfContestedApplicantCorresponder underTest;

    @Mock
    NotificationService notificationService;

    @Spy
    private FinremCaseDetails caseDetails;

    @BeforeEach
    void setUp() {
        underTest = new FinremHwfContestedApplicantCorresponder(notificationService);
    }

    @Test
    void shouldEmailApplicant() {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        underTest.sendCorrespondence(caseDetails);
        verify(notificationService).isApplicantSolicitorDigitalAndEmailPopulated(caseDetails);
        verify(notificationService).sendContestedHwfSuccessfulConfirmationEmail(caseDetails);
    }

    @Test
    void shouldNotEmailApplicant() {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        underTest.sendCorrespondence(caseDetails);
        verify(notificationService).isApplicantSolicitorDigitalAndEmailPopulated(caseDetails);
        verifyNoMoreInteractions(notificationService);
    }
}