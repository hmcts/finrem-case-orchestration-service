package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.GeneralApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.GeneralApplicationStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationOutcome;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_LEVEL_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;

@RunWith(MockitoJUnitRunner.class)
public class GeneralApplicationOutcomeAboutToSubmitHandlerTest extends BaseHandlerTest {

    private GeneralApplicationOutcomeAboutToStartHandler startHandler;
    private GeneralApplicationOutcomeAboutToSubmitHandler submitHandler;
    @Mock
    private GenericDocumentService service;
    @Mock
    private GeneralApplicationService gaService;
    private GeneralApplicationHelper helper;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    public static final String AUTH_TOKEN = "tokien:)";
    private static final String GA_JSON = "/fixtures/contested/general-application-referred-finrem.json";
    private static final String GA_NON_COLL_JSON = "/fixtures/contested/general-application-finrem.json";
    private static final String NO_GA_JSON = "/fixtures/contested/no-general-application-finrem.json";
    private CaseDetails caseDetails;

    @Before
    public void setup() {
        objectMapper = new ObjectMapper();
        finremCaseDetailsMapper = new FinremCaseDetailsMapper(objectMapper);
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
    @Test
    public void givenCase_whenNonCollectionApproveAnApplication_thenMigratedAndUpdateStatusApproved() {
        FinremCallbackRequest callbackRequest =
            buildFinremCallbackRequest(GA_NON_COLL_JSON);
        callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper().getGeneralApplications()
            .forEach(ga -> ga.getValue().setGeneralApplicationSender(buildDynamicIntervenerList()));
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> startHandle = startHandler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData caseData = startHandle.getData();
        DynamicList dynamicList = helper.objectToDynamicList(caseData.getGeneralApplicationWrapper().getGeneralApplicationOutcomeList());
        assertEquals(1, dynamicList.getListItems().size());

        callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper().setGeneralApplicationOutcome(GeneralApplicationOutcome.APPROVED);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> submitHandle = submitHandler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData data = submitHandle.getData();
        List<GeneralApplicationsCollection> generalApplicationsCollection = data.getGeneralApplicationWrapper().getGeneralApplications();

        assertEquals(1, generalApplicationsCollection.size());

        assertEquals(GeneralApplicationStatus.APPROVED.getId(),
            generalApplicationsCollection.get(0).getValue().getGeneralApplicationStatus());
        assertNull(data.getGeneralApplicationWrapper().getGeneralApplicationOutcomeList());
        assertNull(data.getGeneralApplicationWrapper().getGeneralApplicationOutcome());
    }

    @Test
    public void givenCase_whenApproveAnApplication_thenUpdateStatusApproved() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest(GA_JSON);
        callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper().getGeneralApplications()
            .forEach(ga -> ga.getValue().setGeneralApplicationSender(buildDynamicIntervenerList()));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> startHandle = startHandler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData caseData = startHandle.getData();
        DynamicList dynamicList = helper.objectToDynamicList(caseData.getGeneralApplicationWrapper().getGeneralApplicationOutcomeList());
        assertEquals(2, dynamicList.getListItems().size());
        callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper().setGeneralApplicationOutcome(GeneralApplicationOutcome.APPROVED);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> submitHandle = submitHandler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData data = submitHandle.getData();
        List<GeneralApplicationCollectionData> generalApplicationCollectionData
            = helper.covertToGeneralApplicationData(data.getGeneralApplicationWrapper().getGeneralApplications());
        assertEquals(2, generalApplicationCollectionData.size());

        assertEquals(GeneralApplicationStatus.APPROVED.getId(),
            generalApplicationCollectionData.get(0).getGeneralApplicationItems().getGeneralApplicationStatus());
        assertNull(data.getGeneralApplicationWrapper().getGeneralApplicationOutcomeList());
        assertNull(data.getGeneralApplicationWrapper().getGeneralApplicationOutcome());
    }

    @Test
    public void givenCase_whenNotApproveAnApplication_thenUpdateStatusNotApproved() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest(GA_JSON);
        callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper().getGeneralApplications()
            .forEach(ga -> ga.getValue().setGeneralApplicationSender(buildDynamicIntervenerList()));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> startHandle =
            startHandler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData caseData = startHandle.getData();
        DynamicList dynamicList = helper.objectToDynamicList(caseData.getGeneralApplicationWrapper()
            .getGeneralApplicationOutcomeList());
        assertEquals(2, dynamicList.getListItems().size());
        callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper()
            .setGeneralApplicationOutcome(GeneralApplicationOutcome.NOT_APPROVED);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> submitHandle =
            submitHandler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData data = submitHandle.getData();
        List<GeneralApplicationCollectionData> generalApplicationCollectionData
            = helper.covertToGeneralApplicationData(data.getGeneralApplicationWrapper().getGeneralApplications());
        assertEquals(2, generalApplicationCollectionData.size());

