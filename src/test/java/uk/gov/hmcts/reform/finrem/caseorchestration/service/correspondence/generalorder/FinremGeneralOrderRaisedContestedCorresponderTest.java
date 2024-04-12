package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.generalorder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.SolicitorCaseDataKeysWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinremGeneralOrderRaisedContestedCorresponderTest {
    @InjectMocks
    private FinremGeneralOrderRaisedContestedCorresponder corresponder;

    @Mock
    private NotificationService notificationService;

    private FinremCaseData caseData;
    private FinremCaseDetails caseDetails;

    @BeforeEach
    void setup() {
        caseData = FinremCaseData.builder().build();
        caseDetails = FinremCaseDetails.builder().data(caseData).build();
    }

    @Test
    void shouldNotSendGeneralOrderEmail() {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);

        corresponder.sendCorrespondence(caseDetails);

        verify(notificationService, never()).sendContestedGeneralOrderEmailRespondent(caseDetails);
    }

    @Test
    void shouldSendContestedGeneralOrderEmails() {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);

        corresponder.sendCorrespondence(caseDetails);

        verify(notificationService).sendContestedGeneralOrderEmailApplicant(caseDetails);
        verify(notificationService).sendContestedGeneralOrderEmailRespondent(caseDetails);
    }

    @Test
    void shouldNotSendContestedGeneralOrderEmailToRespondent_ThenTheEmailIsNotIssued() {
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);

        corresponder.sendCorrespondence(caseDetails);

        verify(notificationService, never()).sendContestedConsentGeneralOrderEmailRespondentSolicitor(caseDetails);
        verify(notificationService, never()).sendContestedGeneralOrderEmailRespondent(caseDetails);
    }

    @Test
    void shouldNotSendContestedGeneralOrderEmailToIntervener_ThenTheEmailIsNotIssued() {
        corresponder.sendCorrespondence(caseDetails);

        verify(notificationService, never()).sendContestedGeneralOrderEmailIntervener(any(FinremCaseDetails.class),
            any(SolicitorCaseDataKeysWrapper.class));
    }
}
