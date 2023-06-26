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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.GeneralApplicationStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.GeneralApplicationStatus.DIRECTION_APPROVED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_LEVEL_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;

@RunWith(MockitoJUnitRunner.class)
public class GeneralApplicationAboutToStartHandlerTest extends BaseHandlerTest {

    private GeneralApplicationAboutToStartHandler handler;
    @Mock
    private AssignCaseAccessService assignCaseAccessService;
    @Mock
    private GenericDocumentService service;
    private ObjectMapper objectMapper;
    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;
    private GeneralApplicationHelper helper;
    @Mock
    private GeneralApplicationService generalApplicationService;

    public static final String AUTH_TOKEN = "tokien:)";

    @Before
    public void setup() {
        objectMapper = new ObjectMapper();
        helper = new GeneralApplicationHelper(objectMapper, service);
        handler = new GeneralApplicationAboutToStartHandler(finremCaseDetailsMapper, helper,
            generalApplicationService, assignCaseAccessService);
    }

    @Test
    public void canHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.GENERAL_APPLICATION),
            is(true));
    }

    @Test
    public void canNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.GENERAL_APPLICATION),
            is(false));
    }

    @Test
    public void canNotHandleWrongEventType() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.CLOSE),
            is(false));
    }

    @Test
    public void canNotHandleWrongCallbackType() {
        assertThat(handler
                .canHandle(CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.GENERAL_APPLICATION),
            is(false));
    }

    @Test
    public void givenCase_whenExistingGeneApp_thenSetcreatedBy() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        when(assignCaseAccessService.getActiveUser(callbackRequest.getCaseDetails().getId().toString(), AUTH_TOKEN)).thenReturn("Case");
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData caseData = handle.getData();
        List<GeneralApplicationCollectionData> generalApplicationList = helper.getGeneralApplicationList(caseData, GENERAL_APPLICATION_COLLECTION);
        assertData(caseData, generalApplicationList.get(1).getGeneralApplicationItems());
    }

    @Test
    public void givenContestedCase_whenExistingGeneAppAndDirectionGiven_thenMigrateToCollection() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseData data = callbackRequest.getCaseDetails().getData();
        data.getGeneralApplicationWrapper().setGeneralApplicationOutcome(GeneralApplicationOutcome.APPROVED);
        data.getGeneralApplicationWrapper().setGeneralApplicationDirectionsHearingRequired(YesOrNo.YES);
        when(assignCaseAccessService.getActiveUser(callbackRequest.getCaseDetails().getId().toString(), AUTH_TOKEN)).thenReturn("Case");
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData caseData = handle.getData();
        List<GeneralApplicationCollectionData> generalApplicationList = helper.getGeneralApplicationList(caseData, GENERAL_APPLICATION_COLLECTION);
        assertData(data, generalApplicationList.get(1).getGeneralApplicationItems());
    }

    private void assertData(FinremCaseData caseData, GeneralApplicationItems generalApplicationItems) {
        assertEquals("Applicant", generalApplicationItems.getGeneralApplicationReceivedFrom().getValue().getCode());
        assertEquals("Applicant", generalApplicationItems.getGeneralApplicationReceivedFrom().getValue().getLabel());
        assertEquals("Claire Mumford", generalApplicationItems.getGeneralApplicationCreatedBy());
        assertEquals("No", generalApplicationItems.getGeneralApplicationHearingRequired());
        String directionGiven = Objects.toString(caseData.getGeneralApplicationWrapper().getGeneralApplicationDirectionsHearingRequired(), null);
        assertEquals(directionGiven == null
                ? GeneralApplicationStatus.CREATED.getId() : DIRECTION_APPROVED.getId(),
            generalApplicationItems.getGeneralApplicationStatus());
        System.out.println(generalApplicationItems.getGeneralApplicationStatus());
        assertNull(generalApplicationItems.getGeneralApplicationTimeEstimate());
        assertNull(generalApplicationItems.getGeneralApplicationSpecialMeasures());
        CaseDocument generalApplicationDocument = generalApplicationItems.getGeneralApplicationDocument();
        assertNotNull(generalApplicationDocument);
        assertEquals("http://dm-store/documents/b067a2dd-657a-4ed2-98c3-9c3159d1482e",
            generalApplicationDocument.getDocumentUrl());
        assertEquals("InterimHearingNotice.pdf",
            generalApplicationDocument.getDocumentFilename());
        assertEquals("http://dm-store/documents/b067a2dd-657a-4ed2-98c3-9c3159d1482e/binary",
            generalApplicationDocument.getDocumentBinaryUrl());
        CaseDocument generalApplicationDraftOrderDocument = generalApplicationItems.getGeneralApplicationDocument();
        assertNotNull(generalApplicationDraftOrderDocument);
        assertEquals("http://dm-store/documents/b067a2dd-657a-4ed2-98c3-9c3159d1482e",
            generalApplicationDraftOrderDocument.getDocumentUrl());
        assertEquals("InterimHearingNotice.pdf",
            generalApplicationDraftOrderDocument.getDocumentFilename());
        assertEquals("http://dm-store/documents/b067a2dd-657a-4ed2-98c3-9c3159d1482e/binary",
            generalApplicationDraftOrderDocument.getDocumentBinaryUrl());
        CaseDocument generalApplicationDirectionOrderDocument = generalApplicationItems.getGeneralApplicationDirectionsDocument();
        assertNotNull(generalApplicationDirectionOrderDocument);
        assertEquals("http://dm-store/documents/b067a2dd-657a-4ed2-98c3-9c3159d1482e",
            generalApplicationDirectionOrderDocument.getDocumentUrl());
        assertEquals("InterimHearingNotice.pdf",
            generalApplicationDirectionOrderDocument.getDocumentFilename());
        assertEquals("http://dm-store/documents/b067a2dd-657a-4ed2-98c3-9c3159d1482e/binary",
            generalApplicationDirectionOrderDocument.getDocumentBinaryUrl());
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

    protected FinremCallbackRequest buildCallbackRequest() {
        GeneralApplicationItems generalApplicationItems =
            GeneralApplicationItems.builder().generalApplicationReceivedFrom(
                buildDynamicIntervenerList()).generalApplicationCreatedBy("Claire Mumford")
                .generalApplicationHearingRequired("Yes").generalApplicationTimeEstimate("24 hours")
                .generalApplicationSpecialMeasures("Special measure").generalApplicationCreatedDate(
                    LocalDate.of(2022, 8, 2)).build();
        GeneralApplicationsCollection generalApplications = GeneralApplicationsCollection.builder().build();
        GeneralApplicationsCollection generalApplicationsBefore = GeneralApplicationsCollection.builder().build();
        generalApplications.setValue(generalApplicationItems);
        generalApplicationsBefore.setId(UUID.randomUUID());
        generalApplications.setId(UUID.randomUUID());
        CaseDocument caseDocument = CaseDocument.builder().documentFilename("InterimHearingNotice.pdf")
            .documentUrl("http://dm-store/documents/b067a2dd-657a-4ed2-98c3-9c3159d1482e")
            .documentBinaryUrl("http://dm-store/documents/b067a2dd-657a-4ed2-98c3-9c3159d1482e/binary").build();
        GeneralApplicationItems generalApplicationItemsAdded =
            GeneralApplicationItems.builder().generalApplicationDocument(caseDocument).generalApplicationDraftOrder(caseDocument)
                .generalApplicationDirectionsDocument(caseDocument).generalApplicationReceivedFrom(buildDynamicIntervenerList())
                .generalApplicationCreatedBy("Claire Mumford")
                .generalApplicationStatus(String.valueOf(DIRECTION_APPROVED)).generalApplicationHearingRequired("No")
                .generalApplicationCreatedDate(LocalDate.now()).build();
        generalApplicationsBefore.setValue(generalApplicationItemsAdded);
        List<GeneralApplicationsCollection> generalApplicationsCollection = new ArrayList<>();
        List<GeneralApplicationsCollection> generalApplicationsCollectionBefore = new ArrayList<>();
        generalApplicationsCollectionBefore.add(generalApplications);
        generalApplicationsCollection.add(generalApplications);
        generalApplicationsCollection.add(generalApplicationsBefore);
        FinremCaseData caseData = FinremCaseData.builder()
            .generalApplicationWrapper(GeneralApplicationWrapper.builder().generalApplicationDirectionsHearingRequired(YesOrNo.YES)
                .generalApplicationCreatedBy("Claire Mumford").generalApplicationPreState("applicationIssued")
                .generalApplications(generalApplicationsCollection).generalApplicationOutcome(GeneralApplicationOutcome.APPROVED)
                .build()).build();
        FinremCaseData caseDataBefore = FinremCaseData.builder()
            .generalApplicationWrapper(GeneralApplicationWrapper.builder()
                .generalApplicationCreatedBy("Claire Mumford").generalApplicationOutcome(GeneralApplicationOutcome.APPROVED)
                .generalApplicationPreState("applicationIssued")
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