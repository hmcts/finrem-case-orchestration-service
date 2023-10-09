package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFourWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThreeWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwoWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_RESP_GENERAL_APPLICATION_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_LEVEL_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER1_GENERAL_APPLICATION_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER2_GENERAL_APPLICATION_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER3;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER3_GENERAL_APPLICATION_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER4;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER4_GENERAL_APPLICATION_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;

@RunWith(MockitoJUnitRunner.class)
public class GeneralApplicationHelperTest {

    public static final String AUTH_TOKEN = "tokien:)";
    @Mock
    private GenericDocumentService service;
    private String caseId = "123123123";
    private ObjectMapper objectMapper;
    @Mock
    FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Test
    public void givenDate_whenOjectToDateTimeIsNotNull_thenReturnLocalDate() {
        FinremCallbackRequest callbackRequest = callbackRequest();
        FinremCaseData data = callbackRequest.getCaseDetails().getData();
        data.getGeneralOrderWrapper().setGeneralOrderDate(LocalDate.of(2022, 10, 10));
        GeneralApplicationHelper helper = new GeneralApplicationHelper(new ObjectMapper(), service);
        assertEquals(helper.objectToDateTime(data.getGeneralOrderWrapper().getGeneralOrderDate()), LocalDate.of(2022, 10, 10));
    }

    @Test
    public void givenDate_whenOjectToDateTimeIsNull_thenReturnNull() {
        FinremCallbackRequest callbackRequest = callbackRequest();
        FinremCaseData data = callbackRequest.getCaseDetails().getData();
        GeneralApplicationHelper helper = new GeneralApplicationHelper(new ObjectMapper(), service);
        assertNull(helper.objectToDateTime(data.getGeneralOrderWrapper().getGeneralOrderDate()));
    }

    @Test
    public void givenContestedCase_whenMigratingExistingGeneralApplicationAndCreatedByIsNull_thenReturnNull() {
        FinremCallbackRequest callbackRequest = callbackRequest();
        FinremCaseData data = callbackRequest.getCaseDetails().getData();
        data.getGeneralApplicationWrapper().setGeneralApplicationCreatedBy(null);
        GeneralApplicationHelper helper = new GeneralApplicationHelper(new ObjectMapper(), service);
        assertNull(helper.migrateExistingGeneralApplication(data, AUTH_TOKEN, anyString()));
    }

    @Test
    public void givenContestedCase_whenRetrieveInitialGeneralApplicationDataCreatedByIsNull_thenReturnNull() {
        FinremCallbackRequest callbackRequest = callbackRequest();
        FinremCaseData data = callbackRequest.getCaseDetails().getData();
        data.getGeneralApplicationWrapper().setGeneralApplicationCreatedBy(null);
        GeneralApplicationHelper helper = new GeneralApplicationHelper(new ObjectMapper(), service);
        assertNull(helper.retrieveInitialGeneralApplicationData(data, "any", AUTH_TOKEN, anyString()));
    }

