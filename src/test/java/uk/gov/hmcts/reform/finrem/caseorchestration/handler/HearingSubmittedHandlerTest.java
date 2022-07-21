package uk.gov.hmcts.reform.finrem.caseorchestration.handler;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AdditionalHearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.io.InputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HearingSubmittedHandlerTest {

    @InjectMocks
    private HearingSubmittedHandler handler;
    @Mock
    private HearingDocumentService hearingDocumentService;
    @Mock
    private AdditionalHearingDocumentService additionalHearingDocumentService;
    @Mock
    private CaseDataService caseDataService;
    @Mock
    private NotificationService notificationService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String AUTH_TOKEN = "tokien:)";
    private static final String TEST_JSON = "/fixtures/contested/interim-hearing-two-collection-formA.json";

    @Test
    public void canHandle() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.LIST_FOR_HEARING),
            is(true));
    }

    @Test
    public void canNotHandleWrongCaseType() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONSENTED, EventType.LIST_FOR_HEARING),
            is(false));
    }

    @Test
    public void canNotHandleWrongEventType() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.CLOSE),
            is(false));
    }

    @Test
    public void canNotHandleWrongCallbackType() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.LIST_FOR_HEARING),
            is(false));
    }

    @Test
    public void givenSolAgreedToEmails_and_noPreviousHearing_shouldSendPrepareForHearingEmail_and_PrintHearingDocuments() {
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(true);
        when(caseDataService.isContestedPaperApplication(any())).thenReturn(true);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(true);
        when(hearingDocumentService.alreadyHadFirstHearing(any())).thenReturn(false);

        handler.handle(buildCallbackRequest(),AUTH_TOKEN);

        verify(notificationService).sendPrepareForHearingEmailApplicant(any());
        verify(notificationService).sendPrepareForHearingEmailRespondent(any());
        verify(hearingDocumentService).sendFormCAndGForBulkPrint(any(), eq(AUTH_TOKEN));
    }

    @Test
    public void shouldNotSendPrepareForHearingEmailWhenNotAgreed() {
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(false);

        handler.handle(buildCallbackRequest(),AUTH_TOKEN);

        verify(notificationService, never()).sendPrepareForHearingEmailApplicant(any());
        verify(notificationService, never()).sendPrepareForHearingEmailRespondent(any());
    }

    @Test
    public void givenHadPreviousHearing_whenNotifyHearingInvoked_thenPrintAdditionalHearingDocuments() {
        when(caseDataService.isContestedPaperApplication(any())).thenReturn(true);
        when(hearingDocumentService.alreadyHadFirstHearing(any())).thenReturn(true);

        handler.handle(buildCallbackRequest(),AUTH_TOKEN);

        verify(additionalHearingDocumentService).sendAdditionalHearingDocuments(eq(AUTH_TOKEN), any());
    }

    private CallbackRequest buildCallbackRequest()  {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(TEST_JSON)) {
            return objectMapper.readValue(resourceAsStream, CallbackRequest.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}