package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.GeneralApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
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

@RunWith(MockitoJUnitRunner.class)
public class RejectGeneralApplicationAboutToSubmitHandlerTest extends BaseHandlerTestSetup {

    private RejectGeneralApplicationAboutToStartHandler startHandler;
    private RejectGeneralApplicationAboutToSubmitHandler submitHandler;
    @Mock
    private GenericDocumentService service;
    @Mock
    private GeneralApplicationService generalApplicationService;
    private GeneralApplicationHelper helper;
    private ObjectMapper objectMapper;
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    public static final String AUTH_TOKEN = "tokien:)";
    private static final String NO_GA_JSON = "/fixtures/contested/no-general-application-finrem.json";
    private static final String GA_JSON = "/fixtures/contested/general-application-details.json";
    private static final String GA_NON_COLL_JSON = "/fixtures/contested/general-application.json";

    @Before
    public void setup() {
        objectMapper = new ObjectMapper();
        helper = new GeneralApplicationHelper(objectMapper, service);
        startHandler = new RejectGeneralApplicationAboutToStartHandler(finremCaseDetailsMapper, helper, generalApplicationService);
        submitHandler = new RejectGeneralApplicationAboutToSubmitHandler(finremCaseDetailsMapper, helper, generalApplicationService);
    }

    @Test
    public void givenCase_whenCorrectConfigSupplied_thenHandlerCanHandle() {
        assertThat(submitHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.REJECT_GENERAL_APPLICATION),
            is(true));
    }

    @Test
    public void givenCase_whenInCorrectConfigCaseTypeSupplied_thenHandlerCanHandle() {
        assertThat(submitHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.REJECT_GENERAL_APPLICATION),
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
                .canHandle(CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.REJECT_GENERAL_APPLICATION),
            is(false));
    }

    @Ignore
    @Test
    public void givenCase_whenRejectingAnApplication_thenRemoveElementFromCollection() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest(GA_JSON);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> startHandle = startHandler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData caseData = startHandle.getData();
        DynamicList dynamicList = helper.objectToDynamicList(caseData.getGeneralApplicationWrapper().getGeneralApplicationList());
        assertEquals(2, dynamicList.getListItems().size());
        assertNull(caseData.getGeneralApplicationWrapper().getGeneralApplicationRejectReason());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> submitHandle = submitHandler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData data = submitHandle.getData();

        List<GeneralApplicationCollectionData> generalApplicationCollectionData
            = helper.covertToGeneralApplicationData(data.getGeneralApplicationWrapper().getGeneralApplications());
        assertEquals(1, generalApplicationCollectionData.size());
        assertEquals("applicationIssued", submitHandle.getState());
    }

    @Test
    public void givenCase_whenNoApplicationAvailableToReject_thenReturnError() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest(NO_GA_JSON);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> startHandle = startHandler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData caseData = startHandle.getData();
        DynamicList dynamicList = helper.objectToDynamicList(caseData.getGeneralApplicationWrapper().getGeneralApplicationList());
        assertNull(dynamicList);
        assertNull(caseData.getGeneralApplicationWrapper().getGeneralApplicationRejectReason());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> submitHandle = submitHandler.handle(callbackRequest, AUTH_TOKEN);
        assertThat(submitHandle.getErrors(), CoreMatchers.hasItem("There is no general application available to reject."));

    }

    @Test
    public void givenCase_whenRejectingAnExistinNonCollApplication_thenRemoveGeneralApplicationData() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest(GA_NON_COLL_JSON);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> startHandle = startHandler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData caseData = startHandle.getData();
        DynamicList dynamicList = helper.objectToDynamicList(caseData.getGeneralApplicationWrapper().getGeneralApplicationList());
        assertEquals(1, dynamicList.getListItems().size());
        assertNull(caseData.getGeneralApplicationWrapper().getGeneralApplicationRejectReason());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> submitHandle = submitHandler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData data = submitHandle.getData();
        assertExistingGeneralApplication(data);

    }

    private void assertExistingGeneralApplication(FinremCaseData caseData) {
        assertNull(caseData.getGeneralApplicationWrapper().getGeneralApplicationReceivedFrom());
        assertNull(caseData.getGeneralApplicationWrapper().getGeneralApplicationCreatedBy());
        assertNull(caseData.getGeneralApplicationWrapper().getGeneralApplicationHearingRequired());
        assertNull(caseData.getGeneralApplicationWrapper().getGeneralApplicationTimeEstimate());
        assertNull(caseData.getGeneralApplicationWrapper().getGeneralApplicationSpecialMeasures());
        assertNull(caseData.getGeneralApplicationWrapper().getGeneralApplicationDocument());
        assertNull(caseData.getGeneralApplicationWrapper().getGeneralApplicationDraftOrder());
        assertNull(caseData.getGeneralApplicationWrapper().getGeneralApplicationLatestDocumentDate());
    }

}