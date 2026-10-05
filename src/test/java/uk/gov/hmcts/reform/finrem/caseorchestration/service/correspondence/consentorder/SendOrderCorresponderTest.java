package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.notificationrequest.FinremNotificationRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFour;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOne;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThree;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.SolicitorCaseDataKeysWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.sendorder.SendOrderCorresponder;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTEST_ORDER_APPROVED_APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTEST_ORDER_APPROVED_INTERVENER1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTEST_ORDER_APPROVED_RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.INTERVENER_ONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.RESPONDENT;

@ExtendWith(MockitoExtension.class)
class SendOrderCorresponderTest {

    @InjectMocks
    private SendOrderCorresponder underTest;

    @Mock
    private NotificationService notificationService;

    @Mock
    private FinremNotificationRequestMapper finremNotificationRequestMapper;

    @Mock
    private FinremCaseDetails finremCaseDetails;

    @Spy
    private FinremCaseData caseData;

    @Spy
    private IntervenerOne intervenerOne;

    @Spy
    private IntervenerTwo intervenerTwo;

    @Spy
    private IntervenerThree intervenerThree;

    @Spy
    private IntervenerFour intervenerFour;

    @Mock
    private NotificationRequest applicantRequest;

    @Mock
    private NotificationRequest respondentRequest;

    @Mock
    private NotificationRequest intervenerRequest;

    @BeforeEach
    void setup() {
        when(finremCaseDetails.getData()).thenReturn(caseData);
    }

    @Test
    void shouldBuildMultipleCorrespondenceEvents() {
        Arrays.asList(intervenerOne, intervenerTwo, intervenerThree, intervenerFour)
            .forEach(intervenerWrapper -> when(intervenerWrapper.isPresent()).thenReturn(true));

        when(caseData.getIntervenerOne()).thenReturn(intervenerOne);
        when(caseData.getIntervenerTwo()).thenReturn(intervenerTwo);
        when(caseData.getIntervenerThree()).thenReturn(intervenerThree);
        when(caseData.getIntervenerFour()).thenReturn(intervenerFour);

        List<SendCorrespondenceEvent> events = underTest
            .buildCorrespondenceEventIfNeeded(finremCaseDetails, AUTH_TOKEN);
        assertThat(events).hasSize(6);
    }

    @Test
    void givenNoPresentInterveners_whenBuild_thenApplicantAndRespondentEventsOnly() {
        when(finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(finremCaseDetails))
            .thenReturn(applicantRequest);
        when(finremNotificationRequestMapper.getNotificationRequestForRespondentSolicitor(finremCaseDetails))
            .thenReturn(respondentRequest);

        List<SendCorrespondenceEvent> events =
            underTest.buildCorrespondenceEventIfNeeded(finremCaseDetails, AUTH_TOKEN);

        assertThat(events).hasSize(2);

        assertThat(events.get(0))
            .returns(List.of(APPLICANT), SendCorrespondenceEvent::getNotificationParties)
            .returns(applicantRequest, SendCorrespondenceEvent::getEmailNotificationRequest)
            .returns(FR_CONTEST_ORDER_APPROVED_APPLICANT, SendCorrespondenceEvent::getEmailTemplate)
            .returns(AUTH_TOKEN, SendCorrespondenceEvent::getAuthToken);

        assertThat(events.get(1))
            .returns(List.of(RESPONDENT), SendCorrespondenceEvent::getNotificationParties)
            .returns(respondentRequest, SendCorrespondenceEvent::getEmailNotificationRequest)
            .returns(FR_CONTEST_ORDER_APPROVED_RESPONDENT, SendCorrespondenceEvent::getEmailTemplate)
            .returns(AUTH_TOKEN, SendCorrespondenceEvent::getAuthToken);

        verifyNoInteractions(notificationService);
    }

    @Test
    void givenPresentIntervener_whenBuild_thenIntervenerEventAddedAfterRespondent() {
        when(intervenerOne.isPresent()).thenReturn(true);
        when(caseData.getIntervenerOne()).thenReturn(intervenerOne);

        SolicitorCaseDataKeysWrapper intervenerKey = mock(SolicitorCaseDataKeysWrapper.class);

        when(finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(finremCaseDetails))
            .thenReturn(applicantRequest);
        when(finremNotificationRequestMapper.getNotificationRequestForRespondentSolicitor(finremCaseDetails))
            .thenReturn(respondentRequest);
        when(notificationService.getCaseDataKeysForIntervenerSolicitor(intervenerOne))
            .thenReturn(intervenerKey);
        when(finremNotificationRequestMapper.getNotificationRequestForIntervenerSolicitor(
            finremCaseDetails, intervenerKey)).thenReturn(intervenerRequest);

        List<SendCorrespondenceEvent> events =
            underTest.buildCorrespondenceEventIfNeeded(finremCaseDetails, AUTH_TOKEN);

        assertThat(events).hasSize(3);
        assertThat(events)
            .extracting(SendCorrespondenceEvent::getNotificationParties)
            .containsExactly(
                List.of(APPLICANT),
                List.of(RESPONDENT),
                List.of(INTERVENER_ONE));

        assertThat(events.get(2))
            .returns(intervenerRequest, SendCorrespondenceEvent::getEmailNotificationRequest)
            .returns(FR_CONTEST_ORDER_APPROVED_INTERVENER1, SendCorrespondenceEvent::getEmailTemplate)
            .returns(AUTH_TOKEN, SendCorrespondenceEvent::getAuthToken);
    }

    @Test
    void givenIntervenerNotPresent_whenBuild_thenNoIntervenerEvent() {
        when(caseData.getIntervenerOne()).thenReturn(intervenerOne);

        List<SendCorrespondenceEvent> events =
            underTest.buildCorrespondenceEventIfNeeded(finremCaseDetails, AUTH_TOKEN);

        assertThat(events).hasSize(2);
        verify(notificationService, never()).getCaseDataKeysForIntervenerSolicitor(any());
    }
}
