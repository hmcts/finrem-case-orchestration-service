package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.GeneralApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@RunWith(MockitoJUnitRunner.class)
public class GeneralApplicationMidHandlerTest extends BaseHandlerTest {

    private GeneralApplicationMidHandler handler;
    @Mock
    private GenericDocumentService service;
    private ObjectMapper objectMapper;
    private GeneralApplicationHelper helper;

    public static final String AUTH_TOKEN = "tokien:)";
    private static final String GA_JSON = "/fixtures/contested/general-application-double.json";


    @Before
    public void setup() {
        objectMapper = new ObjectMapper();
        helper = new GeneralApplicationHelper(objectMapper, service);
        handler = new GeneralApplicationMidHandler(helper);
    }

    @Test
    public void canHandle() {
        assertThat(handler
                .canHandle(CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.GENERAL_APPLICATION),
            is(true));
    }

    @Test
    public void canNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.MID_EVENT, CaseType.CONSENTED, EventType.GENERAL_APPLICATION),
            is(false));
    }

    @Test
    public void canNotHandleWrongEventType() {
        assertThat(handler
                .canHandle(CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.CLOSE),
            is(false));
    }

    @Test
    public void canNotHandleWrongCallbackType() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.GENERAL_APPLICATION),
            is(false));
    }

    @Test
    public void givenContestedCase_whenGeneralApplicationEventStartButNotAddedDetails_thenThrowErrorMessage() {
        CallbackRequest callbackRequest = getEmptyCallbackRequest();
        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> response = handler.handle(callbackRequest, AUTH_TOKEN);
        assertTrue(response.getErrors().get(0)
            .contains("Please complete the General Application. No information has been entered for this application."));
    }

    @Test
    public void givenContestedCase_whenGeneralApplicationEventStartAndThereIsExistingApplicationButNotAddedNewApplication_thenThrowErrorMessage() {
        CallbackRequest callbackRequest = buildCallbackRequestWithCaseDetailsBefore(GA_JSON);
        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> response = handler.handle(callbackRequest, AUTH_TOKEN);

        assertTrue(response.getErrors().get(0)
            .contains("Any changes to an existing General Applications will not be saved."
                + " Please add a new General Application in order to progress."));
    }


    private CallbackRequest getEmptyCallbackRequest() {
        return CallbackRequest
            .builder()
            .caseDetailsBefore(CaseDetails.builder().caseTypeId(CONTESTED.getCcdType()).data(new HashMap<>()).build())
            .caseDetails(CaseDetails.builder().caseTypeId(CONTESTED.getCcdType()).data(new HashMap<>()).build())
            .build();

    }

    private CallbackRequest buildCallbackRequestWithCaseDetailsBefore(String testJson) {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(testJson)) {
            CaseDetails caseDetails =
                objectMapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
            return CallbackRequest.builder().caseDetails(caseDetails).caseDetailsBefore(caseDetails).build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}