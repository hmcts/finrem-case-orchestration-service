package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import java.io.InputStream;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.GeneralApplicationStatus.APPROVED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_LEVEL_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;

@RunWith(MockitoJUnitRunner.class)
public class GeneralApplicationOutcomeAboutToStartHandlerTest extends BaseHandlerTest {

    private GeneralApplicationOutcomeAboutToStartHandler handler;
    @Mock
    private GenericDocumentService service;
    @Mock
    private GeneralApplicationService gaService;
    private GeneralApplicationHelper helper;
    private ObjectMapper objectMapper;
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    public static final String AUTH_TOKEN = "tokien:)";
    private static final String GA_JSON = "/fixtures/contested/general-application-referred-finrem.json";
    private static final String GA_NON_COLL_JSON = "/fixtures/contested/general-application.json";


    @Before
    public void setup() {
        objectMapper = new ObjectMapper();
        helper = new GeneralApplicationHelper(objectMapper, service);
        handler = new GeneralApplicationOutcomeAboutToStartHandler(finremCaseDetailsMapper, helper, gaService);
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
        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder().caseDetails(buildCaseDetailsWithPath(GA_JSON))
            .caseDetailsBefore(buildCaseDetailsWithPath(GA_NON_COLL_JSON)).build();
        callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper().getGeneralApplications().forEach(
            x -> x.getValue().setGeneralApplicationStatus("Approved"));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> startHandle = handler.handle(callbackRequest, AUTH_TOKEN);
        assertTrue(startHandle.getErrors().contains("There are no general application available for decision."));
    }

    @Test
    public void givenCase_whenExistingGeneAppNonCollection_thenCreateSelectionList() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder().caseDetails(buildCaseDetailsWithPath(GA_NON_COLL_JSON))
            .caseDetailsBefore(buildCaseDetailsWithPath(GA_NON_COLL_JSON)).build();
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData caseData = handle.getData();
        DynamicList dynamicList = helper.objectToDynamicList(caseData.getGeneralApplicationWrapper().getGeneralApplicationOutcomeList());

        assertEquals(1, dynamicList.getListItems().size());
        assertNull(caseData.getGeneralApplicationWrapper().getGeneralApplicationOutcome());
    }

    @Test
    public void givenCase_whenExistingGeneAppAsACollection_thenCreateSelectionList() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder().caseDetails(buildCaseDetailsWithPath(GA_JSON))
            .caseDetailsBefore(buildCaseDetailsWithPath(GA_JSON)).build();
        callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper().getGeneralApplications()
            .forEach(x -> x.getValue().setGeneralApplicationReceivedFrom(buildDynamicIntervenerList()));
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData caseData = handle.getData();
        DynamicList dynamicList = helper.objectToDynamicList(caseData.getGeneralApplicationWrapper().getGeneralApplicationOutcomeList());

        assertEquals(2, dynamicList.getListItems().size());
        assertNull(caseData.getGeneralApplicationWrapper().getGeneralApplicationOutcome());
    }

    @Test
    public void givenCase_whenNoExistingGeneAppAvailable_thenShowError() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder().caseDetails(buildCaseDetailsWithPath(GA_JSON))
            .caseDetailsBefore(buildCaseDetailsWithPath(GA_NON_COLL_JSON)).build();
        callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper().getGeneralApplications()
            .forEach(x -> x.getValue().setGeneralApplicationReceivedFrom(buildDynamicIntervenerList()));
        FinremCaseData data = callbackRequest.getCaseDetails().getData();

        List<GeneralApplicationCollectionData> existingList = helper.getGeneralApplicationList(data, GENERAL_APPLICATION_COLLECTION);
        existingList.forEach(x -> x.getGeneralApplicationItems().setGeneralApplicationStatus(String.valueOf(APPROVED)));
        callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper().setGeneralApplications(
            helper.convertToGeneralApplicationsCollection(existingList));
        callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper().setGeneralApplicationCreatedBy(null);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(callbackRequest, AUTH_TOKEN);
        assertThat(handle.getErrors(), CoreMatchers.hasItem("There are no general application available for decision."));
    }

    private FinremCaseDetails buildCaseDetailsWithPath(String path) {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(path)) {
            FinremCaseDetails caseDetails =
                objectMapper.readValue(resourceAsStream, FinremCallbackRequest.class).getCaseDetails();
            return FinremCallbackRequest.builder().caseDetails(caseDetails).build().getCaseDetails();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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