    @Test
    public void givenContestedCase_whenRetrieveInitialGeneralApplicationDataCreatedByIsNotNull_thenReturnNull() {
        FinremCallbackRequest callbackRequest = callbackRequest();
        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();

        caseData.getGeneralApplicationWrapper().setGeneralApplicationReceivedFrom(APPLICANT);
        caseData.getGeneralApplicationWrapper().setGeneralApplicationCreatedBy(APPLICANT);
        caseData.getGeneralApplicationWrapper().setGeneralApplicationHearingRequired(YesOrNo.YES);
        caseData.getGeneralApplicationWrapper().setGeneralApplicationTimeEstimate("2 weeks");
        caseData.getGeneralApplicationWrapper().setGeneralApplicationSpecialMeasures("None");
        caseData.getGeneralApplicationWrapper().setGeneralApplicationOutcome(GeneralApplicationOutcome.APPROVED);

        GeneralApplicationHelper helper = new GeneralApplicationHelper(new ObjectMapper(), service);
        GeneralApplicationCollectionData data = helper.retrieveInitialGeneralApplicationData(caseData, "any", AUTH_TOKEN, caseId);
        GeneralApplicationItems items = data.getGeneralApplicationItems();

        assertEquals(APPLICANT, items.getGeneralApplicationCreatedBy());
        assertEquals(APPLICANT, items.getGeneralApplicationSender().getValue().getCode());
        assertEquals(APPLICANT, items.getGeneralApplicationSender().getValue().getLabel());
        assertEquals("YES", items.getGeneralApplicationHearingRequired());
        assertEquals("2 weeks", items.getGeneralApplicationTimeEstimate());
        assertEquals("None", items.getGeneralApplicationSpecialMeasures());
        assertEquals("Approved", items.getGeneralApplicationStatus());

        caseData.getGeneralApplicationWrapper().setGeneralApplicationOutcome(GeneralApplicationOutcome.NOT_APPROVED);
        GeneralApplicationCollectionData dataNotApproved = helper.retrieveInitialGeneralApplicationData(caseData,
            "any", AUTH_TOKEN, caseId);
        GeneralApplicationItems itemsNa = dataNotApproved.getGeneralApplicationItems();
        assertEquals("Not Approved", itemsNa.getGeneralApplicationStatus());

        caseData.getGeneralApplicationWrapper().setGeneralApplicationOutcome(GeneralApplicationOutcome.OTHER);
        GeneralApplicationCollectionData dataOther = helper.retrieveInitialGeneralApplicationData(caseData,
            "any", AUTH_TOKEN, caseId);
        GeneralApplicationItems itemsOther = dataOther.getGeneralApplicationItems();
        assertEquals("Other", itemsOther.getGeneralApplicationStatus());

        caseData.getGeneralApplicationWrapper().setGeneralApplicationDirectionsHearingRequired(YesOrNo.YES);

        caseData.getGeneralApplicationWrapper().setGeneralApplicationOutcome(GeneralApplicationOutcome.APPROVED);
        GeneralApplicationCollectionData dataApproved = helper.retrieveInitialGeneralApplicationData(caseData,
            "any", AUTH_TOKEN, caseId);
        GeneralApplicationItems itemsApp = dataApproved.getGeneralApplicationItems();
        assertEquals("Approved, Completed", itemsApp.getGeneralApplicationStatus());

        caseData.getGeneralApplicationWrapper().setGeneralApplicationOutcome(GeneralApplicationOutcome.NOT_APPROVED);
        GeneralApplicationCollectionData dataNotApproved2 = helper.retrieveInitialGeneralApplicationData(caseData,
            "any", AUTH_TOKEN, caseId);
        GeneralApplicationItems itemsNa2 = dataNotApproved2.getGeneralApplicationItems();
        assertEquals("Not Approved, Completed", itemsNa2.getGeneralApplicationStatus());

        caseData.getGeneralApplicationWrapper().setGeneralApplicationOutcome(GeneralApplicationOutcome.OTHER);
        GeneralApplicationCollectionData dataOther2 = helper.retrieveInitialGeneralApplicationData(caseData,
            "any", AUTH_TOKEN, caseId);
        GeneralApplicationItems itemsOther2 = dataOther2.getGeneralApplicationItems();
        assertEquals("Other, Completed", itemsOther2.getGeneralApplicationStatus());
    }

    @Test
    public void giveCase_whenCaseDocumentIsNotPdf_thenConvertToPdf() {
        CallbackRequest callbackRequest = callbackRequestForCaseDetails();
        Map<String, Object> data = callbackRequest.getCaseDetails().getData();
        GeneralApplicationHelper helper = new GeneralApplicationHelper(new ObjectMapper(), service);
        CaseDocument docxDocment = helper.convertToCaseDocument(callbackRequest.getCaseDetails().getData().get("caseDocument"));
        when(service.convertDocumentIfNotPdfAlready(docxDocment, AUTH_TOKEN, caseId)).thenReturn(caseDocument());
        CaseDocument caseDocument = helper.getPdfDocument(helper.convertToCaseDocument(data.get("caseDocument")), AUTH_TOKEN, caseId);
        assertEquals(FILE_NAME, caseDocument.getDocumentFilename());
    }

