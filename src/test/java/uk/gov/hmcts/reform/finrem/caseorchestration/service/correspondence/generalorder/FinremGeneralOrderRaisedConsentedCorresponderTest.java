package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.generalorder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinremGeneralOrderRaisedConsentedCorresponderTest {

    @InjectMocks
    private FinremGeneralOrderRaisedConsentedCorresponder corresponder;

    @Mock
    private NotificationService notificationService;

    @Mock
    private CaseDataService caseDataService;

    private FinremCaseDetails caseDetails;
    private FinremCallbackRequest callbackRequest;

    @BeforeEach
    void setup() {
        caseDetails = FinremCaseDetails.builder().build();
        callbackRequest = FinremCallbackRequest.builder().build();
    }

    @Test
    void shouldEmailAllSolicitorsWhenConsentedAndBothDigital() {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);

        corresponder.sendCorrespondence(caseDetails);

        verify(notificationService).sendConsentedGeneralOrderEmailToApplicantSolicitor(caseDetails);
        verify(notificationService).sendConsentedGeneralOrderEmailToRespondentSolicitor(caseDetails);
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldNotSendGeneralOrderEmail() {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);

        corresponder.sendCorrespondence(caseDetails);

        verify(notificationService, never()).sendConsentedGeneralOrderEmailToRespondentSolicitor(caseDetails);
    }
}
