package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.GeneralApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationItems;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationsCollection;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_CREATED_BY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DOCUMENT_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DOCUMENT_LATEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DOCUMENT_LATEST_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DRAFT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_HEARING_REQUIRED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_PRE_STATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_RECEIVED_FROM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_SPECIAL_MEASURES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_TIME_ESTIMATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER3;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER4;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;

@RunWith(MockitoJUnitRunner.class)
public class GeneralApplicationServiceTest {

    private static final String AUTH_TOKEN = "token";
    public static final String DOC_UPLOADED_URL = "http://dm-store/lhjbyuivu87y989hijbb";
    public static final String DOC_UPLOADED_BINARY_URL = "http://dm-store/lhjbyuivu87y989hijbb/binary";
    public static final String DOC_UPLOADED_NAME = "app_docs";
    public static final String WORD_FORMAT_EXTENSION = ".doc";
    public static final String PDF_FORMAT_EXTENSION = ".pdf";
    public static final String DOC_IN_EXISTING_COLLECTION_URL =
        "http://document-management-store:8080/documents/0abf044e-3d01-45eb-b792-c06d1e6344ee";
    public static final String DOC_IN_NEW_COLLECTION_URL =
        "http://document-management-store:8080/documents/0fbf044e-3d01-85eb-b792-c36d1e6344ee";
    public static final String USER_NAME = "Tony";

    private GeneralApplicationService generalApplicationService;
    @Mock
    private IdamService idamService;
    @Mock
    private DocumentHelper documentHelper;
    @Mock
    private GenericDocumentService genericDocumentService;
    @Mock
    private AssignCaseAccessService accessService;
    private GeneralApplicationHelper helper;
    private ObjectMapper objectMapper;
    private CaseDetails caseDetails;
    private CaseDetails caseDetailsBefore;
    private String caseId = "123123123";

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        caseDetailsBefore = getApplicationIssuedCaseDetailsBefore();
        caseDetails = CaseDetails.builder().data(new LinkedHashMap<>()).build();
        helper = new GeneralApplicationHelper(objectMapper, genericDocumentService);
        generalApplicationService = new GeneralApplicationService(documentHelper,
            objectMapper, idamService, genericDocumentService, accessService, helper);

