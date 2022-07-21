package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService;

import java.io.InputStream;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;

public class HearingAboutToStartHandlerTest {

    private HearingAboutToStartHandler handler;

    private final ObjectMapper objectMapper = new ObjectMapper();
    public static final String AUTH_TOKEN = "tokien:)";

    private static final String INVALID_HEARING_JSON = "/fixtures/contested/invalid-hearing.json";

    @Before
    public void setup() {
        handler = new HearingAboutToStartHandler(new ValidateHearingService());
    }

    @Test
    public void givenCase_whenValid_thenHandlerCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.LIST_FOR_HEARING),
            is(true));
    }

    @Test
    public void givenCase_whenInValidCaseType_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.LIST_FOR_HEARING),
            is(false));
    }

    @Test
    public void givenCase_whenInValidEventType_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.CLOSE),
            is(false));
    }

    @Test
    public void givenCase_whenInValidCallbackType_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.INTERIM_HEARING),
            is(false));
    }

    @Test
    public void givenCase_whenMandatoryFieldNotPresent_thenIssueError() {
        CallbackRequest callbackRequest = buildCallbackRequest();

        AboutToStartOrSubmitCallbackResponse response = handler.handle(callbackRequest, AUTH_TOKEN);
        assertThat(response.getErrors(), contains("Issue Date, fast track decision or hearingDate is empty"));
    }

    @Test
    public void givenCase_whenMandatoryFieldPresent_thenHandleRequest() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        Map<String, Object> caseData = callbackRequest.getCaseDetails().getData();
        caseData.put("issueDate","2019-03-04");
        caseData.put("hearingDate","2019-03-04");
        caseData.put("fastTrackDecision","yes");

        AboutToStartOrSubmitCallbackResponse response = handler.handle(callbackRequest, AUTH_TOKEN);
        assertTrue(response.getErrors().isEmpty());
    }

    private CallbackRequest buildCallbackRequest()  {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(INVALID_HEARING_JSON)) {
            CaseDetails caseDetails = objectMapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
            return CallbackRequest.builder().caseDetails(caseDetails).build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}