package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.GeneralApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.GeneralApplicationStatus.APPROVED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_CREATED_BY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_OUTCOME_DECISION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_OUTCOME_LIST;

@RunWith(MockitoJUnitRunner.class)
public class GeneralApplicationOutcomeAboutToStartHandlerTest extends BaseHandlerTest {

    private GeneralApplicationOutcomeAboutToStartHandler handler;
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
        handler = new GeneralApplicationOutcomeAboutToStartHandler(helper);
    }

    @Test
    public void givenCase_whenCorrectConfigSupplied_thenHandlerCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.GENERAL_APPLICATION_OUTCOME),
            is(true));
    }

    @Test
    public void givenCase_whenInCorrectConfigCaseTypeSupplied_thenHandlerCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.GENERAL_APPLICATION_OUTCOME),
            is(false));
    }

    @Test
    public void givenCase_whenInCorrectConfigEventTypeSupplied_thenHandlerCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.CLOSE),
            is(false));
    }

    @Test
    public void givenCase_whenInCorrectConfigCallbackTypeSupplied_thenHandlerCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.GENERAL_APPLICATION_OUTCOME),
            is(false));
    }

    @Test
    public void givenContestedCase_whenNonCollectionGeneralApplicationExistAndAlreadyOutcomed_thenReturnError() {
        CallbackRequest callbackRequest =
            buildCallbackRequest(GA_NON_COLL_JSON);
        callbackRequest.getCaseDetails().getData().put(GENERAL_APPLICATION_OUTCOME_DECISION, APPROVED.getId());

        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> startHandle = handler.handle(callbackRequest, AUTH_TOKEN);
        assertTrue(startHandle.getErrors().contains("There are no general application available for decision."));
    }

    @Test
    public void givenCase_whenExistingGeneAppNonCollection_thenCreateSelectionList() {
        CallbackRequest callbackRequest = buildCallbackRequest(GA_NON_COLL_JSON);
        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> handle = handler.handle(callbackRequest, AUTH_TOKEN);

        Map<String, Object> caseData = handle.getData();
        DynamicList dynamicList = helper.objectToDynamicList(caseData.get(GENERAL_APPLICATION_OUTCOME_LIST));

        assertEquals(1, dynamicList.getListItems().size());
        assertNull(caseData.get(GENERAL_APPLICATION_OUTCOME_DECISION));
    }

    @Test
    public void givenCase_whenExistingGeneAppAsACollection_thenCreateSelectionList() {
        CallbackRequest callbackRequest = buildCallbackRequest(GA_JSON);
        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> handle = handler.handle(callbackRequest, AUTH_TOKEN);

        Map<String, Object> caseData = handle.getData();
        DynamicList dynamicList = helper.objectToDynamicList(caseData.get(GENERAL_APPLICATION_OUTCOME_LIST));

        assertEquals(2, dynamicList.getListItems().size());
        assertNull(caseData.get(GENERAL_APPLICATION_OUTCOME_DECISION));
    }

    @Test
    public void givenCase_whenNoExistingGeneAppAvailable_thenShowError() {
        CallbackRequest callbackRequest = buildCallbackRequest(GA_NON_COLL_JSON);

        List<GeneralApplicationCollectionData> existingList = helper.getGeneralApplicationList(callbackRequest.getCaseDetails().getData());
        List<GeneralApplicationCollectionData> updatedList
            = existingList.stream().map(this::updateStatus).collect(Collectors.toList());
        callbackRequest.getCaseDetails().getData().put(GENERAL_APPLICATION_COLLECTION, updatedList);
        callbackRequest.getCaseDetails().getData().remove(GENERAL_APPLICATION_CREATED_BY);

        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> handle = handler.handle(callbackRequest, AUTH_TOKEN);
        assertThat(handle.getErrors(), CoreMatchers.hasItem("There are no general application available for decision."));


    }

    private GeneralApplicationCollectionData updateStatus(GeneralApplicationCollectionData obj) {
        if (obj.getId().equals("b0bfb0af-4f07-4628-a677-1de904b6ea1c")) {
            obj.getGeneralApplicationItems().setGeneralApplicationStatus(APPROVED.getId());
        }
        return obj;
    }

}