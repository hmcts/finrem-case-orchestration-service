package uk.gov.hmcts.reform.finrem.caseorchestration.handler;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentHearingService;

import java.io.InputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class HearingConsentSubmittedHandlerTest {

    @InjectMocks
    private HearingConsentSubmittedHandler handler;
    @Mock
    private ConsentHearingService service;

    private ObjectMapper objectMapper;
    private static final String AUTH_TOKEN = "tokien:)";
    private static final String TEST_JSON = "/fixtures/consented.listOfHearing/list-for-hearing.json";

    @Before
    public void setup() {
        objectMapper = JsonMapper
            .builder()
            .addModule(new JavaTimeModule())
            .addModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES))
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();
    }

    @Test
    public void givenConsentedCase_whenRequestedListForHearing_thenHandlerCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONSENTED, EventType.LIST_FOR_HEARING_CONSENTED),
            is(true));
    }

    @Test
    public void givenConsentedCase_whenWrongEventType_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONSENTED, EventType.INTERIM_HEARING),
            is(false));
    }

    @Test
    public void givenConsentedCase_whenWrongCallbackType_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.LIST_FOR_HEARING_CONSENTED),
            is(false));
    }

    @Test
    public void givenConsentedCase_whenWrongCaseType_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.LIST_FOR_HEARING_CONSENTED),
            is(false));
    }

    @Test
    public void givenConsentedCase_WhenPartiesNeedToNotify_ThenItShouldSendNotification() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(callbackRequest, AUTH_TOKEN);

        assertNotNull(handle.getData());

        verify(service).sendNotification(any(FinremCaseDetails.class), any());
    }

    private FinremCallbackRequest buildCallbackRequest() {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(TEST_JSON)) {
            return objectMapper.readValue(resourceAsStream, FinremCallbackRequest.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}