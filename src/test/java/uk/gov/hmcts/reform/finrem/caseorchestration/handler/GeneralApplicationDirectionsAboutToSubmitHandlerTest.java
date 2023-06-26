package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationItems;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationDirectionsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.GeneralApplicationStatus.DIRECTION_APPROVED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.GeneralApplicationStatus.DIRECTION_NOT_APPROVED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.GeneralApplicationStatus.DIRECTION_OTHER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_LEVEL_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PREPARE_FOR_HEARING_STATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;

@RunWith(MockitoJUnitRunner.class)
public class GeneralApplicationDirectionsAboutToSubmitHandlerTest extends BaseHandlerTest {

    private GeneralApplicationDirectionsAboutToStartHandler startHandler;
    private GeneralApplicationDirectionsAboutToSubmitHandler submitHandler;
    private GeneralApplicationHelper helper;
    @Mock
    private AssignCaseAccessService assignCaseAccessService;
    @Mock
    private GeneralApplicationDirectionsService service;
    @Mock
    private GenericDocumentService documentService;
    @Mock
    private GeneralApplicationService gaService;

    private ObjectMapper objectMapper;
    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    public static final String AUTH_TOKEN = "tokien:)";

    @Before
    public void setup() {
        objectMapper = new ObjectMapper();
        finremCaseDetailsMapper = new FinremCaseDetailsMapper(objectMapper);
        helper = new GeneralApplicationHelper(objectMapper, documentService);
        startHandler = new GeneralApplicationDirectionsAboutToStartHandler(
            assignCaseAccessService, finremCaseDetailsMapper, helper, service);
        submitHandler = new GeneralApplicationDirectionsAboutToSubmitHandler(
            finremCaseDetailsMapper, helper, service, gaService);
    }

