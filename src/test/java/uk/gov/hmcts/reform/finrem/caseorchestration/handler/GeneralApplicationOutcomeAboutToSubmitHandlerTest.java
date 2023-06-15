package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.GeneralApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.GeneralApplicationStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.GeneralApplicationStatus.APPROVED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_OUTCOME_DECISION;

@RunWith(MockitoJUnitRunner.class)
public class GeneralApplicationOutcomeAboutToSubmitHandlerTest extends BaseHandlerTest {

    private GeneralApplicationOutcomeAboutToStartHandler startHandler;
    private GeneralApplicationOutcomeAboutToSubmitHandler submitHandler;
    @Mock
    private GenericDocumentService service;
    @Mock
    private GeneralApplicationService gaService;
    private GeneralApplicationHelper helper;
    private ObjectMapper objectMapper;
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    public static final String AUTH_TOKEN = "tokien:)";
    private static final String GA_JSON = "/fixtures/contested/general-application-referred.json";
    private static final String GA_NON_COLL_JSON = "/fixtures/contested/general-application.json";

    @Before
    public void setup() {
        objectMapper = new ObjectMapper();
        helper = new GeneralApplicationHelper(objectMapper, service);
        startHandler = new GeneralApplicationOutcomeAboutToStartHandler(finremCaseDetailsMapper, helper, gaService);
        submitHandler = new GeneralApplicationOutcomeAboutToSubmitHandler(finremCaseDetailsMapper, helper, gaService);
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
    @Ignore
    @Test
    public void givenCase_whenNonCollectionApproveAnApplication_thenMigratedAndUpdateStatusApproved() {
        CallbackRequest callbackRequest =
            buildCallbackRequest(GA_NON_COLL_JSON);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> startHandle = startHandler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData caseData = startHandle.getData();
        DynamicList dynamicList = helper.objectToDynamicList(caseData.getGeneralApplicationWrapper().getGeneralApplicationOutcomeList());
        assertEquals(1, dynamicList.getListItems().size());

        callbackRequest.getCaseDetails().getData().put(GENERAL_APPLICATION_OUTCOME_DECISION, APPROVED.getId());
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> submitHandle = submitHandler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData data = submitHandle.getData();
        List<GeneralApplicationCollectionData> generalApplicationCollectionData
            = helper.covertToGeneralApplicationData(data.getGeneralApplicationWrapper().getGeneralApplications());
        assertEquals(1, generalApplicationCollectionData.size());

        assertEquals(GeneralApplicationStatus.APPROVED.getId(),
            generalApplicationCollectionData.get(0).getGeneralApplicationItems().getGeneralApplicationStatus());
        assertNull(data.getGeneralApplicationWrapper().getGeneralApplicationOutcomeList());
        assertNull(data.getGeneralApplicationWrapper().getGeneralApplicationOutcome());
    }

    @Ignore
    @Test
    public void givenCase_whenApproveAnApplication_thenUpdateStatusApproved() {
        CallbackRequest callbackRequest = buildCallbackRequest(GA_JSON);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> startHandle = startHandler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData caseData = startHandle.getData();
        DynamicList dynamicList = helper.objectToDynamicList(caseData.getGeneralApplicationWrapper().getGeneralApplicationOutcomeList());
        assertEquals(2, dynamicList.getListItems().size());
        callbackRequest.getCaseDetails().getData().put(GENERAL_APPLICATION_OUTCOME_DECISION, "Approved");
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> submitHandle = submitHandler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData data = submitHandle.getData();
        List<GeneralApplicationCollectionData> generalApplicationCollectionData
            = helper.covertToGeneralApplicationData(data.getGeneralApplicationWrapper().getGeneralApplications());
        assertEquals(2, generalApplicationCollectionData.size());

        assertEquals(GeneralApplicationStatus.APPROVED.getId(),
            generalApplicationCollectionData.get(1).getGeneralApplicationItems().getGeneralApplicationStatus());
        assertNull(data.getGeneralApplicationWrapper().getGeneralApplicationOutcomeList());
        assertNull(data.getGeneralApplicationWrapper().getGeneralApplicationOutcome());
    }

    @Ignore
    @Test
    public void givenCase_whenNotApproveAnApplication_thenUpdateStatusNotApproved() {
        CallbackRequest callbackRequest = buildCallbackRequest(GA_JSON);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> startHandle = startHandler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData caseData = startHandle.getData();
        DynamicList dynamicList = helper.objectToDynamicList(caseData.getGeneralApplicationWrapper().getGeneralApplicationOutcomeList());
        assertEquals(2, dynamicList.getListItems().size());
        callbackRequest.getCaseDetails().getData().put(GENERAL_APPLICATION_OUTCOME_DECISION, "Not Approved");
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> submitHandle = submitHandler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData data = submitHandle.getData();
        List<GeneralApplicationCollectionData> generalApplicationCollectionData
            = helper.covertToGeneralApplicationData(data.getGeneralApplicationWrapper().getGeneralApplications());
        assertEquals(2, generalApplicationCollectionData.size());

        assertEquals(GeneralApplicationStatus.NOT_APPROVED.getId(),
            generalApplicationCollectionData.get(1).getGeneralApplicationItems().getGeneralApplicationStatus());
        assertNull(data.getGeneralApplicationWrapper().getGeneralApplicationOutcomeList());
    }

    @Ignore
    @Test
    public void givenCase_whenOtherAnApplication_thenUpdateStatusOther() {
        CallbackRequest callbackRequest = buildCallbackRequest(GA_JSON);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> startHandle = startHandler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData caseData = startHandle.getData();
        DynamicList dynamicList = helper.objectToDynamicList(caseData.getGeneralApplicationWrapper().getGeneralApplicationOutcomeList());
        assertEquals(2, dynamicList.getListItems().size());
        callbackRequest.getCaseDetails().getData().put(GENERAL_APPLICATION_OUTCOME_DECISION, "Other");
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> submitHandle = submitHandler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData data = submitHandle.getData();
        List<GeneralApplicationCollectionData> generalApplicationCollectionData
            = helper.covertToGeneralApplicationData(data.getGeneralApplicationWrapper().getGeneralApplications());
        assertEquals(2, generalApplicationCollectionData.size());

        assertEquals(GeneralApplicationStatus.OTHER.getId(),
            generalApplicationCollectionData.get(1).getGeneralApplicationItems().getGeneralApplicationStatus());
        assertNull(data.getGeneralApplicationWrapper().getGeneralApplicationOutcomeList());
    }

    @Ignore
    @Test
    public void givenCase_whenUnknowAnApplication_thenThrowException() {
        CallbackRequest callbackRequest = buildCallbackRequest(GA_JSON);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> startHandle = startHandler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData caseData = startHandle.getData();
        DynamicList dynamicList = helper.objectToDynamicList(caseData.getGeneralApplicationWrapper().getGeneralApplicationOutcomeList());
        assertEquals(2, dynamicList.getListItems().size());
        callbackRequest.getCaseDetails().getData().put(GENERAL_APPLICATION_OUTCOME_DECISION, "UNKNOWN");

        Exception exception = assertThrows(IllegalStateException.class, () -> submitHandler.handle(callbackRequest, AUTH_TOKEN));

        var expectedMessage = "Unexpected value: UNKNOWN";
        var actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);
    }
}