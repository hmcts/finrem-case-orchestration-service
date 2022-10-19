package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.GeneralApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.GeneralApplicationStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_CREATED_BY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DOCUMENT_LATEST_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DRAFT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_HEARING_REQUIRED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_RECEIVED_FROM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_REFER_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_SPECIAL_MEASURES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_TIME_ESTIMATE;

@RunWith(MockitoJUnitRunner.class)
public class GeneralApplicationReferToJudgeAboutToSubmitHandlerTest extends BaseHandlerTest {

    private GeneralApplicationReferToJudgeAboutToStartHandler startHandler;
    private GeneralApplicationReferToJudgeAboutToSubmitHandler submitHandler;
    private GeneralApplicationHelper helper;
    private ObjectMapper objectMapper;

    public static final String AUTH_TOKEN = "tokien:)";
    private static final String NO_GA_JSON = "/fixtures/contested/no-general-application.json";
    private static final String GA_JSON = "/fixtures/contested/general-application-double.json";
    private static final String GA_NON_COLL_JSON = "/fixtures/contested/general-application.json";

    @Before
    public void setup() {
        objectMapper = new ObjectMapper();
        helper = new GeneralApplicationHelper(objectMapper);
        startHandler = new GeneralApplicationReferToJudgeAboutToStartHandler(helper);
        submitHandler = new GeneralApplicationReferToJudgeAboutToSubmitHandler(helper);
    }

    @Test
    public void givenCase_whenCorrectConfigSupplied_thenHandlerCanHandle() {
        assertThat(submitHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.GENERAL_APPLICATION_REFER_TO_JUDGE),
            is(true));
    }

    @Test
    public void givenCase_whenInCorrectConfigCaseTypeSupplied_thenHandlerCanHandle() {
        assertThat(submitHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.GENERAL_APPLICATION_REFER_TO_JUDGE),
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
                .canHandle(CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.GENERAL_APPLICATION_REFER_TO_JUDGE),
            is(false));
    }

    @Test
    public void givenCase_whenRejectingAnApplication_thenRemoveElementFromCollection() {
        CallbackRequest callbackRequest = buildCallbackRequest(GA_JSON);

        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> startHandle = startHandler.handle(callbackRequest, AUTH_TOKEN);

        Map<String, Object> caseData = startHandle.getData();
        DynamicList dynamicList = helper.objectToDynamicList(caseData.get(GENERAL_APPLICATION_REFER_LIST));
        assertEquals(2, dynamicList.getListItems().size());

        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> submitHandle = submitHandler.handle(callbackRequest, AUTH_TOKEN);

        Map<String, Object> data = submitHandle.getData();
        List<GeneralApplicationCollectionData> generalApplicationCollectionData
            = helper.covertToGeneralApplicationData(data.get(GENERAL_APPLICATION_COLLECTION));
        assertEquals(2, generalApplicationCollectionData.size());

        assertEquals(GeneralApplicationStatus.REFERRED.getId(),
            generalApplicationCollectionData.get(1).getGeneralApplicationItems().getGeneralApplicationStatus());
    }

    @Test
    public void givenContestedCase_whenNonCollectionGeneralApplicationExistAndAlreadyReferred_thenReturnError() {
        CallbackRequest callbackRequest = buildCallbackRequest(NO_GA_JSON);

        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> startHandle = startHandler.handle(callbackRequest, AUTH_TOKEN);

        Map<String, Object> caseData = startHandle.getData();
        DynamicList dynamicList = helper.objectToDynamicList(caseData.get(GENERAL_APPLICATION_REFER_LIST));
        assertNull(dynamicList);
        assertNull(caseData.get(GENERAL_APPLICATION_REFER_LIST));

        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> submitHandle = submitHandler.handle(callbackRequest, AUTH_TOKEN);
        assertThat(submitHandle.getErrors(), CoreMatchers.hasItem("There is no general application available to refer."));

    }

    @Test
    public void givenCase_whenRejectingAnExistinNonCollApplication_thenRemoveGeneralApplicationData() {
        CallbackRequest callbackRequest = buildCallbackRequest(GA_NON_COLL_JSON);

        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> startHandle = startHandler.handle(callbackRequest, AUTH_TOKEN);

        Map<String, Object> caseData = startHandle.getData();
        DynamicList dynamicList = helper.objectToDynamicList(caseData.get(GENERAL_APPLICATION_REFER_LIST));
        assertEquals(1, dynamicList.getListItems().size());

        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> submitHandle = submitHandler.handle(callbackRequest, AUTH_TOKEN);

        Map<String, Object> data = submitHandle.getData();
        assertExistingGeneralApplication(data);

        List<GeneralApplicationCollectionData> generalApplicationCollectionData
            = helper.covertToGeneralApplicationData(data.get(GENERAL_APPLICATION_COLLECTION));
        assertEquals(1, generalApplicationCollectionData.size());

        assertEquals(GeneralApplicationStatus.REFERRED.getId(),
            generalApplicationCollectionData.get(0).getGeneralApplicationItems().getGeneralApplicationStatus());
    }

    private void assertExistingGeneralApplication(Map<String, Object> caseData) {
        assertNull(caseData.get(GENERAL_APPLICATION_RECEIVED_FROM));
        assertNull(caseData.get(GENERAL_APPLICATION_CREATED_BY));
        assertNull(caseData.get(GENERAL_APPLICATION_HEARING_REQUIRED));
        assertNull(caseData.get(GENERAL_APPLICATION_TIME_ESTIMATE));
        assertNull(caseData.get(GENERAL_APPLICATION_SPECIAL_MEASURES));
        assertNull(caseData.get(GENERAL_APPLICATION_DOCUMENT));
        assertNull(caseData.get(GENERAL_APPLICATION_DRAFT_ORDER));
        assertNull(caseData.get(GENERAL_APPLICATION_DOCUMENT_LATEST_DATE));
    }

}