        CaseDocument caseDocument = getCaseDocument(PDF_FORMAT_EXTENSION);
        when(documentHelper.convertToCaseDocument(any())).thenReturn(caseDocument);
        when(genericDocumentService.convertDocumentIfNotPdfAlready(any(), anyString(), any()))
            .thenReturn(getCaseDocument(PDF_FORMAT_EXTENSION));
    }

    @Test
    public void updateAndSortGeneralApplicationsForCaseworker() {

        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        when(accessService.getActiveUser(any(), any())).thenReturn("Case");
        GeneralApplicationWrapper wrapper = callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper();
        GeneralApplicationWrapper wrapperBefore = callbackRequest.getCaseDetailsBefore().getData().getGeneralApplicationWrapper();
        GeneralApplicationsCollection generalApplications = GeneralApplicationsCollection
            .builder().id(UUID.randomUUID()).build();
        GeneralApplicationItems generalApplicationItems =
            GeneralApplicationItems.builder().generalApplicationCreatedBy("Claire Mumford")
                .generalApplicationHearingRequired("No").generalApplicationTimeEstimate("48 hours")
                .generalApplicationSpecialMeasures("Special measure").generalApplicationCreatedDate(LocalDate.now()).build();
        generalApplications.setValue(generalApplicationItems);
        wrapper.setGeneralApplications(List.of(wrapperBefore.getGeneralApplications().get(0), generalApplications));
        wrapper.setGeneralApplications(wrapperBefore.getGeneralApplications());
        wrapper.getGeneralApplications().forEach(
            x -> x.getValue().setGeneralApplicationReceivedFrom(buildDynamicList(APPLICANT)));

        FinremCaseData caseData = generalApplicationService.updateGeneralApplications(callbackRequest, AUTH_TOKEN);

        List<GeneralApplicationCollectionData> generalApplicationCollectionDataList
            = helper.covertToGeneralApplicationData(caseData.getGeneralApplicationWrapper().getGeneralApplications());

        assertEquals(1, generalApplicationCollectionDataList.size());
        assertEquals(APPLICANT, caseData.getGeneralApplicationWrapper().getGeneralApplications().get(0)
            .getValue().getGeneralApplicationReceivedFrom().getValue().getCode());
    }

    @Test
    public void updateAndSortGeneralApplicationsForIntervener1() {

        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        when(accessService.getActiveUser(any(), any())).thenReturn("Intervener1");
        GeneralApplicationWrapper wrapper = callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper();
        GeneralApplicationWrapper wrapperBefore = callbackRequest.getCaseDetailsBefore().getData().getGeneralApplicationWrapper();
        wrapper.setIntervener1GeneralApplications(wrapperBefore.getGeneralApplications());
        wrapper.setGeneralApplications(wrapperBefore.getGeneralApplications());
        wrapper.getIntervener1GeneralApplications().forEach(
            x -> x.getValue().setGeneralApplicationReceivedFrom(buildDynamicList(INTERVENER1)));

        FinremCaseData caseData = generalApplicationService.updateGeneralApplications(callbackRequest, AUTH_TOKEN);

        List<GeneralApplicationCollectionData> generalApplicationCollectionDataList
            = helper.covertToGeneralApplicationData(caseData.getGeneralApplicationWrapper().getIntervener1GeneralApplications());

        assertEquals(2, generalApplicationCollectionDataList.size());
        assertEquals(INTERVENER1, caseData.getGeneralApplicationWrapper().getIntervener1GeneralApplications().get(0)
            .getValue().getGeneralApplicationReceivedFrom().getValue().getCode());
    }

    @Test
    public void updateAndSortGeneralApplicationsForIntervener2() {

        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        when(accessService.getActiveUser(any(), any())).thenReturn("Intervener2");
        GeneralApplicationWrapper wrapper = callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper();
        GeneralApplicationWrapper wrapperBefore = callbackRequest.getCaseDetailsBefore().getData().getGeneralApplicationWrapper();
        wrapper.setIntervener2GeneralApplications(wrapperBefore.getGeneralApplications());
        wrapper.setGeneralApplications(wrapperBefore.getGeneralApplications());
        wrapper.getIntervener2GeneralApplications().forEach(
            x -> x.getValue().setGeneralApplicationReceivedFrom(buildDynamicList(INTERVENER2)));

        FinremCaseData caseData = generalApplicationService.updateGeneralApplications(callbackRequest, AUTH_TOKEN);

        List<GeneralApplicationCollectionData> generalApplicationCollectionDataList
            = helper.covertToGeneralApplicationData(caseData.getGeneralApplicationWrapper().getIntervener2GeneralApplications());

        assertEquals(2, generalApplicationCollectionDataList.size());
        assertEquals(INTERVENER2, caseData.getGeneralApplicationWrapper().getIntervener2GeneralApplications().get(0)
            .getValue().getGeneralApplicationReceivedFrom().getValue().getCode());
    }

    @Test
    public void updateAndSortGeneralApplicationsForIntervener3() {

        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        when(accessService.getActiveUser(any(), any())).thenReturn("Intervener3");
        GeneralApplicationWrapper wrapper = callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper();
        GeneralApplicationWrapper wrapperBefore = callbackRequest.getCaseDetailsBefore().getData().getGeneralApplicationWrapper();
        wrapper.setIntervener3GeneralApplications(wrapperBefore.getGeneralApplications());
        wrapper.setGeneralApplications(wrapperBefore.getGeneralApplications());
        wrapper.getIntervener3GeneralApplications().forEach(
            x -> x.getValue().setGeneralApplicationReceivedFrom(buildDynamicList(INTERVENER3)));

        FinremCaseData caseData = generalApplicationService.updateGeneralApplications(callbackRequest, AUTH_TOKEN);

        List<GeneralApplicationCollectionData> generalApplicationCollectionDataList
            = helper.covertToGeneralApplicationData(caseData.getGeneralApplicationWrapper().getIntervener3GeneralApplications());

        assertEquals(2, generalApplicationCollectionDataList.size());
        assertEquals(INTERVENER3, caseData.getGeneralApplicationWrapper().getIntervener3GeneralApplications().get(0)
            .getValue().getGeneralApplicationReceivedFrom().getValue().getCode());
    }

    @Test
    public void updateAndSortGeneralApplicationsForIntervener4() {

        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        when(accessService.getActiveUser(any(), any())).thenReturn("Intervener4");
        GeneralApplicationWrapper wrapper = callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper();
        GeneralApplicationWrapper wrapperBefore = callbackRequest.getCaseDetailsBefore().getData().getGeneralApplicationWrapper();
        wrapper.setIntervener4GeneralApplications(wrapperBefore.getGeneralApplications());
        wrapper.setGeneralApplications(wrapperBefore.getGeneralApplications());
        wrapper.getIntervener4GeneralApplications().forEach(
            x -> x.getValue().setGeneralApplicationReceivedFrom(buildDynamicList(INTERVENER4)));

        FinremCaseData caseData = generalApplicationService.updateGeneralApplications(callbackRequest, AUTH_TOKEN);

        List<GeneralApplicationCollectionData> generalApplicationCollectionDataList
            = helper.covertToGeneralApplicationData(caseData.getGeneralApplicationWrapper().getIntervener4GeneralApplications());

        assertEquals(2, generalApplicationCollectionDataList.size());
        assertEquals(INTERVENER4, caseData.getGeneralApplicationWrapper().getIntervener4GeneralApplications().get(0)
            .getValue().getGeneralApplicationReceivedFrom().getValue().getCode());
    }

    @Test
    public void updateAndSortGeneralApplicationsForApplicant() {

        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        when(accessService.getActiveUser(any(), any())).thenReturn("Applicant");
        GeneralApplicationWrapper wrapper = callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper();
        wrapper.setAppRespGeneralApplications(wrapper.getGeneralApplications());
        wrapper.setGeneralApplications(List.of(wrapper.getGeneralApplications().get(0)));
        GeneralApplicationWrapper wrapperBefore = callbackRequest.getCaseDetailsBefore().getData().getGeneralApplicationWrapper();

        wrapper.getAppRespGeneralApplications().forEach(
            x -> x.getValue().setAppRespGeneralApplicationReceivedFrom(APPLICANT));
        wrapperBefore.getGeneralApplications().forEach(
            x -> x.getValue().setGeneralApplicationReceivedFrom(buildDynamicList(APPLICANT)));
        wrapperBefore.setAppRespGeneralApplications(wrapperBefore.getGeneralApplications());

        FinremCaseData caseData = generalApplicationService.updateGeneralApplications(callbackRequest, AUTH_TOKEN);

        List<GeneralApplicationCollectionData> generalApplicationCollectionDataList
            = helper.covertToGeneralApplicationData(caseData.getGeneralApplicationWrapper().getAppRespGeneralApplications());

        assertEquals(1, generalApplicationCollectionDataList.size());
        assertEquals("applicant", caseData.getGeneralApplicationWrapper().getAppRespGeneralApplications().get(0)
            .getValue().getAppRespGeneralApplicationReceivedFrom());
    }

    @Test
    public void updateAndSortGeneralApplicationsForRespondent() {

        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        when(accessService.getActiveUser(any(), any())).thenReturn("Respondent");
        GeneralApplicationWrapper wrapper = callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper();
        wrapper.setAppRespGeneralApplications(wrapper.getGeneralApplications());
        wrapper.setGeneralApplications(List.of(wrapper.getGeneralApplications().get(0)));
        GeneralApplicationWrapper wrapperBefore = callbackRequest.getCaseDetailsBefore().getData().getGeneralApplicationWrapper();

        wrapper.getAppRespGeneralApplications().forEach(
            x -> x.getValue().setAppRespGeneralApplicationReceivedFrom(RESPONDENT));
        wrapperBefore.getGeneralApplications().forEach(
            x -> x.getValue().setGeneralApplicationReceivedFrom(buildDynamicList(RESPONDENT)));
        wrapperBefore.setAppRespGeneralApplications(wrapperBefore.getGeneralApplications());

        FinremCaseData caseData = generalApplicationService.updateGeneralApplications(callbackRequest, AUTH_TOKEN);

        List<GeneralApplicationCollectionData> generalApplicationCollectionDataList
            = helper.covertToGeneralApplicationData(caseData.getGeneralApplicationWrapper().getAppRespGeneralApplications());

        assertEquals(1, generalApplicationCollectionDataList.size());
        assertEquals("respondent", caseData.getGeneralApplicationWrapper().getAppRespGeneralApplications().get(0)
            .getValue().getAppRespGeneralApplicationReceivedFrom());
    }

    @Test
    public void givenUploadGenAppDocWordFormat_whenUpdateCaseDataSubmit_thenConvertGenAppDocLatestToPdf() {

        Map<String, String> documentMapInWordFormat = getCcdDocumentMap();
        caseDetails.getData().put(GENERAL_APPLICATION_DOCUMENT, documentMapInWordFormat);

        CaseDocument caseDocument = getCaseDocument(WORD_FORMAT_EXTENSION);
        when(documentHelper.convertToCaseDocument(documentMapInWordFormat)).thenReturn(caseDocument);
        generalApplicationService.updateCaseDataSubmit(caseDetails.getData(), caseDetailsBefore, AUTH_TOKEN, caseId);

        String convertedDocumentName =
            ((CaseDocument) caseDetails.getData().get(GENERAL_APPLICATION_DOCUMENT_LATEST)).getDocumentFilename();
        assertThat(convertedDocumentName, containsString(PDF_FORMAT_EXTENSION));
    }

    @Test
    public void whenDraftOrderNotUploaded() {
        generalApplicationService.updateCaseDataSubmit(caseDetails.getData(), caseDetailsBefore, AUTH_TOKEN, caseId);
        assertNull(caseDetails.getData().get(GENERAL_APPLICATION_DRAFT_ORDER));
    }

    @Test
    public void givenUploadDraftDocWordFormat_whenUpdateCaseDataSubmit_thenConvertDraftDocToPdf() {

        Map<String, String> documentMapInWordFormat = getCcdDocumentMap();
        caseDetails.getData().put(GENERAL_APPLICATION_DRAFT_ORDER, documentMapInWordFormat);

        CaseDocument caseDocument = getCaseDocument(WORD_FORMAT_EXTENSION);
        when(documentHelper.convertToCaseDocument(documentMapInWordFormat)).thenReturn(caseDocument);
        generalApplicationService.updateCaseDataSubmit(caseDetails.getData(), caseDetailsBefore, AUTH_TOKEN, caseId);

        String convertedDocumentName =
            ((CaseDocument) caseDetails.getData().get(GENERAL_APPLICATION_DRAFT_ORDER)).getDocumentFilename();
        assertThat(convertedDocumentName, containsString(PDF_FORMAT_EXTENSION));
    }

    @Test
    public void givenGeneralApplication_whenUpdateCaseDataSubmit_thenStateIsIssued() {

        generalApplicationService.updateCaseDataSubmit(caseDetails.getData(), caseDetailsBefore, AUTH_TOKEN, caseId);

        assertThat(caseDetails.getData().get(GENERAL_APPLICATION_PRE_STATE), is("applicationIssued"));
    }

    @Test
    public void givenGeneralApplication_whenUpdateCaseDataSubmit_thenGenAppDocumentLatestDateIsNow() {

        generalApplicationService.updateCaseDataSubmit(caseDetails.getData(), caseDetailsBefore, AUTH_TOKEN, caseId);

        assertThat(caseDetails.getData().get(GENERAL_APPLICATION_DOCUMENT_LATEST_DATE), is(LocalDate.now()));
    }

    @Test
    public void givenGeneralApplication_whenUpdateCaseDataSubmit_thenGenAppDataListHasUploadedDoc() {

        generalApplicationService.updateCaseDataSubmit(caseDetails.getData(), caseDetailsBefore, AUTH_TOKEN, caseId);

        List<GeneralApplicationData> generalApplicationDataList = objectMapper.convertValue(caseDetails.getData()
            .get(GENERAL_APPLICATION_DOCUMENT_COLLECTION), new TypeReference<>() {});
        assertThat(generalApplicationDataList, hasSize(1));
        assertThat(
            matchesUploadedDocumentFields(
                generalApplicationDataList.get(0).getGeneralApplication().getGeneralApplicationDocument()),
            is(true));
    }

    @Test
    public void givenGeneralApplication_whenUpdateCaseDataSubmit_thenGenAppDocLatestIsUploadedDoc() {

        generalApplicationService.updateCaseDataSubmit(caseDetails.getData(), caseDetailsBefore, AUTH_TOKEN, caseId);

        CaseDocument generalApplicationLatest =
            (CaseDocument) caseDetails.getData().get(GENERAL_APPLICATION_DOCUMENT_LATEST);
        assertThat(matchesUploadedDocumentFields(generalApplicationLatest), is(true));
    }

    @Test
    public void givenGeneralApplicationWithPreviousDocs_whenUpdateCaseDataSubmit_thenGenAppDocIsAddedToCollection() {

        caseDetails.getData().put(GENERAL_APPLICATION_DOCUMENT_COLLECTION, getGeneralApplicationDataList());
        generalApplicationService.updateCaseDataSubmit(caseDetails.getData(), caseDetailsBefore, AUTH_TOKEN, caseId);

        List<GeneralApplicationData> generalApplicationDataList = objectMapper.convertValue(caseDetails.getData()
            .get(GENERAL_APPLICATION_DOCUMENT_COLLECTION), new TypeReference<>() {});
        assertThat(generalApplicationDataList, hasSize(2));
        assertThat(generalApplicationDataList.get(0).getGeneralApplication().getGeneralApplicationDocument().getDocumentUrl(),
            is(DOC_IN_EXISTING_COLLECTION_URL));
        assertThat(
            matchesUploadedDocumentFields(
                generalApplicationDataList.get(1).getGeneralApplication().getGeneralApplicationDocument()),
            is(true));
    }


    @Test
    public void givenGeneralApplicationWithPreviousData_whenUpdateCaseDataStart_thenPreviousDataDeleted() {

        String[] generalAppParameters = {GENERAL_APPLICATION_RECEIVED_FROM,
            GENERAL_APPLICATION_HEARING_REQUIRED,
            GENERAL_APPLICATION_TIME_ESTIMATE,
            GENERAL_APPLICATION_SPECIAL_MEASURES,
            GENERAL_APPLICATION_DOCUMENT,
            GENERAL_APPLICATION_DRAFT_ORDER,
            GENERAL_APPLICATION_DIRECTIONS_DOCUMENT};
        Stream.of(generalAppParameters).forEach(ccdFieldName -> caseDetails.getData().put(ccdFieldName, "Something"));

        generalApplicationService.updateCaseDataStart(caseDetails.getData(), AUTH_TOKEN);

        Stream.of(generalAppParameters)
            .forEach(ccdFieldName -> assertThat(caseDetails.getData().get(ccdFieldName), is(nullValue())));
    }

    @Test
    public void givenGeneralApplication_whenUpdateCaseDataStart_thenCreatedByIsCurrentLoggedUser() {
        when(idamService.getIdamFullName(AUTH_TOKEN)).thenReturn(USER_NAME);
        generalApplicationService.updateCaseDataStart(caseDetails.getData(), AUTH_TOKEN);

        assertThat(caseDetails.getData().get(GENERAL_APPLICATION_CREATED_BY), is(USER_NAME));
    }

    @Test
    public void givenGeneralApplication_shouldGetInterimGeneralApplicationList() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();
        FinremCaseData caseDataBefore = callbackRequest.getCaseDetailsBefore().getData();
        GeneralApplicationsCollection generalApplications = GeneralApplicationsCollection
            .builder().id(UUID.randomUUID()).build();
        GeneralApplicationItems generalApplicationItems =
            GeneralApplicationItems.builder().generalApplicationCreatedBy("Claire Mumford")
                .generalApplicationHearingRequired("No").generalApplicationTimeEstimate("48 hours")
                .generalApplicationSpecialMeasures("Special measure").generalApplicationCreatedDate(LocalDate.now()).build();
        generalApplications.setValue(generalApplicationItems);
        caseData.getGeneralApplicationWrapper().setGeneralApplications(List.of(
            caseDataBefore.getGeneralApplicationWrapper().getGeneralApplications().get(0), generalApplications));
        caseData.getGeneralApplicationWrapper().getGeneralApplications().forEach(x -> x.getValue()
            .setGeneralApplicationReceivedFrom(buildDynamicList(INTERVENER1)));
        String generalApplicationCollection = GENERAL_APPLICATION_COLLECTION;

        List<GeneralApplicationCollectionData> col =
            generalApplicationService.getInterimGeneralApplicationList(
                generalApplicationCollection, caseData, caseDataBefore);
        List<GeneralApplicationItems> itemsActual = new ArrayList<>();

        col.forEach(x -> itemsActual.add(x.getGeneralApplicationItems()));

        assertEquals("No", itemsActual.get(0).getGeneralApplicationHearingRequired());
        assertEquals("Special measure", itemsActual.get(0).getGeneralApplicationSpecialMeasures());
        assertEquals("48 hours", itemsActual.get(0).getGeneralApplicationTimeEstimate());
        assertEquals("Claire Mumford", itemsActual.get(0).getGeneralApplicationCreatedBy());
        assertEquals("Intervener1", itemsActual.get(0).getGeneralApplicationReceivedFrom().getValue()
            .getCode());

    }

    @Test
    public void givenGeneralApplicationAndNoExistingIntervenerDirectionsDocument_ShouldUpdateIntervenerDirectionsDocuments() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        GeneralApplicationWrapper wrapper = callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper();
        CaseDocument caseDocument = getCaseDocument(PDF_FORMAT_EXTENSION);
        caseDocument.setDocumentUrl(DOC_IN_EXISTING_COLLECTION_URL);
        generalApplicationService.updateIntervenerDirectionsDocumentCollection(wrapper, caseDocument);
        assertEquals(caseDocument, wrapper.getGeneralApplicationIntvrDocuments().get(0).getValue().getDocument());
    }

    @Test
    public void givenGeneralApplicationAndExistingIntervenerDirectionsDocument_ShouldUpdateIntervenerDirectionsDocuments() {
        IntervenerCaseDocumentCollection gaCaseDocumentCollection = IntervenerCaseDocumentCollection.builder().build();
        CaseDocument existingCaseDocument = getCaseDocument(PDF_FORMAT_EXTENSION);
        existingCaseDocument.setDocumentUrl(DOC_IN_NEW_COLLECTION_URL);
        IntervenerCaseDocument gaCaseDocument = IntervenerCaseDocument.builder().build();
        gaCaseDocument.setDocument(existingCaseDocument);
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        gaCaseDocumentCollection.setValue(gaCaseDocument);
        List<IntervenerCaseDocumentCollection> gaDocumentCollectionList = new ArrayList<>();
        gaDocumentCollectionList.add(gaCaseDocumentCollection);
        GeneralApplicationWrapper wrapper = callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper();
        wrapper.setGeneralApplicationIntvrDocuments(gaDocumentCollectionList);
        CaseDocument caseDocument = getCaseDocument(PDF_FORMAT_EXTENSION);
        caseDocument.setDocumentUrl(DOC_IN_EXISTING_COLLECTION_URL);
        generalApplicationService.updateIntervenerDirectionsDocumentCollection(wrapper, caseDocument);
        assertEquals(caseDocument, wrapper.getGeneralApplicationIntvrDocuments().get(1).getValue().getDocument());
        assertEquals(existingCaseDocument, wrapper.getGeneralApplicationIntvrDocuments().get(0).getValue().getDocument());
    }

    public DynamicRadioList buildDynamicList(String role) {

        List<DynamicRadioListElement> dynamicListElements = List.of(
            getDynamicListElement(role, role)
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

    private List<GeneralApplicationData> getGeneralApplicationDataList() {
        CaseDocument caseDocument = getCaseDocument(PDF_FORMAT_EXTENSION);
        caseDocument.setDocumentUrl(DOC_IN_EXISTING_COLLECTION_URL);
        GeneralApplication generalApplication = GeneralApplication.builder()
            .generalApplicationDocument(caseDocument)
            .build();
        List<GeneralApplicationData> generalApplicationList = new ArrayList<>();
        generalApplicationList.add(
            GeneralApplicationData.builder()
                .id(UUID.randomUUID().toString())
                .generalApplication(generalApplication)
                .build()
        );
        return generalApplicationList;
    }

    private boolean matchesUploadedDocumentFields(CaseDocument document) {
        return document.getDocumentFilename().equals(DOC_UPLOADED_NAME + PDF_FORMAT_EXTENSION)
            && document.getDocumentUrl().equals(DOC_UPLOADED_URL)
            && document.getDocumentBinaryUrl().equals(DOC_UPLOADED_BINARY_URL);
    }

    private CaseDocument getCaseDocument(String documentFormat) {
        return CaseDocument.builder()
            .documentUrl(DOC_UPLOADED_URL)
            .documentBinaryUrl(DOC_UPLOADED_BINARY_URL)
            .documentFilename(DOC_UPLOADED_NAME + documentFormat)
            .build();
    }

    private Map<String, String> getCcdDocumentMap() {
        return Map.of(
            "document_url", DOC_UPLOADED_URL,
            "document_filename", DOC_UPLOADED_NAME + WORD_FORMAT_EXTENSION,
            "document_binary_url", DOC_UPLOADED_BINARY_URL);
    }

    private CaseDetails getApplicationIssuedCaseDetailsBefore() {
        return CaseDetails.builder().state("applicationIssued").build();
    }

    protected FinremCallbackRequest buildCallbackRequest() {
        GeneralApplicationItems generalApplicationItemsAdded =
            GeneralApplicationItems.builder().generalApplicationCreatedBy("Claire Mumford")
                .generalApplicationHearingRequired("Yes").generalApplicationTimeEstimate("24 hours")
                .generalApplicationSpecialMeasures("Special measure").generalApplicationCreatedDate(
                    LocalDate.of(2022, 8, 2)).build();
        GeneralApplicationsCollection generalApplications = GeneralApplicationsCollection.builder().build();
        GeneralApplicationsCollection generalApplicationsBefore = GeneralApplicationsCollection.builder().build();
        generalApplications.setValue(generalApplicationItemsAdded);
        generalApplicationsBefore.setId(UUID.randomUUID());
        generalApplications.setId(UUID.randomUUID());
        GeneralApplicationItems generalApplicationItems =
            GeneralApplicationItems.builder().generalApplicationCreatedBy("Claire Mumford")
                .generalApplicationHearingRequired("No").generalApplicationTimeEstimate("48 hours")
                .generalApplicationSpecialMeasures("Special measure").generalApplicationCreatedDate(LocalDate.now()).build();
        generalApplicationsBefore.setValue(generalApplicationItems);
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