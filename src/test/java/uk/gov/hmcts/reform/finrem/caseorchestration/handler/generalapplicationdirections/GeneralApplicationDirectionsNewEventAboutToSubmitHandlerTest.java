package uk.gov.hmcts.reform.finrem.caseorchestration.handler.generalapplicationdirections;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationOutcome;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationDirectionsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PartyService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.GeneralApplicationsCategoriser;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.ManageHearingActionService;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@RunWith(MockitoJUnitRunner.class)
class GeneralApplicationDirectionsNewEventAboutToSubmitHandlerTest {

    private GeneralApplicationDirectionsNewEventAboutToStartHandler startHandler;
    private GeneralApplicationDirectionsNewEventAboutToSubmitHandler aboutToSubmitHandler;
    @Mock
    private GeneralApplicationHelper helper;
    @Mock
    private AssignCaseAccessService assignCaseAccessService;
    @Mock
    private GeneralApplicationDirectionsService service;
    @Mock
    private GeneralApplicationService gaService;
    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;
    @Mock
    private GeneralApplicationsCategoriser generalApplicationsCategoriser;
    @Mock
    private ManageHearingActionService manageHearingActionService;
    @Mock
    private PartyService partyService;

    private ObjectMapper objectMapper;

    public static final String AUTH_TOKEN = "tokien:)";
    private static final String GA_JSON = "/fixtures/contested/general-application-details.json";

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        startHandler = new GeneralApplicationDirectionsNewEventAboutToStartHandler(
            assignCaseAccessService, finremCaseDetailsMapper, helper, service, partyService);
        aboutToSubmitHandler = new GeneralApplicationDirectionsNewEventAboutToSubmitHandler(
            finremCaseDetailsMapper, helper, service, gaService, manageHearingActionService, generalApplicationsCategoriser);
    }

    @Test
    void testCanHandle() {
        assertCanHandle(aboutToSubmitHandler,
            Arguments.of(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.GENERAL_APPLICATION_DIRECTIONS_MH)
        );
    }

    /**
     * Given an approved general application,
     * When the handler processes the application,
     * Then the status is updated to approved and the post state is returned.
     */
    @Test
    void givenApplicationIsApproved_whenHandle_thenUpdateStatusToApprovedAndReturnToPostState() {
        // Arrange
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest();
        callbackRequest.setEventType(EventType.GENERAL_APPLICATION_DIRECTIONS_MH);

        callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper()
            .setGeneralApplications(List.of(GeneralApplicationsCollection.builder().build()));

        callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper()
            .setGeneralApplicationOutcome(GeneralApplicationOutcome.APPROVED);

        DynamicList dynamicListForCaseDetails = DynamicList.builder().build();
        DynamicListElement listElement = DynamicListElement.builder()
            .label("General Application 1 - Received from - applicant - Created Date 2023-06-13 -Hearing Required - No")
            .code("46132d38-259b-467e-bd4e-e4d7431e32f0#Not Approved").build();
        dynamicListForCaseDetails.setValue(listElement);

        List<DynamicListElement> listItems = new ArrayList<>();
        listItems.add(listElement);
        dynamicListForCaseDetails.setListItems(listItems);

        GeneralApplicationItems generalApplicationItems =
            GeneralApplicationItems.builder().generalApplicationSender(buildDynamicIntervenerList())
                .generalApplicationCreatedBy("Claire Mumford")
                .generalApplicationHearingRequired("Yes").generalApplicationTimeEstimate("24 hours")
                .generalApplicationSpecialMeasures("Special measure").build();

        when(helper.getApplicationItems(callbackRequest.getCaseDetails().getData(),
            AUTH_TOKEN, callbackRequest.getCaseDetails().getId().toString())).thenReturn(
            generalApplicationItems);

        // Act
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> startHandle = startHandler.handle(callbackRequest, AUTH_TOKEN);
        FinremCaseData caseData = startHandle.getData();
        FinremCaseData finremCaseData = startHandle.getData();

        finremCaseData.getGeneralApplicationWrapper().setGeneralApplicationDirectionsList(dynamicListForCaseDetails);
        finremCaseData.getGeneralApplicationWrapper().setGeneralApplicationDirectionsHearingRequired(YesOrNo.YES);

        DynamicList dynamicList = objectToDynamicList(caseData.getGeneralApplicationWrapper().getGeneralApplicationDirectionsList());
        assertEquals(1, dynamicList.getListItems().size());

        String collectionId = UUID.randomUUID().toString();

        when(helper.getGeneralApplicationList(any(), any())).thenReturn(
            List.of(GeneralApplicationCollectionData.builder()
                .id(collectionId)
                .generalApplicationItems(generalApplicationItems)
                .build()));
        when(helper.objectToDynamicList(any())).thenReturn(dynamicList);
        when(service.isHearingRequired(any(FinremCaseDetails.class))).thenReturn(true);

        doAnswer(invocation -> {
            FinremCaseDetails caseDetails = invocation.getArgument(1);

            caseDetails.getData().getGeneralApplicationWrapper().getGeneralApplications()
                .getFirst()
                .setValue(generalApplicationItems);

            return null;
        }).when(gaService).updateGeneralApplicationCollectionData(any(), any());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> submitHandle = aboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);
        FinremCaseData data = submitHandle.getData();

        // Assert
        List<GeneralApplicationsCollection> applications = data.getGeneralApplicationWrapper().getGeneralApplications();

        Assertions.assertThat(applications).hasSize(1);
        verify(manageHearingActionService).performAddHearing(any(FinremCaseDetails.class), any(String.class));
        verify(manageHearingActionService).updateTabData(any(FinremCaseData.class));
    }

    @Test
    void givenCase_whenExistingApplication_thenMigratedAndUpdateStatusApprovedCompleted() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest();
        callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper()
            .setGeneralApplications(List.of(GeneralApplicationsCollection.builder().build()));

        callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper()
            .setGeneralApplicationOutcome(GeneralApplicationOutcome.APPROVED);

        DynamicList dynamicListForCaseDetails = DynamicList.builder().build();
        DynamicListElement listElement = DynamicListElement.builder()
            .label("General Application 1 - Received from - applicant - Created Date 2023-06-13 -Hearing Required - No")
            .code("46132d38-259b-467e-bd4e-e4d7431e32f0#Not Approved").build();
        dynamicListForCaseDetails.setValue(listElement);
        List<DynamicListElement> listItems = new ArrayList<>();
        listItems.add(listElement);
        dynamicListForCaseDetails.setListItems(listItems);
        CaseDetails details = buildCaseDetailsFromJson();
        GeneralApplicationItems generalApplicationItems =
            GeneralApplicationItems.builder().generalApplicationSender(buildDynamicIntervenerList())
                .generalApplicationCreatedBy("Claire Mumford")
                .generalApplicationHearingRequired("Yes").generalApplicationTimeEstimate("24 hours")
                .generalApplicationSpecialMeasures("Special measure").build();
        when(finremCaseDetailsMapper.mapToCaseDetails(callbackRequest.getCaseDetails())).thenReturn(details);
        when(helper.getApplicationItems(callbackRequest.getCaseDetails().getData(),
            AUTH_TOKEN, callbackRequest.getCaseDetails().getId().toString())).thenReturn(
            generalApplicationItems);
        when(service.isHearingRequired(any(FinremCaseDetails.class))).thenReturn(true);

        callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper().setGeneralApplicationDirectionsHearingRequired(YesOrNo.YES);

        //Hearing required document
        Optional<CaseDocument> generatedDocument = Optional.of(CaseDocument.builder().documentFilename("HearingNotice.pdf")
            .documentUrl("http://dm-store/documents/b067a2dd-657a-4ed2-98c3-9c3159d1482e")
            .documentBinaryUrl("http://dm-store/documents/b067a2dd-657a-4ed2-98c3-9c3159d1482e/binary").build());
        when(service.generateGeneralApplicationDirectionsDocument(eq(AUTH_TOKEN), any(FinremCaseDetails.class))).thenReturn(generatedDocument);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> startHandle = startHandler.handle(callbackRequest, AUTH_TOKEN);
        FinremCaseData caseData = startHandle.getData();
        FinremCaseData finremCaseData = startHandle.getData();
        finremCaseData.getGeneralApplicationWrapper().setGeneralApplicationDirectionsList(dynamicListForCaseDetails);
        DynamicList dynamicList = objectToDynamicList(caseData.getGeneralApplicationWrapper().getGeneralApplicationDirectionsList());
        assertEquals(1, dynamicList.getListItems().size());

        String collectionId = UUID.randomUUID().toString();

        when(helper.mapExistingGeneralApplicationToData(callbackRequest.getCaseDetails().getData(),
            AUTH_TOKEN, callbackRequest.getCaseDetails().getId().toString())).thenReturn(
            GeneralApplicationCollectionData.builder()
                .id(collectionId)
                .generalApplicationItems(generalApplicationItems)
                .build());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> aboutToSubmitHandle = aboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);
        FinremCaseData data = aboutToSubmitHandle.getData();

        List<GeneralApplicationsCollection> applications = data.getGeneralApplicationWrapper().getGeneralApplications();
        Assertions.assertThat(applications).hasSize(1);

        verify(manageHearingActionService).performAddHearing(any(FinremCaseDetails.class), any(String.class));
        verify(manageHearingActionService).updateTabData(any(FinremCaseData.class));
    }

    @Test
    void givenCase_whenApproveAnApplication_thenUpdateStatusApprovedCompleted() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest();
        callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper()
            .getGeneralApplications().forEach(ga -> ga.getValue().setGeneralApplicationStatus(DIRECTION_APPROVED.getId()));

        DynamicList dynamicListForCaseDetails = DynamicList.builder().build();
        DynamicListElement listElement = DynamicListElement.builder()
            .label("General Application 1 - Received from - applicant - Created Date 2023-06-13 -Hearing Required - No")
            .code("46132d38-259b-467e-bd4e-e4d7431e32f0#Not Approved").build();
        dynamicListForCaseDetails.setValue(listElement);
        List<DynamicListElement> listItems = new ArrayList<>();
        listItems.add(listElement);
        dynamicListForCaseDetails.setListItems(listItems);
        CaseDetails details = buildCaseDetailsFromJson();
        GeneralApplicationItems generalApplicationItems =
            GeneralApplicationItems.builder().generalApplicationSender(buildDynamicIntervenerList())
                .generalApplicationCreatedBy("Claire Mumford")
                .generalApplicationHearingRequired("Yes").generalApplicationTimeEstimate("24 hours")
                .generalApplicationSpecialMeasures("Special measure").build();
        GeneralApplicationsCollection generalApplications = GeneralApplicationsCollection.builder()
            .value(generalApplicationItems).build();
        details.getData().put(GENERAL_APPLICATION_COLLECTION, generalApplications);
        when(helper.getApplicationItems(callbackRequest.getCaseDetails().getData(),
            AUTH_TOKEN, callbackRequest.getCaseDetails().getId().toString())).thenReturn(
            callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper()
                .getGeneralApplications().getFirst().getValue());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> startHandle = startHandler.handle(callbackRequest, AUTH_TOKEN);
        FinremCaseData caseData = startHandle.getData();
        caseData.getGeneralApplicationWrapper().setGeneralApplicationDirectionsList(dynamicListForCaseDetails);
        DynamicList dynamicList = objectToDynamicList(caseData.getGeneralApplicationWrapper().getGeneralApplicationDirectionsList());
        assertEquals(1, dynamicList.getListItems().size());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> submitHandle = aboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);
        FinremCaseData data = submitHandle.getData();

        List<GeneralApplicationsCollection> applications = data.getGeneralApplicationWrapper().getGeneralApplications();
        Assertions.assertThat(applications).hasSize(2);

        assertEquals(DIRECTION_APPROVED.getId(),
            applications.get(1).getValue().getGeneralApplicationStatus());
        assertNull(caseData.getGeneralApplicationWrapper().getGeneralApplicationDirectionsList());
        assertNull(caseData.getGeneralApplicationWrapper().getGeneralApplicationDirectionsDocument());

        verify(service).submitCollectionGeneralApplicationDirections(any(), any(), any());
    }

    @Test
    void givenCase_whenNotApproveAnApplication_thenUpdateStatusNotApproved() {
        FinremCallbackRequest finremCallbackRequest = buildFinremCallbackRequest();
        finremCallbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper().getGeneralApplications()
            .forEach(ga -> ga.getValue().setGeneralApplicationStatus(DIRECTION_NOT_APPROVED.getId()));

        DynamicList dynamicListForCaseDetails = DynamicList.builder().build();
        DynamicListElement listElement = DynamicListElement.builder()
            .label("General Application 1 - Received from - applicant - Created Date 2023-06-13 -Hearing Required - No")
            .code("46132d38-259b-467e-bd4e-e4d7431e32f0#Not Approved").build();
        dynamicListForCaseDetails.setValue(listElement);
        List<DynamicListElement> listItems = new ArrayList<>();
        listItems.add(listElement);

        dynamicListForCaseDetails.setListItems(listItems);

        CaseDetails details = buildCaseDetailsFromJson();
        GeneralApplicationItems generalApplicationItems =
            GeneralApplicationItems.builder().generalApplicationSender(buildDynamicIntervenerList())
                .generalApplicationCreatedBy("Claire Mumford")
                .generalApplicationHearingRequired("Yes").generalApplicationTimeEstimate("24 hours")
                .generalApplicationSpecialMeasures("Special measure").build();
        GeneralApplicationsCollection generalApplications = GeneralApplicationsCollection.builder()
            .value(generalApplicationItems).build();
        details.getData().put(GENERAL_APPLICATION_COLLECTION, generalApplications);
        when(helper.getApplicationItems(finremCallbackRequest.getCaseDetails().getData(),
            AUTH_TOKEN, finremCallbackRequest.getCaseDetails().getId().toString())).thenReturn(
            finremCallbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper()
                .getGeneralApplications().getFirst().getValue());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> startHandle = startHandler.handle(finremCallbackRequest, AUTH_TOKEN);
        FinremCaseData finremCaseData = startHandle.getData();
        finremCaseData.getGeneralApplicationWrapper().setGeneralApplicationDirectionsList(dynamicListForCaseDetails);
        DynamicList dynamicList = objectToDynamicList(finremCaseData.getGeneralApplicationWrapper().getGeneralApplicationDirectionsList());
        assertEquals(1, dynamicList.getListItems().size());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> submitHandle = aboutToSubmitHandler.handle(finremCallbackRequest, AUTH_TOKEN);
        FinremCaseData data = submitHandle.getData();

        List<GeneralApplicationsCollection> applications = data.getGeneralApplicationWrapper().getGeneralApplications();
        Assertions.assertThat(applications).hasSize(2);

        assertEquals(DIRECTION_NOT_APPROVED.getId(),
            applications.get(1).getValue().getGeneralApplicationStatus());
        assertNull(data.getGeneralApplicationWrapper().getGeneralApplicationDirectionsList());
        assertNull(data.getGeneralApplicationWrapper().getGeneralApplicationDirectionsDocument());
    }

    @Test
    void givenCase_whenOtherAnApplication_thenUpdateStatusOther() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest();
        callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper().getGeneralApplications(
        ).forEach(ga -> ga.getValue().setGeneralApplicationStatus(DIRECTION_OTHER.getId()));

        DynamicList dynamicListForCaseDetails = DynamicList.builder().build();
        DynamicListElement listElement = DynamicListElement.builder()
            .label("General Application 1 - Received from - applicant - Created Date 2023-06-13 -Hearing Required - No")
            .code("46132d38-259b-467e-bd4e-e4d7431e32f0#Not Approved").build();
        dynamicListForCaseDetails.setValue(listElement);
        List<DynamicListElement> listItems = new ArrayList<>();
        listItems.add(listElement);
        dynamicListForCaseDetails.setListItems(listItems);

        CaseDetails details = buildCaseDetailsFromJson();
        GeneralApplicationItems generalApplicationItems =
            GeneralApplicationItems.builder().generalApplicationSender(buildDynamicIntervenerList())
                .generalApplicationCreatedBy("Claire Mumford")
                .generalApplicationHearingRequired("Yes").generalApplicationTimeEstimate("24 hours")
                .generalApplicationSpecialMeasures("Special measure").build();
        GeneralApplicationsCollection generalApplications = GeneralApplicationsCollection.builder()
            .value(generalApplicationItems).build();
        details.getData().put(GENERAL_APPLICATION_COLLECTION, generalApplications);
        when(helper.getApplicationItems(callbackRequest.getCaseDetails().getData(),
            AUTH_TOKEN, callbackRequest.getCaseDetails().getId().toString())).thenReturn(
            callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper()
                .getGeneralApplications().getFirst().getValue());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> startHandle = startHandler.handle(callbackRequest, AUTH_TOKEN);
        FinremCaseData finremCaseData = startHandle.getData();
        finremCaseData.getGeneralApplicationWrapper().setGeneralApplicationDirectionsList(dynamicListForCaseDetails);
        DynamicList dynamicList = objectToDynamicList(finremCaseData.getGeneralApplicationWrapper().getGeneralApplicationDirectionsList());

        assertEquals(1, dynamicList.getListItems().size());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> submitHandle = aboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);
        FinremCaseData data = submitHandle.getData();

        List<GeneralApplicationsCollection> applications = data.getGeneralApplicationWrapper().getGeneralApplications();
        Assertions.assertThat(applications).hasSize(2);

        assertEquals(DIRECTION_OTHER.getId(),
            applications.get(1).getValue().getGeneralApplicationStatus());
        assertNull(data.getGeneralApplicationWrapper().getGeneralApplicationDirectionsList());
        assertNull(data.getGeneralApplicationWrapper().getGeneralApplicationDirectionsDocument());
    }

    @Test
    void givenCase_whenApproveAnApplication_thenUpdateStatusApprovedCompletedAndReturnToPostState() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest();
        when(service.getEventPostState(any(), any())).thenReturn(PREPARE_FOR_HEARING_STATE);
        callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper().getGeneralApplications().forEach(
            ga -> ga.getValue().setGeneralApplicationStatus(DIRECTION_APPROVED.getId()));
        DynamicList dynamicListForCaseDetails = DynamicList.builder().build();
        DynamicListElement listElement = DynamicListElement.builder()
            .label("General Application 1 - Received from - applicant - Created Date 2023-06-13 -Hearing Required - No")
            .code("46132d38-259b-467e-bd4e-e4d7431e32f0#Not Approved").build();
        dynamicListForCaseDetails.setValue(listElement);
        List<DynamicListElement> listItems = new ArrayList<>();
        listItems.add(listElement);
        dynamicListForCaseDetails.setListItems(listItems);
        CaseDetails details = buildCaseDetailsFromJson();
        GeneralApplicationItems generalApplicationItems =
            GeneralApplicationItems.builder().generalApplicationSender(buildDynamicIntervenerList())
                .generalApplicationCreatedBy("Claire Mumford")
                .generalApplicationHearingRequired("Yes").generalApplicationTimeEstimate("24 hours")
                .generalApplicationSpecialMeasures("Special measure").build();
        GeneralApplicationsCollection generalApplications = GeneralApplicationsCollection.builder()
            .value(generalApplicationItems).build();
        details.getData().put(GENERAL_APPLICATION_COLLECTION, generalApplications);
        when(helper.getApplicationItems(callbackRequest.getCaseDetails().getData(),
            AUTH_TOKEN, callbackRequest.getCaseDetails().getId().toString())).thenReturn(
            callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper()
                .getGeneralApplications().getFirst().getValue());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> startHandle = startHandler.handle(callbackRequest, AUTH_TOKEN);
        FinremCaseData finremCaseData = startHandle.getData();
        finremCaseData.getGeneralApplicationWrapper().setGeneralApplicationDirectionsList(dynamicListForCaseDetails);
        DynamicList dynamicList = objectToDynamicList(finremCaseData.getGeneralApplicationWrapper().getGeneralApplicationDirectionsList());
        assertEquals(1, dynamicList.getListItems().size());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> submitHandle = aboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);
        FinremCaseData data = submitHandle.getData();

        List<GeneralApplicationsCollection> applications = data.getGeneralApplicationWrapper().getGeneralApplications();
        Assertions.assertThat(applications).hasSize(2);

        assertEquals(DIRECTION_APPROVED.getId(),
            applications.get(1).getValue().getGeneralApplicationStatus());
        assertNull(data.getGeneralApplicationWrapper().getGeneralApplicationDirectionsList());
        assertNull(data.getGeneralApplicationWrapper().getGeneralApplicationDirectionsList());
        assertEquals(PREPARE_FOR_HEARING_STATE, submitHandle.getState());
        verify(service).submitCollectionGeneralApplicationDirections(any(), any(), any());
    }

    private DynamicRadioList buildDynamicIntervenerList() {

        List<DynamicRadioListElement> dynamicListElements = List.of(getDynamicListElement(APPLICANT, APPLICANT),
            getDynamicListElement(RESPONDENT, RESPONDENT),
            getDynamicListElement(CASE_LEVEL_ROLE, CASE_LEVEL_ROLE)
        );
        return DynamicRadioList.builder()
            .value(dynamicListElements.getFirst())
            .listItems(dynamicListElements)
            .build();
    }

    private DynamicRadioListElement getDynamicListElement(String code, String label) {
        return DynamicRadioListElement.builder()
            .code(code)
            .label(label)
            .build();
    }

    private FinremCallbackRequest buildFinremCallbackRequest() {
        GeneralApplicationItems generalApplicationItems =
            GeneralApplicationItems.builder().generalApplicationSender(buildDynamicIntervenerList())
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
        CaseDocument caseDocument2 = CaseDocument.builder().documentFilename("app_docs.pdf")
            .documentUrl("http://dm-store/documents/b067a2dd-657a-4ed2-98c3-9c3159d1482e")
            .documentBinaryUrl("http://dm-store/documents/b067a2dd-657a-4ed2-98c3-9c3159d1482e/binary").build();
        GeneralApplicationItems generalApplicationItemsAdded =
            GeneralApplicationItems.builder().generalApplicationSender(buildDynamicIntervenerList())
                .generalApplicationDraftOrder(caseDocument1)
                .generalApplicationDirectionsDocument(caseDocument2).generalApplicationDocument(caseDocument1)
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

    private DynamicList objectToDynamicList(Object object) {
        if (object != null) {
            return objectMapper.registerModule(new JavaTimeModule()).convertValue(object, DynamicList.class);
        }
        return null;
    }

    private CaseDetails buildCaseDetailsFromJson() {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(GeneralApplicationDirectionsNewEventAboutToSubmitHandlerTest.GA_JSON)) {
            return objectMapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
