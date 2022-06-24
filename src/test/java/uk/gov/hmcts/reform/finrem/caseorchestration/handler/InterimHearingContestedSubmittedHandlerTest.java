package uk.gov.hmcts.reform.finrem.caseorchestration.handler;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InterimHearingService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.io.InputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class InterimHearingContestedSubmittedHandlerTest {

    @InjectMocks
    private InterimHearingContestedSubmittedHandler interimHearingContestedSubmittedHandler;
    @Mock
    private InterimHearingService interimHearingService;
    @Mock
    private CaseDataService caseDataService;
    @Mock
    private NotificationService notificationService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String AUTH_TOKEN = "tokien:)";
    private static final String TEST_PAPER_JSON = "/fixtures/contested/interim-hearing-two-collection.json";
    private static final String TEST_JSON = "/fixtures/contested/interim-hearing-two-collection-formA.json";

    @Test
    public void canHandle() {
        assertThat(interimHearingContestedSubmittedHandler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.INTERIM_HEARING),
            is(true));
    }

    @Test
    public void canNotHandle() {
        assertThat(interimHearingContestedSubmittedHandler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONSENTED, EventType.INTERIM_HEARING),
            is(false));
    }

    @Test
    public void canNotHandleWrongEventType() {
        assertThat(interimHearingContestedSubmittedHandler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.CLOSE),
            is(false));
    }

    @Test
    public void canNotHandleWrongCallbackType() {
        assertThat(interimHearingContestedSubmittedHandler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.INTERIM_HEARING),
            is(false));
    }

    @Test
    public void handleWhenPaperApplication() {
        CallbackRequest callbackRequest = buildCallbackRequest(TEST_PAPER_JSON);

        AboutToStartOrSubmitCallbackResponse handle =
            interimHearingContestedSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        assertNotNull(handle.getData());

        verify(interimHearingService).filterInterimHearingToProcess(any());
        verify(interimHearingService).convertInterimHearingCollectionDataToMap(any());

        verify(caseDataService).isPaperApplication(any());
        verify(caseDataService, never()).isApplicantSolicitorAgreeToReceiveEmails(any());

        verify(notificationService, never()).shouldEmailRespondentSolicitor(any());
        verify(notificationService, never()).sendInterimHearingNotificationEmailToApplicantSolicitor(any(), anyMap());
        verify(notificationService, never()).sendInterimHearingNotificationEmailToRespondentSolicitor(any(), anyMap());
    }

    @Test
    public void handleNonPaperApplication() {
        CallbackRequest callbackRequest = buildCallbackRequest(TEST_JSON);
        AboutToStartOrSubmitCallbackResponse handle =
            interimHearingContestedSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        assertNotNull(handle.getData());

        verify(interimHearingService).filterInterimHearingToProcess(any());
        verify(interimHearingService).convertInterimHearingCollectionDataToMap(any());

        verify(caseDataService, times(1)).isPaperApplication(any());
    }

    private CallbackRequest buildCallbackRequest(String path)  {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(path)) {
            return objectMapper.readValue(resourceAsStream, CallbackRequest.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}