    @Test
    public void givenCollectionName_ThenShouldGetRelevantCollectionDataBasedOnIntervener1Role() {
        GeneralApplicationHelper helper = new GeneralApplicationHelper(new ObjectMapper(), service);
        FinremCallbackRequest callbackRequest = callbackRequest();
        FinremCaseData caseData = callbackRequest.getCaseDetailsBefore().getData();
        List<GeneralApplicationsCollection> collection = caseData.getGeneralApplicationWrapper()
            .getGeneralApplications();
        caseData.getGeneralApplicationWrapper().setIntervener1GeneralApplications(collection);
        List<GeneralApplicationItems> resultingList = new ArrayList<>();
        helper.getGeneralApplicationList(caseData, INTERVENER1_GENERAL_APPLICATION_COLLECTION).forEach(
            x -> resultingList.add(x.getGeneralApplicationItems()));
        assertData(resultingList);
    }

    @Test
    public void givenCollectionName_ThenShouldGetRelevantCollectionDataBasedOnIntervener2Role() {
        GeneralApplicationHelper helper = new GeneralApplicationHelper(new ObjectMapper(), service);
        FinremCallbackRequest callbackRequest = callbackRequest();
        FinremCaseData caseData = callbackRequest.getCaseDetailsBefore().getData();
        List<GeneralApplicationsCollection> collection = caseData.getGeneralApplicationWrapper()
            .getGeneralApplications();
        caseData.getGeneralApplicationWrapper().setIntervener2GeneralApplications(collection);
        List<GeneralApplicationItems> resultingList = new ArrayList<>();
        helper.getGeneralApplicationList(caseData, INTERVENER2_GENERAL_APPLICATION_COLLECTION).forEach(
            x -> resultingList.add(x.getGeneralApplicationItems()));
        assertData(resultingList);
    }

    @Test
    public void givenCollectionName_ThenShouldGetRelevantCollectionDataBasedOnIntervener3Role() {
        GeneralApplicationHelper helper = new GeneralApplicationHelper(new ObjectMapper(), service);
        FinremCallbackRequest callbackRequest = callbackRequest();
        FinremCaseData caseData = callbackRequest.getCaseDetailsBefore().getData();
        List<GeneralApplicationsCollection> collection = caseData.getGeneralApplicationWrapper()
            .getGeneralApplications();
        caseData.getGeneralApplicationWrapper().setIntervener3GeneralApplications(collection);
        List<GeneralApplicationItems> resultingList = new ArrayList<>();
        helper.getGeneralApplicationList(caseData, INTERVENER3_GENERAL_APPLICATION_COLLECTION).forEach(
            x -> resultingList.add(x.getGeneralApplicationItems()));
        assertData(resultingList);
    }

    @Test
    public void givenCollectionName_ThenShouldGetRelevantCollectionDataBasedOnIntervener4Role() {
        GeneralApplicationHelper helper = new GeneralApplicationHelper(new ObjectMapper(), service);
        FinremCallbackRequest callbackRequest = callbackRequest();
        FinremCaseData caseData = callbackRequest.getCaseDetailsBefore().getData();
        List<GeneralApplicationsCollection> collection = caseData.getGeneralApplicationWrapper()
            .getGeneralApplications();
        caseData.getGeneralApplicationWrapper().setIntervener4GeneralApplications(collection);
        List<GeneralApplicationItems> resultingList = new ArrayList<>();
        helper.getGeneralApplicationList(caseData, INTERVENER4_GENERAL_APPLICATION_COLLECTION).forEach(
            x -> resultingList.add(x.getGeneralApplicationItems()));
        assertData(resultingList);
    }

    @Test
    public void givenCollectionName_ThenShouldGetRelevantCollectionDataBasedOnApplicantRole() {
        GeneralApplicationHelper helper = new GeneralApplicationHelper(new ObjectMapper(), service);
        FinremCallbackRequest callbackRequest = callbackRequest();
        FinremCaseData caseData = callbackRequest.getCaseDetailsBefore().getData();
        List<GeneralApplicationsCollection> collection = caseData.getGeneralApplicationWrapper()
            .getGeneralApplications();
        caseData.getGeneralApplicationWrapper().setAppRespGeneralApplications(collection);
        List<GeneralApplicationItems> resultingList = new ArrayList<>();
        helper.getGeneralApplicationList(caseData, APP_RESP_GENERAL_APPLICATION_COLLECTION).forEach(
            x -> resultingList.add(x.getGeneralApplicationItems()));
        assertData(resultingList);
    }

