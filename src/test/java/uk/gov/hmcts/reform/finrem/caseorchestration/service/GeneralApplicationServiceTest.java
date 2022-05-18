package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationData;

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
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;
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

@RunWith(MockitoJUnitRunner.class)
public class GeneralApplicationServiceTest {

    private static final String AUTH_TOKEN = "token";
    public static final String DOC_UPLOADED_URL = "http://dm-store/lhjbyuivu87y989hijbb";
    public static final String DOC_UPLOADED_BINARY_URL = "http://dm-store/lhjbyuivu87y989hijbb/binary";
    public static final String DOC_UPLOADED_NAME = "app_docs";
    public static final String WORD_FORMAT_EXTENSION = ".doc";
    public static final String PDF_FORMAT_EXTENSION = ".pdf";
    public static final String DOC_IN_EXISTING_COLLECTION_URL = "http://document-management-store:8080/documents/0abf044e-3d01-45eb-b792-c06d1e6344ee";
    public static final String USER_NAME = "Tony";

    @InjectMocks
    private GeneralApplicationService generalApplicationService;
    @Mock
    private IdamService idamService;
    @Mock
    private DocumentHelper documentHelper;
    @Mock
    private GenericDocumentService genericDocumentService;
    @Mock
    private ObjectMapper objectMapper;

    private CaseDetails caseDetails;
    private CaseDetails caseDetailsBefore;

    @Before
    public void setUp() {
        caseDetailsBefore = getApplicationIssuedCaseDetailsBefore();
        caseDetails = CaseDetails.builder().data(new LinkedHashMap<>()).build();

        CaseDocument caseDocument = getCaseDocument(PDF_FORMAT_EXTENSION);
        when(documentHelper.convertToCaseDocument(any())).thenReturn(caseDocument);
        when(genericDocumentService.convertDocumentIfNotPdfAlready(any(), anyString()))
            .thenReturn(getCaseDocument(PDF_FORMAT_EXTENSION));
    }

    @Test
    public void givenUploadGenAppDocWordFormat_whenUpdateCaseDataSubmit_thenConvertGenAppDocLatestToPdf() {

        Map<String, String> documentMapInWordFormat = getCcdDocumentMap();
        caseDetails.getData().put(GENERAL_APPLICATION_DOCUMENT, documentMapInWordFormat);

        CaseDocument caseDocument = getCaseDocument(WORD_FORMAT_EXTENSION);
        when(documentHelper.convertToCaseDocument(documentMapInWordFormat)).thenReturn(caseDocument);
        generalApplicationService.updateCaseDataSubmit(caseDetails.getData(), caseDetailsBefore, AUTH_TOKEN);

        String convertedDocumentName =
            ((CaseDocument) caseDetails.getData().get(GENERAL_APPLICATION_DOCUMENT_LATEST)).getDocumentFilename();
        assertThat(convertedDocumentName, containsString(PDF_FORMAT_EXTENSION));
    }

    @Test
    public void whenDraftOrderNotUploaded() {
        generalApplicationService.updateCaseDataSubmit(caseDetails.getData(), caseDetailsBefore, AUTH_TOKEN);
        assertNull(caseDetails.getData().get(GENERAL_APPLICATION_DRAFT_ORDER));
    }

    @Test
    public void givenUploadDraftDocWordFormat_whenUpdateCaseDataSubmit_thenConvertDraftDocToPdf() {

        Map<String, String> documentMapInWordFormat = getCcdDocumentMap();
        caseDetails.getData().put(GENERAL_APPLICATION_DRAFT_ORDER, documentMapInWordFormat);

        CaseDocument caseDocument = getCaseDocument(WORD_FORMAT_EXTENSION);
        when(documentHelper.convertToCaseDocument(documentMapInWordFormat)).thenReturn(caseDocument);
        generalApplicationService.updateCaseDataSubmit(caseDetails.getData(), caseDetailsBefore, AUTH_TOKEN);

        String convertedDocumentName =
            ((CaseDocument) caseDetails.getData().get(GENERAL_APPLICATION_DRAFT_ORDER)).getDocumentFilename();
        assertThat(convertedDocumentName, containsString(PDF_FORMAT_EXTENSION));
    }

    @Test
    public void givenGeneralApplication_whenUpdateCaseDataSubmit_thenStateIsIssued() {

        generalApplicationService.updateCaseDataSubmit(caseDetails.getData(), caseDetailsBefore, AUTH_TOKEN);

        assertThat(caseDetails.getData().get(GENERAL_APPLICATION_PRE_STATE), is("applicationIssued"));
    }

    @Test
    public void givenGeneralApplication_whenUpdateCaseDataSubmit_thenGenAppDocumentLatestDateIsNow() {

        generalApplicationService.updateCaseDataSubmit(caseDetails.getData(), caseDetailsBefore, AUTH_TOKEN);

        assertThat(caseDetails.getData().get(GENERAL_APPLICATION_DOCUMENT_LATEST_DATE), is(LocalDate.now()));
    }

    @Test
    public void givenGeneralApplication_whenUpdateCaseDataSubmit_thenGenAppDataListHasUploadedDoc() {

        generalApplicationService.updateCaseDataSubmit(caseDetails.getData(), caseDetailsBefore, AUTH_TOKEN);

        List<GeneralApplicationData> generalApplicationDataList =
            (List<GeneralApplicationData>) caseDetails.getData().get(GENERAL_APPLICATION_DOCUMENT_COLLECTION);
        assertThat(generalApplicationDataList, hasSize(1));
        assertThat(
            matchesUploadedDocumentFields(
                generalApplicationDataList.get(0).getGeneralApplication().getGeneralApplicationDocument()),
            is(true));
    }

    @Test
    public void givenGeneralApplication_whenUpdateCaseDataSubmit_thenGenAppDocLatestIsUploadedDoc() {

        generalApplicationService.updateCaseDataSubmit(caseDetails.getData(), caseDetailsBefore, AUTH_TOKEN);

        CaseDocument generalApplicationLatest =
            (CaseDocument) caseDetails.getData().get(GENERAL_APPLICATION_DOCUMENT_LATEST);
        assertThat(matchesUploadedDocumentFields(generalApplicationLatest), is(true));
    }

    @Test
    public void givenGeneralApplicationWithPreviousDocs_whenUpdateCaseDataSubmit_thenGenAppDocIsAddedToCollection() {

        caseDetails.getData().put(GENERAL_APPLICATION_DOCUMENT_COLLECTION, getGeneralApplicationDataList());

        when(objectMapper.convertValue(any(), (TypeReference<Object>) any())).thenReturn(getGeneralApplicationDataList());
        generalApplicationService.updateCaseDataSubmit(caseDetails.getData(), caseDetailsBefore, AUTH_TOKEN);

        List<GeneralApplicationData> generalApplicationDataList =
            (List<GeneralApplicationData>) caseDetails.getData().get(GENERAL_APPLICATION_DOCUMENT_COLLECTION);
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

        assertThat(caseDetails.getData().get(GENERAL_APPLICATION_CREATED_BY),is(USER_NAME));
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
}