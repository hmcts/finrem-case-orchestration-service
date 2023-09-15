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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationItems;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_LEVEL_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;

@RunWith(MockitoJUnitRunner.class)
public class GeneralApplicationAboutToSubmitHandlerTest {

    private GeneralApplicationAboutToSubmitHandler handler;
    @Mock
    private GenericDocumentService documentService;
    private ObjectMapper objectMapper;
    private FinremCaseDetailsMapper finremCaseDetailsMapper;
    private GeneralApplicationHelper helper;

    @Mock
    private GeneralApplicationService service;

    public static final String AUTH_TOKEN = "tokien:)";

    @Before
    public void setup() {
        objectMapper = new ObjectMapper();
        helper = new GeneralApplicationHelper(objectMapper, documentService);
        handler = new GeneralApplicationAboutToSubmitHandler(finremCaseDetailsMapper, helper, service);
    }

    @Test
    public void canHandle() {
        assertThat(handler
                        .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.GENERAL_APPLICATION),
                is(true));
    }

    @Test
    public void canNotHandle() {
        assertThat(handler
                        .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.GENERAL_APPLICATION),
                is(false));
    }

    @Test
    public void canNotHandleWrongEventType() {
        assertThat(handler
                        .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.CLOSE),
                is(false));
    }

    @Test
    public void canNotHandleWrongCallbackType() {
        assertThat(handler
                        .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.GENERAL_APPLICATION),
                is(false));
    }

    @Test
    public void givenCase_whenExistingGeneApp_thenSetCreatedBy() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();

        List<GeneralApplicationCollectionData> existingGeneralApplication = new ArrayList<>();
        existingGeneralApplication.add(migrateExistingGeneralApplication(callbackRequest.getCaseDetails().getData()));
        callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper().setGeneralApplications(
                helper.convertToGeneralApplicationsCollection(existingGeneralApplication));

        when(service.updateGeneralApplications(callbackRequest, AUTH_TOKEN)).thenReturn(callbackRequest.getCaseDetails().getData());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData caseData = handle.getData();
        List<GeneralApplicationCollectionData> generalApplicationList =
                helper.getGeneralApplicationList(caseData, GENERAL_APPLICATION_COLLECTION);
        assertEquals(1, generalApplicationList.size());
        assertExistingGeneralApplication(caseData);
    }

    @Test
    public void sortGeneralApplicationListByLatestDate() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();

        List<GeneralApplicationCollectionData> unsortedList =
                helper.getGeneralApplicationList(callbackRequest.getCaseDetails().getData(), GENERAL_APPLICATION_COLLECTION);

        List<GeneralApplicationCollectionData> applicationCollectionDataList = unsortedList.stream()
                .sorted(helper::getCompareTo)
                .collect(Collectors.toList());

        FinremCaseData data = callbackRequest.getCaseDetails().getData();
        data.getGeneralApplicationWrapper().setGeneralApplications(helper.convertToGeneralApplicationsCollection(applicationCollectionDataList));

        when(service.updateGeneralApplications(callbackRequest, AUTH_TOKEN)).thenReturn(data);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData caseData = handle.getData();
        List<GeneralApplicationCollectionData> generalApplicationList = helper.getGeneralApplicationList(caseData, GENERAL_APPLICATION_COLLECTION);
        assertEquals(2, generalApplicationList.size());
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

    private GeneralApplicationCollectionData migrateExistingGeneralApplication(FinremCaseData caseData) {
        return GeneralApplicationCollectionData.builder()
                .id(UUID.randomUUID().toString())
                .generalApplicationItems(getApplicationItems(caseData))
                .build();
    }

    private GeneralApplicationItems getApplicationItems(FinremCaseData caseData) {
        GeneralApplicationItems.GeneralApplicationItemsBuilder builder =
                GeneralApplicationItems.builder();
        builder.generalApplicationSender(buildDynamicIntervenerList());
        builder.generalApplicationCreatedBy(Objects.toString(
                caseData.getGeneralApplicationWrapper().getGeneralApplicationCreatedBy(), null));
        builder.generalApplicationHearingRequired(Objects.toString(
                caseData.getGeneralApplicationWrapper().getGeneralApplicationHearingRequired(), null));
        builder.generalApplicationTimeEstimate(Objects.toString(
                caseData.getGeneralApplicationWrapper().getGeneralApplicationTimeEstimate(), null));
        builder.generalApplicationSpecialMeasures(Objects.toString(
                caseData.getGeneralApplicationWrapper().getGeneralApplicationSpecialMeasures(), null));
        builder.generalApplicationDocument(helper.convertToCaseDocument(
                caseData.getGeneralApplicationWrapper().getGeneralApplicationDocument()));
        CaseDocument draftDocument = helper.convertToCaseDocument(
                caseData.getGeneralApplicationWrapper().getGeneralApplicationDraftOrder());
        builder.generalApplicationCreatedDate(helper.objectToDateTime(
                caseData.getGeneralApplicationWrapper().getGeneralApplicationLatestDocumentDate()));
        builder.generalApplicationDraftOrder(draftDocument);
        return builder.build();
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
                GeneralApplicationItems.builder().generalApplicationSender(buildDynamicIntervenerList())
                        .generalApplicationCreatedBy("Claire Mumford")
                        .generalApplicationHearingRequired("Yes").generalApplicationTimeEstimate("24 hours")
                        .generalApplicationSpecialMeasures("Special measure").generalApplicationCreatedDate(
                                LocalDate.of(2022, 8, 2)).build();
        GeneralApplicationsCollection generalApplications = GeneralApplicationsCollection.builder().build();
        GeneralApplicationsCollection generalApplicationsBefore = GeneralApplicationsCollection.builder().build();
        generalApplications.setValue(generalApplicationItems);
        generalApplicationsBefore.setId(UUID.randomUUID());
        generalApplications.setId(UUID.randomUUID());
        GeneralApplicationItems generalApplicationItemsAdded =
                GeneralApplicationItems.builder().generalApplicationSender(buildDynamicIntervenerList())
                        .generalApplicationCreatedBy("Claire Mumford")
                        .generalApplicationHearingRequired("No").generalApplicationTimeEstimate("48 hours")
                        .generalApplicationSpecialMeasures("Special measure").generalApplicationCreatedDate(LocalDate.now()).build();
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