    @Test
    public void givenCase_whenCorrectConfigSupplied_thenHandlerCanHandle() {
        assertThat(submitHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.GENERAL_APPLICATION_DIRECTIONS),
            is(true));
    }

    @Test
    public void givenCase_whenInCorrectConfigCaseTypeSupplied_thenHandlerCanHandle() {
        assertThat(submitHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.GENERAL_APPLICATION_DIRECTIONS),
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
                .canHandle(CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.GENERAL_APPLICATION_DIRECTIONS),
            is(false));
    }

    @Test
    public void givenCase_whenExistingApplication_thenMigratedAndUpdateStatusApprovedCompleted() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest();

        callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper().getGeneralApplications().forEach(x ->
            x.getValue().setGeneralApplicationStatus(DIRECTION_APPROVED.getId()));

        DynamicList dynamicListForCaseDetails = DynamicList.builder().build();
        DynamicListElement listElement = DynamicListElement.builder()
            .label("General Application 1 - Received from - applicant - Created Date 2023-06-13 -Hearing Required - No")
            .code("46132d38-259b-467e-bd4e-e4d7431e32f0#Not Approved").build();
        dynamicListForCaseDetails.setValue(listElement);
        List<DynamicListElement> listItems = new ArrayList<>();
        listItems.add(listElement);
        dynamicListForCaseDetails.setListItems(listItems);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> startHandle = startHandler.handle(callbackRequest, AUTH_TOKEN);
        FinremCaseData caseData = startHandle.getData();
        FinremCaseData finremCaseData = startHandle.getData();
        finremCaseData.getGeneralApplicationWrapper().setGeneralApplicationDirectionsList(dynamicListForCaseDetails);
        DynamicList dynamicList = helper.objectToDynamicList(caseData.getGeneralApplicationWrapper().getGeneralApplicationDirectionsList());
        assertEquals(1, dynamicList.getListItems().size());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> submitHandle = submitHandler.handle(callbackRequest, AUTH_TOKEN);
        FinremCaseData data = submitHandle.getData();

        List<GeneralApplicationCollectionData> list
            = helper.covertToGeneralApplicationData(data.getGeneralApplicationWrapper().getGeneralApplications());
        assertEquals(2, list.size());

        assertEquals("InterimHearingNotice.pdf", list.get(1).getGeneralApplicationItems()
            .getGeneralApplicationDocument().getDocumentFilename());
        assertEquals("InterimHearingNotice.pdf", list.get(1).getGeneralApplicationItems()
            .getGeneralApplicationDraftOrder().getDocumentFilename());
        assertEquals("app_docs.pdf", list.get(1).getGeneralApplicationItems()
            .getGeneralApplicationDirectionsDocument().getDocumentFilename());

        assertEquals(DIRECTION_APPROVED.getId(),
            list.get(1).getGeneralApplicationItems().getGeneralApplicationStatus());
        assertNull(data.getGeneralApplicationWrapper().getGeneralApplicationDirectionsList());
        assertNull(data.getGeneralApplicationWrapper().getGeneralApplicationOutcome());
    }

    @Test
    public void givenCase_whenApproveAnApplication_thenUpdateStatusApprovedCompleted() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest();

        List<GeneralApplicationCollectionData> existingList = helper.getGeneralApplicationList(callbackRequest.getCaseDetails().getData(),
            GENERAL_APPLICATION_COLLECTION);
        existingList.forEach(x -> x.getGeneralApplicationItems().setGeneralApplicationStatus(DIRECTION_APPROVED.getId()));
        callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper()
            .setGeneralApplications(helper.convertToGeneralApplicationsCollection(existingList));

        DynamicList dynamicListForCaseDetails = DynamicList.builder().build();
        DynamicListElement listElement = DynamicListElement.builder()
            .label("General Application 1 - Received from - applicant - Created Date 2023-06-13 -Hearing Required - No")
            .code("46132d38-259b-467e-bd4e-e4d7431e32f0#Not Approved").build();
        dynamicListForCaseDetails.setValue(listElement);
        List<DynamicListElement> listItems = new ArrayList<>();
        listItems.add(listElement);
        dynamicListForCaseDetails.setListItems(listItems);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> startHandle = startHandler.handle(callbackRequest, AUTH_TOKEN);
        FinremCaseData caseData = startHandle.getData();
        caseData.getGeneralApplicationWrapper().setGeneralApplicationDirectionsList(dynamicListForCaseDetails);
        DynamicList dynamicList = helper.objectToDynamicList(caseData.getGeneralApplicationWrapper().getGeneralApplicationDirectionsList());
        assertEquals(1, dynamicList.getListItems().size());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> submitHandle = submitHandler.handle(callbackRequest, AUTH_TOKEN);
        FinremCaseData data = submitHandle.getData();

        List<GeneralApplicationCollectionData> list
            = helper.covertToGeneralApplicationData(data.getGeneralApplicationWrapper().getGeneralApplications());
        assertEquals(2, list.size());

        assertEquals(DIRECTION_APPROVED.getId(),
            list.get(1).getGeneralApplicationItems().getGeneralApplicationStatus());
        assertNull(caseData.getGeneralApplicationWrapper().getGeneralApplicationDirectionsList());
        assertNull(caseData.getGeneralApplicationWrapper().getGeneralApplicationDirectionsDocument());

        verify(service).submitCollectionGeneralApplicationDirections(any(), any(), any());
    }


    @Test
    public void givenCase_whenNotApproveAnApplication_thenUpdateStatusNotApproved() {
        FinremCallbackRequest finremCallbackRequest = buildFinremCallbackRequest();
        List<GeneralApplicationCollectionData> existingList = helper.getGeneralApplicationList(finremCallbackRequest.getCaseDetails().getData(),
            GENERAL_APPLICATION_COLLECTION);
        existingList.forEach(x -> x.getGeneralApplicationItems().setGeneralApplicationStatus(DIRECTION_NOT_APPROVED.getId()));
        finremCallbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper().setGeneralApplications(
            helper.convertToGeneralApplicationsCollection(existingList));

        DynamicList dynamicListForCaseDetails = DynamicList.builder().build();
        DynamicListElement listElement = DynamicListElement.builder()
            .label("General Application 1 - Received from - applicant - Created Date 2023-06-13 -Hearing Required - No")
            .code("46132d38-259b-467e-bd4e-e4d7431e32f0#Not Approved").build();
        dynamicListForCaseDetails.setValue(listElement);
        List<DynamicListElement> listItems = new ArrayList<>();
        listItems.add(listElement);
        dynamicListForCaseDetails.setListItems(listItems);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> startHandle = startHandler.handle(finremCallbackRequest, AUTH_TOKEN);
        FinremCaseData finremCaseData = startHandle.getData();
        finremCaseData.getGeneralApplicationWrapper().setGeneralApplicationDirectionsList(dynamicListForCaseDetails);
        DynamicList dynamicList = helper.objectToDynamicList(finremCaseData.getGeneralApplicationWrapper().getGeneralApplicationDirectionsList());
        assertEquals(1, dynamicList.getListItems().size());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> submitHandle = submitHandler.handle(finremCallbackRequest, AUTH_TOKEN);
        FinremCaseData data = submitHandle.getData();

        List<GeneralApplicationCollectionData> list
            = helper.covertToGeneralApplicationData(data.getGeneralApplicationWrapper().getGeneralApplications());
        assertEquals(2, list.size());

        assertEquals(DIRECTION_NOT_APPROVED.getId(),
            list.get(0).getGeneralApplicationItems().getGeneralApplicationStatus());
        assertNull(data.getGeneralApplicationWrapper().getGeneralApplicationDirectionsList());
        assertNull(data.getGeneralApplicationWrapper().getGeneralApplicationDirectionsDocument());
    }

    @Test
    public void givenCase_whenOtherAnApplication_thenUpdateStatusOther() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest();
        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();
        List<GeneralApplicationCollectionData> existingList = helper.getGeneralApplicationList(finremCaseDetails.getData(),
            GENERAL_APPLICATION_COLLECTION);
        existingList.forEach(x -> x.getGeneralApplicationItems().setGeneralApplicationStatus(DIRECTION_OTHER.getId()));
        callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper().setGeneralApplications(
            helper.convertToGeneralApplicationsCollection(existingList));

        DynamicList dynamicListForCaseDetails = DynamicList.builder().build();
        DynamicListElement listElement = DynamicListElement.builder()
            .label("General Application 1 - Received from - applicant - Created Date 2023-06-13 -Hearing Required - No")
            .code("46132d38-259b-467e-bd4e-e4d7431e32f0#Not Approved").build();
        dynamicListForCaseDetails.setValue(listElement);
        List<DynamicListElement> listItems = new ArrayList<>();
        listItems.add(listElement);
        dynamicListForCaseDetails.setListItems(listItems);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> startHandle = startHandler.handle(callbackRequest, AUTH_TOKEN);
        FinremCaseData finremCaseData = startHandle.getData();
        finremCaseData.getGeneralApplicationWrapper().setGeneralApplicationDirectionsList(dynamicListForCaseDetails);
        DynamicList dynamicList = helper.objectToDynamicList(finremCaseData.getGeneralApplicationWrapper().getGeneralApplicationDirectionsList());

        assertEquals(1, dynamicList.getListItems().size());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> submitHandle = submitHandler.handle(callbackRequest, AUTH_TOKEN);
        FinremCaseData data = submitHandle.getData();

        List<GeneralApplicationCollectionData> list = helper.covertToGeneralApplicationData(
            data.getGeneralApplicationWrapper().getGeneralApplications());
        assertEquals(2, list.size());

        assertEquals(DIRECTION_OTHER.getId(),
            list.get(1).getGeneralApplicationItems().getGeneralApplicationStatus());
        assertNull(data.getGeneralApplicationWrapper().getGeneralApplicationDirectionsList());
        assertNull(data.getGeneralApplicationWrapper().getGeneralApplicationDirectionsDocument());
    }

    @Test
    public void givenCase_whenApproveAnApplication_thenUpdateStatusApprovedCompletedAndReturnToPostState() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest();
        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();
        when(service.getEventPostState(any(), any())).thenReturn(PREPARE_FOR_HEARING_STATE);
        List<GeneralApplicationCollectionData> existingList = helper.getGeneralApplicationList(finremCaseDetails.getData(),
            GENERAL_APPLICATION_COLLECTION);
        existingList.forEach(x -> x.getGeneralApplicationItems().setGeneralApplicationStatus(DIRECTION_APPROVED.getId()));
        callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper().setGeneralApplications(
            helper.convertToGeneralApplicationsCollection(existingList));
        DynamicList dynamicListForCaseDetails = DynamicList.builder().build();
        DynamicListElement listElement = DynamicListElement.builder()
            .label("General Application 1 - Received from - applicant - Created Date 2023-06-13 -Hearing Required - No")
            .code("46132d38-259b-467e-bd4e-e4d7431e32f0#Not Approved").build();
        dynamicListForCaseDetails.setValue(listElement);
        List<DynamicListElement> listItems = new ArrayList<>();
        listItems.add(listElement);
        dynamicListForCaseDetails.setListItems(listItems);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> startHandle = startHandler.handle(callbackRequest, AUTH_TOKEN);
        FinremCaseData finremCaseData = startHandle.getData();
        finremCaseData.getGeneralApplicationWrapper().setGeneralApplicationDirectionsList(dynamicListForCaseDetails);
        DynamicList dynamicList = helper.objectToDynamicList(finremCaseData.getGeneralApplicationWrapper().getGeneralApplicationDirectionsList());
        assertEquals(1, dynamicList.getListItems().size());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> submitHandle = submitHandler.handle(callbackRequest, AUTH_TOKEN);
        FinremCaseData data = submitHandle.getData();

        List<GeneralApplicationCollectionData> list
            = helper.covertToGeneralApplicationData(data.getGeneralApplicationWrapper().getGeneralApplications());
        assertEquals(2, list.size());

        assertEquals(DIRECTION_APPROVED.getId(),
            list.get(1).getGeneralApplicationItems().getGeneralApplicationStatus());
        assertNull(data.getGeneralApplicationWrapper().getGeneralApplicationDirectionsList());
        assertNull(data.getGeneralApplicationWrapper().getGeneralApplicationDirectionsList());
        assertEquals(PREPARE_FOR_HEARING_STATE, submitHandle.getState());
        verify(service).submitCollectionGeneralApplicationDirections(any(), any(), any());
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

    public DynamicRadioListElement getDynamicListElement(String code, String label) {
        return DynamicRadioListElement.builder()
            .code(code)
            .label(label)
            .build();
    }

    private FinremCallbackRequest buildFinremCallbackRequest() {
        GeneralApplicationItems generalApplicationItems =
            GeneralApplicationItems.builder().generalApplicationReceivedFrom(buildDynamicIntervenerList())
                .generalApplicationCreatedBy("Claire Mumford")
                .generalApplicationHearingRequired("Yes").generalApplicationTimeEstimate("24 hours")
                .generalApplicationSpecialMeasures("Special measure").build();
        GeneralApplicationsCollection generalApplications = GeneralApplicationsCollection.builder().build();
        GeneralApplicationsCollection generalApplicationsBefore = GeneralApplicationsCollection.builder().build();
        generalApplications.setValue(generalApplicationItems);
        generalApplicationsBefore.setId(UUID.randomUUID());
        generalApplications.setId(UUID.randomUUID());
        CaseDocument caseDocument1 = CaseDocument.builder().documentFilename("InterimHearingNotice.pdf")
            .documentUrl("http://dm-store/documents/b067a2dd-657a-4ed2-98c3-9c3159d1482e")
            .documentBinaryUrl("http://dm-store/documents/b067a2dd-657a-4ed2-98c3-9c3159d1482e/binary").build();
        CaseDocument caseDocument2 = CaseDocument.builder().documentFilename("InterimHearingNotice.pdf")
            .documentUrl("http://dm-store/documents/b067a2dd-657a-4ed2-98c3-9c3159d1482e")
            .documentBinaryUrl("http://dm-store/documents/b067a2dd-657a-4ed2-98c3-9c3159d1482e/binary").build();
        CaseDocument caseDocument3 = CaseDocument.builder().documentFilename("app_docs.pdf")
            .documentUrl("http://dm-store/documents/b067a2dd-657a-4ed2-98c3-9c3159d1482e")
            .documentBinaryUrl("http://dm-store/documents/b067a2dd-657a-4ed2-98c3-9c3159d1482e/binary").build();
        GeneralApplicationItems generalApplicationItemsAdded =
            GeneralApplicationItems.builder().generalApplicationReceivedFrom(buildDynamicIntervenerList())
                .generalApplicationDraftOrder(caseDocument2)
                .generalApplicationDirectionsDocument(caseDocument3).generalApplicationDocument(caseDocument1)
                .generalApplicationCreatedBy("Claire Mumford")
                .generalApplicationHearingRequired("No").generalApplicationTimeEstimate("48 hours")
                .generalApplicationSpecialMeasures("Special measure").build();
        generalApplicationsBefore.setValue(generalApplicationItemsAdded);
        List<GeneralApplicationsCollection> generalApplicationsCollection = new ArrayList<>();
        List<GeneralApplicationsCollection> generalApplicationsCollectionBefore = new ArrayList<>();
        generalApplicationsCollectionBefore.add(generalApplications);
        generalApplicationsCollection.add(generalApplications);
        generalApplicationsCollection.add(generalApplicationsBefore);

        FinremCaseData caseData = FinremCaseData.builder()
            .generalApplicationWrapper(GeneralApplicationWrapper.builder()
                .generalApplicationCreatedBy("Claire Mumford").generalApplicationPreState("applicationIssued")
                .generalApplications(generalApplicationsCollection)
                .build()).build();
        FinremCaseData caseDataBefore = FinremCaseData.builder()
            .generalApplicationWrapper(GeneralApplicationWrapper.builder()
                .generalApplicationCreatedBy("Claire Mumford").generalApplicationPreState("applicationIssued")
                .generalApplications(generalApplicationsCollectionBefore)
                .build()).build();
        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder()
            .caseType(CaseType.CONTESTED)
            .id(12345L)
            .state(State.CASE_ADDED)
            .data(caseData)
            .build();
        FinremCaseDetails finremCaseDetailsBefore = FinremCaseDetails.builder()
            .caseType(CaseType.CONTESTED)
            .id(12345L)
            .state(State.CASE_ADDED)
            .data(caseDataBefore)
            .build();
        return FinremCallbackRequest.builder()
            .caseDetails(finremCaseDetails)
            .caseDetailsBefore(finremCaseDetailsBefore)
            .build();
    }

}