        assertEquals(GeneralApplicationStatus.NOT_APPROVED.getId(),
              generalApplicationCollectionData.get(0).getGeneralApplicationItems().getGeneralApplicationStatus());
        assertNull(data.getGeneralApplicationWrapper().getGeneralApplicationOutcomeList());
    }

    @Test
    public void givenCase_whenOtherAnApplication_thenUpdateStatusOther() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest(GA_JSON);
        callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper().getGeneralApplications()
            .forEach(ga -> ga.getValue().setGeneralApplicationSender(buildDynamicIntervenerList()));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> startHandle = startHandler
            .handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData caseData = startHandle.getData();
        DynamicList dynamicList = helper.objectToDynamicList(caseData.getGeneralApplicationWrapper()
            .getGeneralApplicationOutcomeList());
        assertEquals(2, dynamicList.getListItems().size());
        callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper().setGeneralApplicationOutcome(
            GeneralApplicationOutcome.OTHER);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> submitHandle = submitHandler
            .handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData data = submitHandle.getData();
        List<GeneralApplicationCollectionData> generalApplicationCollectionData
            = helper.covertToGeneralApplicationData(data.getGeneralApplicationWrapper().getGeneralApplications());
        assertEquals(2, generalApplicationCollectionData.size());

        assertEquals(GeneralApplicationStatus.OTHER.getId(),
            generalApplicationCollectionData.get(0).getGeneralApplicationItems().getGeneralApplicationStatus());
        assertNull(data.getGeneralApplicationWrapper().getGeneralApplicationOutcomeList());
    }

    @Test
    public void whenGeneralApplicationListIsEmptyAndThereIsACreator_thenShouldMigrateExistingApplication() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest(NO_GA_JSON);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> startHandle = startHandler
            .handle(callbackRequest, AUTH_TOKEN);
        FinremCaseData caseData = startHandle.getData();
        GeneralApplicationWrapper wrapper = caseData.getGeneralApplicationWrapper();
        wrapper.setGeneralApplicationCreatedBy("Claire Mumford");
        String collectionId = UUID.randomUUID().toString();
        wrapper.setGeneralApplicationTracking(collectionId);
        wrapper.setGeneralApplicationSpecialMeasures("There will be special measures");
        wrapper.setGeneralApplicationOutcome(GeneralApplicationOutcome.APPROVED);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> submitHandle = submitHandler
            .handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData data = submitHandle.getData();

        assertEquals(wrapper.getGeneralApplicationOutcomeList(), null);
        assertEquals(data.getGeneralApplicationWrapper().getGeneralApplicationCreatedBy(), null);
        assertEquals(data.getGeneralApplicationWrapper().getGeneralApplicationReceivedFrom(), null);
        assertEquals(data.getGeneralApplicationWrapper().getGeneralApplicationHearingRequired(), null);
        assertEquals(data.getGeneralApplicationWrapper().getGeneralApplicationTimeEstimate(), null);
        assertEquals(data.getGeneralApplicationWrapper().getGeneralApplicationSpecialMeasures(), null);
        assertEquals(data.getGeneralApplicationWrapper().getGeneralApplicationDocument(), null);
        assertEquals(data.getGeneralApplicationWrapper().getGeneralApplicationDraftOrder(), null);
        assertEquals(data.getGeneralApplicationWrapper().getGeneralApplicationTracking(), null);
    }

    public DynamicRadioListElement getDynamicListElement(String code, String label) {
        return DynamicRadioListElement.builder()
            .code(code)
            .label(label)
            .build();
    }

    public DynamicRadioList buildDynamicIntervenerList() {

        List<DynamicRadioListElement> dynamicListElements = List.of(getDynamicListElement(APPLICANT, APPLICANT),
            getDynamicListElement(RESPONDENT, RESPONDENT),
            getDynamicListElement(CASE_LEVEL_ROLE, CASE_LEVEL_ROLE)
        );
        return DynamicRadioList.builder()
            .value(dynamicListElements.get(0))
            .listItems(dynamicListElements)
            .build();
    }
}