    @Test
    public void givenGeneralApplicationReceivedFromFieldSet_ThenShouldUpdateApplicantAndRespondentColl() {
        GeneralApplicationHelper helper = new GeneralApplicationHelper(new ObjectMapper(), service);
        FinremCallbackRequest callbackRequest = callbackRequest();
        FinremCaseData caseData = callbackRequest.getCaseDetailsBefore().getData();
        List<GeneralApplicationsCollection> collection = caseData.getGeneralApplicationWrapper()
            .getGeneralApplications();
        collection.get(0).getValue().setGeneralApplicationReceivedFrom(APPLICANT);
        collection.forEach(x -> x.getValue().setGeneralApplicationSender(null));
        helper.populateGeneralApplicationSender(caseData, collection);
        assertEquals(APPLICANT, caseData.getGeneralApplicationWrapper().getAppRespGeneralApplications().get(0)
                .getValue().getGeneralApplicationSender().getValue().getLabel());
        assertEquals(APPLICANT, caseData.getGeneralApplicationWrapper().getAppRespGeneralApplications().get(0)
                .getValue().getGeneralApplicationSender().getValue().getCode());
        assertNull(caseData.getGeneralApplicationWrapper().getAppRespGeneralApplications().get(0).getValue()
                .getGeneralApplicationReceivedFrom());
    }

    @Test
    public void givenGeneralApplicationReceivedFromFieldNull_ThenShouldUpdateApplicantAndRespondentColl() {
        FinremCallbackRequest callbackRequest = callbackRequest();
        FinremCaseData caseData = callbackRequest.getCaseDetailsBefore().getData();
        List<GeneralApplicationsCollection> collection2 = new ArrayList<>(caseData.getGeneralApplicationWrapper()
                .getGeneralApplications());
        caseData.getGeneralApplicationWrapper().setAppRespGeneralApplications(collection2);
        List<GeneralApplicationsCollection> collection = caseData.getGeneralApplicationWrapper()
                .getGeneralApplications();
        collection.get(0).getValue().setGeneralApplicationReceivedFrom(APPLICANT);
        collection.forEach(x -> x.getValue().setGeneralApplicationSender(null));
        GeneralApplicationHelper helper = new GeneralApplicationHelper(new ObjectMapper(), service);
        helper.populateGeneralApplicationSender(caseData, collection);
        assertEquals(APPLICANT, caseData.getGeneralApplicationWrapper().getAppRespGeneralApplications().get(0)
                .getValue().getGeneralApplicationSender().getValue().getLabel());
        assertEquals(APPLICANT, caseData.getGeneralApplicationWrapper().getAppRespGeneralApplications().get(0)
                .getValue().getGeneralApplicationSender().getValue().getCode());
        assertEquals(APPLICANT, caseData.getGeneralApplicationWrapper().getAppRespGeneralApplications().get(1)
                .getValue().getGeneralApplicationSender().getValue().getLabel());
        assertEquals(APPLICANT, caseData.getGeneralApplicationWrapper().getAppRespGeneralApplications().get(1)
                .getValue().getGeneralApplicationSender().getValue().getCode());
        assertNull(caseData.getGeneralApplicationWrapper().getAppRespGeneralApplications().get(0).getValue()
                .getGeneralApplicationReceivedFrom());
        assertNull(caseData.getGeneralApplicationWrapper().getAppRespGeneralApplications().get(1).getValue()
                .getGeneralApplicationReceivedFrom());
    }

