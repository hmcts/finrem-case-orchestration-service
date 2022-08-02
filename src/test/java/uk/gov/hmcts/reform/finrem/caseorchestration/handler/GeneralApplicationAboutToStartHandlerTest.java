package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.GeneralApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_COLLECTION;

@RunWith(MockitoJUnitRunner.class)
public class GeneralApplicationAboutToStartHandlerTest {

    private GeneralApplicationAboutToStartHandler handler;
    @Mock
    private IdamService idamService;
    private ObjectMapper objectMapper;
    private GeneralApplicationHelper helper;

    public static final String AUTH_TOKEN = "tokien:)";
    private static final String GA_JSON = "/fixtures/contested/general-application.json";


    @Before
    public void setup() {
        objectMapper = new ObjectMapper();
        helper = new GeneralApplicationHelper(objectMapper);
        handler  = new GeneralApplicationAboutToStartHandler(idamService, helper);
    }

    @Test
    public void canHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.GENERAL_APPLICATION),
            is(true));
    }

    @Test
    public void canNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.GENERAL_APPLICATION),
            is(false));
    }

    @Test
    public void canNotHandleWrongEventType() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.CLOSE),
            is(false));
    }

    @Test
    public void canNotHandleWrongCallbackType() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.GENERAL_APPLICATION),
            is(false));
    }

    @Test
    public void givenCase_whenExistingGeneApp_thenSetcreatedBy() {
        CallbackRequest callbackRequest = buildCallbackRequest(GA_JSON);
        when(idamService.getIdamFullName(AUTH_TOKEN)).thenReturn("Test User");

        AboutToStartOrSubmitCallbackResponse handle = handler.handle(callbackRequest, AUTH_TOKEN);

        Map<String, Object> caseData = handle.getData();
        List<GeneralApplicationCollectionData> existingGeneralApplications = helper.getExistingGeneralApplications(caseData);
        GeneralApplicationCollectionData generalApplicationCollectionData = existingGeneralApplications.get(existingGeneralApplications.size() - 1);

        assertEquals("Test User", generalApplicationCollectionData.getGeneralApplicationItems().getGeneralApplicationCreatedBy());
    }

    @Test
    public void givenCase_whenNoExistingGeneApp_thenSetcreatedBy() {
        CallbackRequest callbackRequest = buildCallbackRequest(GA_JSON);
        callbackRequest.getCaseDetails().getData().remove(GENERAL_APPLICATION_COLLECTION);
        when(idamService.getIdamFullName(AUTH_TOKEN)).thenReturn("Test User");

        AboutToStartOrSubmitCallbackResponse handle = handler.handle(callbackRequest, AUTH_TOKEN);
        Map<String, Object> caseData = handle.getData();
        List<GeneralApplicationCollectionData> existingGeneralApplications = helper.getExistingGeneralApplications(caseData);
        GeneralApplicationCollectionData generalApplicationCollectionData = existingGeneralApplications.get(existingGeneralApplications.size() - 1);

        assertEquals("Test User", generalApplicationCollectionData.getGeneralApplicationItems().getGeneralApplicationCreatedBy());
    }

    private CallbackRequest buildCallbackRequest(final String path)  {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(path)) {
            CaseDetails caseDetails = objectMapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
            return CallbackRequest.builder().caseDetails(caseDetails).build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}