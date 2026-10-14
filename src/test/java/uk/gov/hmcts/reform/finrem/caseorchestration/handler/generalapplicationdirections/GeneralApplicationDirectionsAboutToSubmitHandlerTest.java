package uk.gov.hmcts.reform.finrem.caseorchestration.handler.generalapplicationdirections;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ContactDetailsValidator;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.GeneralApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.managehearings.HearingCorrespondenceHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationItems;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationOutcome;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs.HearingTabItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.notifications.NotificationAudit;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationDirectionsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PartyService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.managehearing.ManageHearingsCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.GeneralApplicationsCategoriser;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.ManageHearingActionService;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestObjectMapperFactory.createObjectMapper;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.verifyTemporaryFieldsWereSanitised;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.ContactDetailsValidator.validateRequiredIntervenersPostalAddresses;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.ContactDetailsValidator.validateRequiredPostalAddresses;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.GENERAL_APPLICATION_DIRECTIONS_MH;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.GeneralApplicationStatus.DIRECTION_APPROVED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.GeneralApplicationStatus.DIRECTION_NOT_APPROVED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.GeneralApplicationStatus.DIRECTION_OTHER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_LEVEL_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PREPARE_FOR_HEARING_STATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.notifications.NotificationType.POSTAL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.INTERVENER_FOUR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.INTERVENER_ONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.INTERVENER_THREE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.INTERVENER_TWO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class GeneralApplicationDirectionsAboutToSubmitHandlerTest {

    @InjectMocks
    private GeneralApplicationDirectionsAboutToStartHandler startHandler;
    @InjectMocks
    private GeneralApplicationDirectionsAboutToSubmitHandler aboutToSubmitHandler;
    @Mock
    private GeneralApplicationHelper generalApplicationHelper;
    @Mock
    private AssignCaseAccessService assignCaseAccessService;
    @Mock
    private GeneralApplicationDirectionsService generalApplicationDirectionsService;
    @Mock
    private GeneralApplicationService gaService;
    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;
    @Mock
    private GeneralApplicationsCategoriser generalApplicationsCategoriser;
    @Mock
    private ManageHearingActionService manageHearingActionService;
    @Mock
    private HearingCorrespondenceHelper hearingCorrespondenceHelper;
    @Mock
    private PartyService partyService;
    @Mock
    private ManageHearingsCorresponder manageHearingsCorresponder;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private ObjectMapper objectMapper;

    private static final String GA_JSON = "/fixtures/contested/general-application-details.json";

    private MockedStatic<ContactDetailsValidator> contactDetailsValidatorMockedStatic;

    @BeforeEach
    void setup() {
        objectMapper = createObjectMapper();

        DynamicMultiSelectList dynamicMultiSelectList = DynamicMultiSelectList.builder().listItems(
            List.of()
        ).build();
        lenient().when(partyService.getAllActivePartyList(any(FinremCaseDetails.class)))
            .thenReturn(dynamicMultiSelectList);

        contactDetailsValidatorMockedStatic = mockStatic(ContactDetailsValidator.class);
        contactDetailsValidatorMockedStatic
            .when(() -> validateRequiredPostalAddresses(any(FinremCaseData.class), eq(GENERAL_APPLICATION_DIRECTIONS_MH)))
            .thenReturn(new ArrayList<>());

        lenient().when(generalApplicationHelper.objectToDynamicList(any())).thenReturn(
            DynamicList.builder().value(DynamicListElement.builder().code("123#456").build()).build()
        );
    }

    @AfterEach
    void tearDown() {
        contactDetailsValidatorMockedStatic.close();
    }

    @Test
    void testCanHandle() {
        assertCanHandle(aboutToSubmitHandler, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, GENERAL_APPLICATION_DIRECTIONS_MH);
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
        callbackRequest.setEventType(GENERAL_APPLICATION_DIRECTIONS_MH);

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

        when(generalApplicationHelper.getApplicationItems(callbackRequest.getCaseDetails(), AUTH_TOKEN)).thenReturn(generalApplicationItems);

        // Act
        var startHandle = startHandler.handle(callbackRequest, AUTH_TOKEN);
        FinremCaseData caseData = startHandle.getData();
        FinremCaseData finremCaseData = startHandle.getData();

        finremCaseData.getGeneralApplicationWrapper().setGeneralApplicationDirectionsList(dynamicListForCaseDetails);

        DynamicList dynamicList = objectToDynamicList(caseData.getGeneralApplicationWrapper().getGeneralApplicationDirectionsList());
        assertThat(dynamicList.getListItems()).hasSize(1);

        String collectionId = UUID.randomUUID().toString();

        when(generalApplicationHelper.getGeneralApplicationList(any(), any())).thenReturn(
            List.of(GeneralApplicationCollectionData.builder()
                .id(collectionId)
                .generalApplicationItems(generalApplicationItems)
                .build()));
        when(generalApplicationHelper.objectToDynamicList(any())).thenReturn(dynamicList);
        when(generalApplicationDirectionsService.isHearingRequired(any(FinremCaseDetails.class))).thenReturn(false); //No hearing required

        doAnswer(invocation -> {
            FinremCaseDetails caseDetails = invocation.getArgument(1);

            caseDetails.getData().getGeneralApplicationWrapper().getGeneralApplications()
                .getFirst()
                .setValue(generalApplicationItems);

            return null;
        }).when(gaService).updateGeneralApplicationCollectionData(any(), any());

        var submitHandle = aboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);
        FinremCaseData data = submitHandle.getData();

        // Assert
        List<GeneralApplicationsCollection> applications = data.getGeneralApplicationWrapper().getGeneralApplications();

        assertThat(applications).hasSize(1);
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

        CaseDocument generalApplicationDocument = caseDocument("GeneralApplicationDocument.pdf", "url", "binaryUrl");
        CaseDocument generalApplicationDraftOrder = caseDocument("GeneralApplicationDraft.pdf", "url", "binaryUrl");
        GeneralApplicationItems generalApplicationItems =
            GeneralApplicationItems.builder()
                .generalApplicationSender(buildDynamicIntervenerList())
                .generalApplicationDocument(generalApplicationDocument)
                .generalApplicationCreatedBy("Claire Mumford")
                .generalApplicationHearingRequired("Yes").generalApplicationTimeEstimate("24 hours")
                .generalApplicationSpecialMeasures("Special measure")
                .generalApplicationDraftOrder(generalApplicationDraftOrder)
                .build();

        when(generalApplicationHelper.getApplicationItems(callbackRequest.getCaseDetails(), AUTH_TOKEN)).thenReturn(generalApplicationItems);
        when(generalApplicationDirectionsService.isHearingRequired(callbackRequest.getCaseDetails())).thenReturn(true); //Hearing is required
        when(generalApplicationHelper.getPdfDocument(any(CaseDocument.class), any(String.class), any(CaseType.class)))
            .thenReturn(generalApplicationDocument, generalApplicationDraftOrder);

        //Hearing required document
        Optional<CaseDocument> generatedDocument = Optional.of(CaseDocument.builder().documentFilename("HearingNotice.pdf")
            .documentUrl("http://dm-store/documents/b067a2dd-657a-4ed2-98c3-9c3159d1482e")
            .documentBinaryUrl("http://dm-store/documents/b067a2dd-657a-4ed2-98c3-9c3159d1482e/binary").build());
        when(generalApplicationDirectionsService.generateGeneralApplicationDirectionsDocumentIfNeeded(eq(AUTH_TOKEN), any(FinremCaseDetails.class)))
            .thenReturn(generatedDocument);

        var startHandle = startHandler.handle(callbackRequest, AUTH_TOKEN);
        FinremCaseData caseData = startHandle.getData();
        FinremCaseData finremCaseData = startHandle.getData();
        finremCaseData.getGeneralApplicationWrapper().setGeneralApplicationDirectionsList(dynamicListForCaseDetails);
        DynamicList dynamicList = objectToDynamicList(caseData.getGeneralApplicationWrapper().getGeneralApplicationDirectionsList());
        assertThat(dynamicList.getListItems())
            .hasSize(1);

        String collectionId = UUID.randomUUID().toString();

        when(generalApplicationHelper.mapExistingGeneralApplicationToData(callbackRequest.getCaseDetails(), AUTH_TOKEN)).thenReturn(
            GeneralApplicationCollectionData.builder()
                .id(collectionId)
                .generalApplicationItems(generalApplicationItems)
                .build());

        when(hearingCorrespondenceHelper.getHearingInContextFromTab(any())).thenReturn(buildHearingTabItem());

        var aboutToSubmitHandle = aboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);
        FinremCaseData data = aboutToSubmitHandle.getData();

        List<GeneralApplicationsCollection> applications = data.getGeneralApplicationWrapper().getGeneralApplications();
        assertThat(applications).hasSize(1);
        verify(generalApplicationDirectionsService).isHearingRequired(callbackRequest.getCaseDetails());
        verify(generalApplicationHelper, times(2)).getPdfDocument(any(CaseDocument.class), any(String.class), any(CaseType.class));
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
        when(generalApplicationHelper.getApplicationItems(callbackRequest.getCaseDetails(), AUTH_TOKEN)).thenReturn(
            callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper()
                .getGeneralApplications().getFirst().getValue());

        var startHandle = startHandler.handle(callbackRequest, AUTH_TOKEN);
        FinremCaseData caseData = startHandle.getData();
        caseData.getGeneralApplicationWrapper().setGeneralApplicationDirectionsList(dynamicListForCaseDetails);
        DynamicList dynamicList = objectToDynamicList(caseData.getGeneralApplicationWrapper().getGeneralApplicationDirectionsList());
        assertThat(dynamicList.getListItems()).hasSize(1);

        var submitHandle = aboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);
        FinremCaseData data = submitHandle.getData();

        List<GeneralApplicationsCollection> applications = data.getGeneralApplicationWrapper().getGeneralApplications();
        assertThat(applications).hasSize(2);

        assertThat(applications.get(1).getValue().getGeneralApplicationStatus())
            .isEqualTo(DIRECTION_APPROVED.getId());
        assertNull(caseData.getGeneralApplicationWrapper().getGeneralApplicationDirectionsList());
        assertNull(caseData.getGeneralApplicationWrapper().getGeneralApplicationDirectionsDocument());

        verify(generalApplicationDirectionsService).submitCollectionGeneralApplicationDirections(any(), any(), any());
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
        when(generalApplicationHelper.getApplicationItems(finremCallbackRequest.getCaseDetails(), AUTH_TOKEN)).thenReturn(
            finremCallbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper()
                .getGeneralApplications().getFirst().getValue());

        var startHandle = startHandler.handle(finremCallbackRequest, AUTH_TOKEN);
        FinremCaseData finremCaseData = startHandle.getData();
        finremCaseData.getGeneralApplicationWrapper().setGeneralApplicationDirectionsList(dynamicListForCaseDetails);
        DynamicList dynamicList = objectToDynamicList(finremCaseData.getGeneralApplicationWrapper().getGeneralApplicationDirectionsList());
        assertThat(dynamicList.getListItems())
            .hasSize(1);

        var submitHandle = aboutToSubmitHandler.handle(finremCallbackRequest, AUTH_TOKEN);
        FinremCaseData data = submitHandle.getData();

        List<GeneralApplicationsCollection> applications = data.getGeneralApplicationWrapper().getGeneralApplications();
        assertThat(applications).hasSize(2);

        assertThat(applications.get(1).getValue().getGeneralApplicationStatus())
            .isEqualTo(DIRECTION_NOT_APPROVED.getId());
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
        when(generalApplicationHelper.getApplicationItems(callbackRequest.getCaseDetails(), AUTH_TOKEN)).thenReturn(
            callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper()
                .getGeneralApplications().getFirst().getValue());

        var startHandle = startHandler.handle(callbackRequest, AUTH_TOKEN);
        FinremCaseData finremCaseData = startHandle.getData();
        finremCaseData.getGeneralApplicationWrapper().setGeneralApplicationDirectionsList(dynamicListForCaseDetails);
        DynamicList dynamicList = objectToDynamicList(finremCaseData.getGeneralApplicationWrapper().getGeneralApplicationDirectionsList());

        assertThat(dynamicList.getListItems())
            .hasSize(1);

        var submitHandle = aboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);
        FinremCaseData data = submitHandle.getData();

        List<GeneralApplicationsCollection> applications = data.getGeneralApplicationWrapper().getGeneralApplications();
        assertThat(applications).hasSize(2);

        assertThat(applications.get(1).getValue().getGeneralApplicationStatus())
            .isEqualTo(DIRECTION_OTHER.getId());
        assertNull(data.getGeneralApplicationWrapper().getGeneralApplicationDirectionsList());
        assertNull(data.getGeneralApplicationWrapper().getGeneralApplicationDirectionsDocument());
    }

    @Test
    void givenCase_whenApproveAnApplication_thenUpdateStatusApprovedCompletedAndReturnToPostState() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest();
        when(generalApplicationDirectionsService.getEventPostState(any(), any())).thenReturn(PREPARE_FOR_HEARING_STATE);
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
        when(generalApplicationHelper.getApplicationItems(callbackRequest.getCaseDetails(), AUTH_TOKEN)).thenReturn(
            callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper()
                .getGeneralApplications().getFirst().getValue());

        var startHandle = startHandler.handle(callbackRequest, AUTH_TOKEN);
        FinremCaseData finremCaseData = startHandle.getData();
        finremCaseData.getGeneralApplicationWrapper().setGeneralApplicationDirectionsList(dynamicListForCaseDetails);
        DynamicList dynamicList = objectToDynamicList(finremCaseData.getGeneralApplicationWrapper().getGeneralApplicationDirectionsList());
        assertThat(dynamicList.getListItems())
            .hasSize(1);

        var submitHandle = aboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);
        FinremCaseData data = submitHandle.getData();

        List<GeneralApplicationsCollection> applications = data.getGeneralApplicationWrapper().getGeneralApplications();
        assertThat(applications).hasSize(2);

        assertThat(applications.get(1).getValue().getGeneralApplicationStatus())
            .isEqualTo(DIRECTION_APPROVED.getId());
        assertNull(data.getGeneralApplicationWrapper().getGeneralApplicationDirectionsList());
        assertNull(data.getGeneralApplicationWrapper().getGeneralApplicationDirectionsList());
        assertThat(submitHandle.getState()).isEqualTo(PREPARE_FOR_HEARING_STATE);
        verify(generalApplicationDirectionsService).submitCollectionGeneralApplicationDirections(any(), any(), any());
    }

    /*
     * Opted for reflection to test performAddHearingIfNecessary.  Avoids complex setup to test this private method.
     * Test written to ensure that ManageHearingsAction.ADD_HEARING is set by performAddHearingIfNecessary.
     */
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void givenCase_whenPerformAddHearingIfNecessary_thenCaseWithHearingHasAddHearingAction(boolean isHearingRequired)
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        // Arrange
        ManageHearingsWrapper hearingsWrapper = mock(ManageHearingsWrapper.class);
        FinremCaseData finremCaseData = spy(FinremCaseData.builder().manageHearingsWrapper(hearingsWrapper).build());

        FinremCaseDetails caseDetails = mock(FinremCaseDetails.class);
        lenient().when(caseDetails.getData()).thenReturn(finremCaseData);

        // Act, call performAddHearingIfNecessary using reflection
        Method method = GeneralApplicationDirectionsAboutToSubmitHandler.class.getDeclaredMethod(
            "performAddHearingIfNecessary", FinremCaseDetails.class, boolean.class, String.class);
        method.setAccessible(true);
        method.invoke(aboutToSubmitHandler, caseDetails, isHearingRequired, AUTH_TOKEN);

        // Assert
        if (isHearingRequired) {
            assertAll(
                () -> verify(hearingsWrapper).setManageHearingsActionSelection(ManageHearingsAction.ADD_HEARING),
                () -> verify(manageHearingActionService).performAddHearing(caseDetails, AUTH_TOKEN),
                () -> verify(manageHearingActionService).updateTabData(caseDetails.getData())
            );
        } else {
            assertAll(
                () -> verify(hearingsWrapper, never()).setManageHearingsActionSelection(any(ManageHearingsAction.class)),
                () -> verify(manageHearingActionService, never()).performAddHearing(any(FinremCaseDetails.class), anyString()),
                () -> verify(manageHearingActionService, never()).updateTabData(any(FinremCaseData.class))
            );
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void givenIsHearingRequired_whenHandled_shouldValidateApplicantOrRespondentAddress(boolean isHearingRequired) {
        contactDetailsValidatorMockedStatic
            .when(() -> validateRequiredPostalAddresses(any(FinremCaseData.class), eq(GENERAL_APPLICATION_DIRECTIONS_MH)))
            .thenReturn(new ArrayList<String>(List.of("missing applicant or respondent address")));
        contactDetailsValidatorMockedStatic
            .when(() -> validateRequiredIntervenersPostalAddresses(any(FinremCaseData.class), eq(GENERAL_APPLICATION_DIRECTIONS_MH),
                anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean()))
            .thenReturn(List.of());

        List<GeneralApplicationsCollection> generalApplications = mock(List.class);
        GeneralApplicationWrapper generalApplicationWrapper = mock(GeneralApplicationWrapper.class);
        when(generalApplicationWrapper.getGeneralApplications()).thenReturn(generalApplications);
        FinremCaseData finremCaseData = spy(FinremCaseData.builder()
            .generalApplicationWrapper(generalApplicationWrapper)
            .build());
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(finremCaseData);

        when(generalApplicationDirectionsService.isHearingRequired(callbackRequest.getCaseDetails())).thenReturn(isHearingRequired);

        var response = aboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getErrors()).containsOnly("missing applicant or respondent address");
        verify(generalApplicationHelper).populateGeneralApplicationSender(finremCaseData, generalApplications);
        verify(generalApplicationDirectionsService, never()).submitCollectionGeneralApplicationDirections(any(), any(), any());
    }

    static Stream<Arguments> hearingRequiredAndIntervenerIndex() {
        return Stream.of(true, false).flatMap(isHearingRequired ->
            IntStream.rangeClosed(1, 4).mapToObj(index -> Arguments.of(isHearingRequired, index)));
    }

    @ParameterizedTest(name = "isHearingRequired={0}, intervener={1}")
    @MethodSource("hearingRequiredAndIntervenerIndex")
    void givenIsHearingRequiredWithAnActiveIntervener_whenHandled_shouldValidateIntervenerAddress(
        boolean isHearingRequired, int intervenerIndex) {

        String expectedError = "missing intervener " + intervenerIndex + " address";

        contactDetailsValidatorMockedStatic
            .when(() -> validateRequiredPostalAddresses(any(FinremCaseData.class), eq(GENERAL_APPLICATION_DIRECTIONS_MH)))
            .thenReturn(new ArrayList<>());
        contactDetailsValidatorMockedStatic
            .when(() -> validateRequiredIntervenersPostalAddresses(any(FinremCaseData.class), eq(GENERAL_APPLICATION_DIRECTIONS_MH),
                eq(intervenerIndex == 1), eq(intervenerIndex == 2), eq(intervenerIndex == 3), eq(intervenerIndex == 4)))
            .thenReturn(List.of(expectedError));

        List<GeneralApplicationsCollection> generalApplications = mock(List.class);
        GeneralApplicationWrapper generalApplicationWrapper = mock(GeneralApplicationWrapper.class);
        when(generalApplicationWrapper.getGeneralApplications()).thenReturn(generalApplications);
        FinremCaseData finremCaseData = spy(FinremCaseData.builder()
            .generalApplicationWrapper(generalApplicationWrapper)
            .build());
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(finremCaseData);

        when(generalApplicationDirectionsService.isHearingRequired(callbackRequest.getCaseDetails())).thenReturn(isHearingRequired);
        SendCorrespondenceEvent event1 = spy(SendCorrespondenceEvent.builder()
            .notificationParties(List.of(intervenerParty(intervenerIndex)))
            .audits(List.of(NotificationAudit.builder().type(POSTAL).build()))
            .build());
        SendCorrespondenceEvent event2 = mock(SendCorrespondenceEvent.class);
        if (isHearingRequired) {
            List<SendCorrespondenceEvent> events = List.of(event1, event2);
            when(manageHearingsCorresponder.buildHearingCorrespondenceEventsIfNeeded(callbackRequest, AUTH_TOKEN))
                .thenReturn(events);
            stubIntervenerActive(finremCaseData, intervenerIndex);
        }

        var response = aboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);

        if (isHearingRequired) {
            assertThat(response.getErrors()).containsOnly(expectedError);
            verify(event1).setSimulatingCorrespondence(true);
            verify(event2).setSimulatingCorrespondence(true);
            verify(applicationEventPublisher).publishEvent(event1);
            verify(applicationEventPublisher).publishEvent(event2);
            verify(generalApplicationDirectionsService, never()).submitCollectionGeneralApplicationDirections(any(), any(), any());
        } else {
            assertThat(response.getErrors()).isEmpty();
            verify(event1, never()).setSimulatingCorrespondence(anyBoolean());
            verify(event2, never()).setSimulatingCorrespondence(anyBoolean());
            verifyNoInteractions(applicationEventPublisher);
            verify(generalApplicationDirectionsService).submitCollectionGeneralApplicationDirections(
                eq(callbackRequest.getCaseDetails()), anyList(), anyString());
        }
        verify(generalApplicationHelper).populateGeneralApplicationSender(finremCaseData, generalApplications);
    }

    @Test
    void shouldRemoveGadPreviewWhenHandled() {
        when(generalApplicationHelper.objectToDynamicList(any())).thenReturn(DynamicList.builder()
                .value(DynamicListElement.builder().code("a#b").build())
            .build());

        final CaseDocument caseDocument = caseDocument();

        verifyTemporaryFieldsWereSanitised(aboutToSubmitHandler,
            finremCaseDetailsMapper, new HashMap<>(Map.of(
                "generalApplicationDirectionsPreview", Map.of(
                    "document_url", caseDocument.getDocumentUrl(),
                    "document_binary_url", caseDocument.getDocumentBinaryUrl(),
                    "document_filename", caseDocument.getDocumentFilename()
                )
            ))
        );
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
        try (InputStream resourceAsStream = getClass().getResourceAsStream(GeneralApplicationDirectionsAboutToSubmitHandlerTest.GA_JSON)) {
            return objectMapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private HearingTabItem buildHearingTabItem() {
        return HearingTabItem.builder()
            .tabDateTime("2023-06-20T10:00:00")
            .tabHearingType("Application Hearing")
            .tabHearingDocuments(List.of(DocumentCollectionItem
                .fromCaseDocument(
                    CaseDocument.builder()
                        .documentFilename("Hearing Notice.pdf")
                        .categoryId("Hearing Notices")
                        .documentBinaryUrl("http://some-url/binary")
                        .build())
            ))
            .build();
    }

    private static NotificationParty intervenerParty(int index) {
        return switch (index) {
            case 1 -> INTERVENER_ONE;
            case 2 -> INTERVENER_TWO;
            case 3 -> INTERVENER_THREE;
            case 4 -> INTERVENER_FOUR;
            default -> throw new IllegalArgumentException("Unsupported intervener index: " + index);
        };
    }

    private void stubIntervenerActive(FinremCaseData caseData, int index) {
        switch (index) {
            case 1 -> when(partyService.isIntervenerOneActive(caseData)).thenReturn(true);
            case 2 -> when(partyService.isIntervenerTwoActive(caseData)).thenReturn(true);
            case 3 -> when(partyService.isIntervenerThreeActive(caseData)).thenReturn(true);
            case 4 -> when(partyService.isIntervenerFourActive(caseData)).thenReturn(true);
            default -> throw new IllegalArgumentException("Unsupported intervener index: " + index);
        }
    }
}
