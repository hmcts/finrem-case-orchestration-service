package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.GeneralApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.GeneralApplicationStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.GeneralApplicationStatus.APPROVED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_OUTCOME_DECISION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_OUTCOME_LIST;

@RunWith(MockitoJUnitRunner.class)
public class GeneralApplicationOutcomeAboutToSubmitHandlerTest extends BaseHandlerTestSetup {

    private GeneralApplicationOutcomeAboutToStartHandler startHandler;
    private GeneralApplicationOutcomeAboutToSubmitHandler submitHandler;
    @Mock
    private GenericDocumentService service;
    private GeneralApplicationHelper helper;
    private ObjectMapper objectMapper;

    public static final String AUTH_TOKEN = "tokien:)";
    private static final String GA_JSON = "/fixtures/contested/general-application-referred.json";
    private static final String GA_NON_COLL_JSON = "/fixtures/contested/general-application.json";

    @Before
    public void setup() {
        objectMapper = new ObjectMapper();
        helper = new GeneralApplicationHelper(objectMapper, service);
        startHandler = new GeneralApplicationOutcomeAboutToStartHandler(helper);
        submitHandler = new GeneralApplicationOutcomeAboutToSubmitHandler(helper);
    }

    @Test
    public void givenCase_whenCorrectConfigSupplied_thenHandlerCanHandle() {
        assertThat(submitHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.GENERAL_APPLICATION_OUTCOME),
            is(true));
    }

    @Test
    public void givenCase_whenInCorrectConfigCaseTypeSupplied_thenHandlerCanHandle() {
        assertThat(submitHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.GENERAL_APPLICATION_OUTCOME),
            is(false));
    }

    @Test
    public void givenCase_whenInCorrectConfigEventTypeSupplied_thenHandlerCanHandle() {
        assertThat(submitHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.CLOSE),
            is(false));
    }

    @Test
    public void givenCase_whenInCorrectConfigCallbackTypeSupplied_thenHandlerCanHandle() {
        assertThat(submitHandler
                .canHandle(CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.GENERAL_APPLICATION_OUTCOME),
            is(false));
    }

    //This senario should not come
    @Test
    public void givenCase_whenNonCollectionApproveAnApplication_thenMigratedAndUpdateStatusApproved() {
        CallbackRequest callbackRequest =
            buildCallbackRequest(GA_NON_COLL_JSON);
        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> startHandle = startHandler.handle(callbackRequest, AUTH_TOKEN);

        Map<String, Object> caseData = startHandle.getData();
        DynamicList dynamicList = helper.objectToDynamicList(caseData.get(GENERAL_APPLICATION_OUTCOME_LIST));
        assertEquals(1, dynamicList.getListItems().size());

        callbackRequest.getCaseDetails().getData().put(GENERAL_APPLICATION_OUTCOME_DECISION, APPROVED.getId());
        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> submitHandle = submitHandler.handle(callbackRequest, AUTH_TOKEN);

        Map<String, Object> data = submitHandle.getData();
        List<GeneralApplicationCollectionData> generalApplicationCollectionData
            = helper.covertToGeneralApplicationData(data.get(GENERAL_APPLICATION_COLLECTION));
        assertEquals(1, generalApplicationCollectionData.size());

        assertEquals(GeneralApplicationStatus.APPROVED.getId(),
            generalApplicationCollectionData.get(0).getGeneralApplicationItems().getGeneralApplicationStatus());
        assertNull(data.get(GENERAL_APPLICATION_OUTCOME_LIST));
        assertNull(data.get(GENERAL_APPLICATION_OUTCOME_DECISION));
    }

    @Test
    public void givenCase_whenApproveAnApplication_thenUpdateStatusApproved() {
        CallbackRequest callbackRequest = buildCallbackRequest(GA_JSON);

        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> startHandle = startHandler.handle(callbackRequest, AUTH_TOKEN);

        Map<String, Object> caseData = startHandle.getData();
        DynamicList dynamicList = helper.objectToDynamicList(caseData.get(GENERAL_APPLICATION_OUTCOME_LIST));
        assertEquals(2, dynamicList.getListItems().size());
        callbackRequest.getCaseDetails().getData().put(GENERAL_APPLICATION_OUTCOME_DECISION, "Approved");
        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> submitHandle = submitHandler.handle(callbackRequest, AUTH_TOKEN);

        Map<String, Object> data = submitHandle.getData();
        List<GeneralApplicationCollectionData> generalApplicationCollectionData
            = helper.covertToGeneralApplicationData(data.get(GENERAL_APPLICATION_COLLECTION));
        assertEquals(2, generalApplicationCollectionData.size());

        assertEquals(GeneralApplicationStatus.APPROVED.getId(),
            generalApplicationCollectionData.get(1).getGeneralApplicationItems().getGeneralApplicationStatus());
        assertNull(data.get(GENERAL_APPLICATION_OUTCOME_LIST));
        assertNull(data.get(GENERAL_APPLICATION_OUTCOME_DECISION));
    }

    @Test
    public void givenCase_whenNotApproveAnApplication_thenUpdateStatusNotApproved() {
        CallbackRequest callbackRequest = buildCallbackRequest(GA_JSON);

        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> startHandle = startHandler.handle(callbackRequest, AUTH_TOKEN);

        Map<String, Object> caseData = startHandle.getData();
        DynamicList dynamicList = helper.objectToDynamicList(caseData.get(GENERAL_APPLICATION_OUTCOME_LIST));
        assertEquals(2, dynamicList.getListItems().size());
        callbackRequest.getCaseDetails().getData().put(GENERAL_APPLICATION_OUTCOME_DECISION, "Not Approved");
        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> submitHandle = submitHandler.handle(callbackRequest, AUTH_TOKEN);

        Map<String, Object> data = submitHandle.getData();
        List<GeneralApplicationCollectionData> generalApplicationCollectionData
            = helper.covertToGeneralApplicationData(data.get(GENERAL_APPLICATION_COLLECTION));
        assertEquals(2, generalApplicationCollectionData.size());

        assertEquals(GeneralApplicationStatus.NOT_APPROVED.getId(),
            generalApplicationCollectionData.get(1).getGeneralApplicationItems().getGeneralApplicationStatus());
        assertNull(data.get(GENERAL_APPLICATION_OUTCOME_LIST));
    }

    @Test
    public void givenCase_whenOtherAnApplication_thenUpdateStatusOther() {
        CallbackRequest callbackRequest = buildCallbackRequest(GA_JSON);

        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> startHandle = startHandler.handle(callbackRequest, AUTH_TOKEN);

        Map<String, Object> caseData = startHandle.getData();
        DynamicList dynamicList = helper.objectToDynamicList(caseData.get(GENERAL_APPLICATION_OUTCOME_LIST));
        assertEquals(2, dynamicList.getListItems().size());
        callbackRequest.getCaseDetails().getData().put(GENERAL_APPLICATION_OUTCOME_DECISION, "Other");
        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> submitHandle = submitHandler.handle(callbackRequest, AUTH_TOKEN);

        Map<String, Object> data = submitHandle.getData();
        List<GeneralApplicationCollectionData> generalApplicationCollectionData
            = helper.covertToGeneralApplicationData(data.get(GENERAL_APPLICATION_COLLECTION));
        assertEquals(2, generalApplicationCollectionData.size());

        assertEquals(GeneralApplicationStatus.OTHER.getId(),
            generalApplicationCollectionData.get(1).getGeneralApplicationItems().getGeneralApplicationStatus());
        assertNull(data.get(GENERAL_APPLICATION_OUTCOME_LIST));
    }

    @Test
    public void givenCase_whenUnknowAnApplication_thenThrowException() {
        CallbackRequest callbackRequest = buildCallbackRequest(GA_JSON);

        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> startHandle = startHandler.handle(callbackRequest, AUTH_TOKEN);

        Map<String, Object> caseData = startHandle.getData();
        DynamicList dynamicList = helper.objectToDynamicList(caseData.get(GENERAL_APPLICATION_OUTCOME_LIST));
        assertEquals(2, dynamicList.getListItems().size());
        callbackRequest.getCaseDetails().getData().put(GENERAL_APPLICATION_OUTCOME_DECISION, "UNKNOWN");

        Exception exception = assertThrows(IllegalStateException.class, () -> submitHandler.handle(callbackRequest, AUTH_TOKEN));

        var expectedMessage = "Unexpected value: UNKNOWN";
        var actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);
    }
}