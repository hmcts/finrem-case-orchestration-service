package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.generalorder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.SolicitorCaseDataKeysWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinremGeneralOrderRaisedCorresponderTest {

    @InjectMocks
    private FinremGeneralOrderRaisedCorresponder corresponder;

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
        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(true);
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);

        corresponder.sendCorrespondence(caseDetails);

        verify(notificationService).sendConsentedGeneralOrderEmailToApplicantSolicitor(caseDetails);
        verify(notificationService).sendConsentedGeneralOrderEmailToRespondentSolicitor(caseDetails);
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldSendContestedConsentGeneralOrderEmailToRespondentInConsentedInContestedCase() {
        callbackRequest.setEventType(EventType.GENERAL_ORDER_CONSENT_IN_CONTESTED);
        EventType eventId = callbackRequest.getEventType();
        when(caseDataService.isConsentInContestedGeneralOrderEvent(eventId)).thenReturn(true);
        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(false);
        when(caseDataService.isConsentedInContestedCase(caseDetails)).thenReturn(true);
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);

        corresponder.sendCorrespondence(caseDetails, eventId);

        verify(notificationService).sendContestedConsentGeneralOrderEmailRespondentSolicitor(caseDetails);
        verify(notificationService, never()).sendContestedGeneralOrderEmailRespondent(caseDetails);
        verify(notificationService, never()).sendConsentedGeneralOrderEmailToRespondentSolicitor(caseDetails);
    }

    @Test
    void shouldNotSendGeneralOrderEmail() {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);

        corresponder.sendCorrespondence(caseDetails);

        verify(notificationService, never()).sendConsentedGeneralOrderEmailToRespondentSolicitor(caseDetails);
        verify(notificationService, never()).sendContestedConsentGeneralOrderEmailRespondentSolicitor(caseDetails);
        verify(notificationService, never()).sendContestedGeneralOrderEmailRespondent(caseDetails);
    }

    @Test
    void shouldSendContestedConsentGeneralOrderEmail() {
        callbackRequest.setEventType(EventType.GENERAL_ORDER_CONSENT_IN_CONTESTED);
        EventType eventId = callbackRequest.getEventType();
        when(caseDataService.isConsentInContestedGeneralOrderEvent(eventId)).thenReturn(true);
        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(false);
        when(caseDataService.isConsentedInContestedCase(caseDetails)).thenReturn(true);
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);

        corresponder.sendCorrespondence(caseDetails, eventId);

        verify(notificationService).sendContestedConsentGeneralOrderEmailApplicantSolicitor(caseDetails);
    }

    @Test
    void shouldSendContestedGeneralOrderEmails() {
        callbackRequest.setEventType(EventType.GENERAL_ORDER);
        EventType eventId = callbackRequest.getEventType();
        when(caseDataService.isConsentInContestedGeneralOrderEvent(eventId)).thenReturn(false);
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);

        corresponder.sendCorrespondence(caseDetails, eventId);

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