    @Test
    public void givenIntervenersOnCase_ThenShouldAddToDynamicList() {
        FinremCallbackRequest callbackRequest = callbackRequest();
        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();
        IntervenerOneWrapper oneWrapper = IntervenerOneWrapper.builder().intervenerName("firstIntervener").build();
        caseData.setIntervenerOneWrapper(oneWrapper);
        IntervenerTwoWrapper twoWrapper = IntervenerTwoWrapper.builder().intervenerName("secondIntervener").build();
        caseData.setIntervenerTwoWrapper(twoWrapper);
        IntervenerThreeWrapper threeWrapper = IntervenerThreeWrapper.builder().intervenerName("thirdIntervener").build();
        caseData.setIntervenerThreeWrapper(threeWrapper);
        IntervenerFourWrapper fourWrapper = IntervenerFourWrapper.builder().intervenerName("fourthIntervener").build();
        caseData.setIntervenerFourWrapper(fourWrapper);
        GeneralApplicationHelper helper = new GeneralApplicationHelper(new ObjectMapper(), service);
        List<DynamicRadioListElement> dynamicListElements = new ArrayList<>();
        helper.buildDynamicIntervenerList(dynamicListElements, caseData);
        assertEquals(dynamicListElements.get(3).getLabel(), INTERVENER1);
        assertEquals(dynamicListElements.get(4).getLabel(), INTERVENER2);
        assertEquals(dynamicListElements.get(5).getLabel(), INTERVENER3);
        assertEquals(dynamicListElements.get(6).getLabel(), INTERVENER4);
        assertEquals(dynamicListElements.get(3).getCode(), INTERVENER1);
        assertEquals(dynamicListElements.get(4).getCode(), INTERVENER2);
        assertEquals(dynamicListElements.get(5).getCode(), INTERVENER3);
        assertEquals(dynamicListElements.get(6).getCode(), INTERVENER4);
    }

    private void assertData(List<GeneralApplicationItems> resultingList) {
        assertEquals(APPLICANT, resultingList.get(0).getGeneralApplicationSender().getValue().getCode());
        assertEquals(APPLICANT, resultingList.get(0).getGeneralApplicationSender().getValue().getLabel());
        assertEquals("Claire Mumford", resultingList.get(0).getGeneralApplicationCreatedBy());
        assertEquals("Yes", resultingList.get(0).getGeneralApplicationHearingRequired());
        assertEquals("24 hours", resultingList.get(0).getGeneralApplicationTimeEstimate());
        assertEquals("Special measure", resultingList.get(0).getGeneralApplicationSpecialMeasures());
        assertEquals(resultingList.get(0).getGeneralApplicationCreatedDate(),
            LocalDate.of(2022, 8, 2));
    }

    private CallbackRequest callbackRequestForCaseDetails() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("caseDocument", caseDocument(DOC_URL, "app_docs.docx", BINARY_URL));
        return CallbackRequest
            .builder()
            .caseDetails(CaseDetails.builder()
                .data(caseData).build())
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

    public DynamicRadioListElement getDynamicListElement(String code, String label) {
        return DynamicRadioListElement.builder()
            .code(code)
            .label(label)
            .build();
    }

    protected FinremCallbackRequest callbackRequest() {
        GeneralApplicationItems generalApplicationItems =
            GeneralApplicationItems.builder().generalApplicationSender(
                    buildDynamicIntervenerList()).generalApplicationCreatedBy("Claire Mumford")
                .generalApplicationHearingRequired("Yes").generalApplicationTimeEstimate("24 hours")
                .generalApplicationSpecialMeasures("Special measure").generalApplicationCreatedDate(
                    LocalDate.of(2022, 8, 2)).build();
        GeneralApplicationsCollection generalApplications = GeneralApplicationsCollection.builder().build();
        GeneralApplicationsCollection generalApplicationsBefore = GeneralApplicationsCollection.builder().build();
        generalApplications.setValue(generalApplicationItems);
        generalApplicationsBefore.setId(UUID.randomUUID());
        generalApplications.setId(UUID.randomUUID());
        GeneralApplicationItems generalApplicationItemsAdded =
            GeneralApplicationItems.builder().generalApplicationSender(
                    buildDynamicIntervenerList()).generalApplicationCreatedBy("Claire Mumford")
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
                .generalApplications(generalApplicationsCollection).intervener1GeneralApplications(generalApplicationsCollection)
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
