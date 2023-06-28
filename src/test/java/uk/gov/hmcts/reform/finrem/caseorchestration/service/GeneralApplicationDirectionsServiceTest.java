package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.CaseEventDetail;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.INTE_BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.INTE_DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.INTE_FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDetailsFromResource;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_HEARING_REQUIRED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DOCUMENT_LATEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_PRE_STATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PREPARE_FOR_HEARING_STATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

public class GeneralApplicationDirectionsServiceTest extends BaseServiceTest {

    private static final String GENERAL_APPLICATION_DIRECTIONS_DOCUMENT_BIN_URL = "http://dm-store/1f3a-gads-doc/binary";
    private static final String INTERIM_HEARING_DOCUMENT_BIN_URL = "http://dm-store/1f3a-gads-doc/binary";

    @Autowired
    private GeneralApplicationDirectionsService generalApplicationDirectionsService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private DocumentConfiguration documentConfiguration;

    @MockBean
    private BulkPrintService bulkPrintService;
    @MockBean
    private GenericDocumentService genericDocumentService;
    @MockBean
    private CcdService ccdService;
    @MockBean
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Captor
    ArgumentCaptor<CaseDetails> documentGenerationRequestCaseDetailsCaptor;
    @Captor
    ArgumentCaptor<List<BulkPrintDocument>> printDocumentsRequestDocumentListCaptor;

    private CaseDetails caseDetails;

    @Before
    public void setup() {
        caseDetails = caseDetailsFromResource("/fixtures/general-application-directions.json", objectMapper);
        when(genericDocumentService.generateDocument(any(), any(), any(), any())).thenReturn(caseDocument(DOC_URL, FILE_NAME,
            GENERAL_APPLICATION_DIRECTIONS_DOCUMENT_BIN_URL));
    }

    @Test
    public void givenContestedCase_whenDirectionEventExecutedReturnNoPreviousStateFound_thenHandlerReturnsPostStateNull() {
        GeneralApplicationWrapper wrapper = GeneralApplicationWrapper.builder().generalApplicationDirectionsHearingRequired(YesOrNo.NO).build();
        FinremCaseData data = FinremCaseData.builder().generalApplicationWrapper(wrapper).build();
        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
            .data(data).build();
        when(finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails)).thenReturn(caseDetails);
        when(ccdService.getCcdEventDetailsOnCase(any(), any(), any())).thenReturn(new ArrayList<>());
        caseDetails.getData().put(GENERAL_APPLICATION_DIRECTIONS_HEARING_REQUIRED, NO_VALUE);
        assertNull(generalApplicationDirectionsService.getEventPostState(finremCaseDetails, AUTH_TOKEN));
    }

    @Test
    public void givenContestedCase_whenDirectionEventExecutedReturnPreviousStateFound_thenHandlerReturnsPreviousPostState() {
        GeneralApplicationWrapper wrapper = GeneralApplicationWrapper.builder().generalApplicationDirectionsHearingRequired(YesOrNo.NO)
            .generalApplicationPreState("applicationIssued").build();
        FinremCaseData data = FinremCaseData.builder().generalApplicationWrapper(wrapper).build();
        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
            .data(data).build();
        when(finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails)).thenReturn(caseDetails);
        when(ccdService.getCcdEventDetailsOnCase(any(), any(), any())).thenReturn(new ArrayList<>());

        assertEquals("applicationIssued", generalApplicationDirectionsService.getEventPostState(finremCaseDetails, AUTH_TOKEN));
    }

    @Test
    public void givenContestedCase_whenDirectionEventExecutedAndAnySortOfHearingGoingOn_thenHandlerReturnsPostState() {

        List<CaseEventDetail> list = new ArrayList<>();
        CaseEventDetail.CaseEventDetailBuilder builder = CaseEventDetail.builder();
        builder.eventName("General Application Outcome");
        list.add(builder.build());

        builder = CaseEventDetail.builder();
        builder.eventName("List for Hearing");
        list.add(builder.build());

        caseDetails.getData().put(GENERAL_APPLICATION_DIRECTIONS_HEARING_REQUIRED, NO_VALUE);
        caseDetails.getData().put(GENERAL_APPLICATION_PRE_STATE, "applicationIssued");
        when(ccdService.getCcdEventDetailsOnCase(AUTH_TOKEN, caseDetails, EventType.GENERAL_APPLICATION_DIRECTIONS.getCcdType()))
            .thenReturn(list);
        GeneralApplicationWrapper wrapper = GeneralApplicationWrapper.builder().generalApplicationDirectionsHearingRequired(YesOrNo.NO)
            .generalApplicationPreState("applicationIssued").build();
        FinremCaseData data = FinremCaseData.builder().generalApplicationWrapper(wrapper).build();
        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
            .data(data).build();
        when(finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails)).thenReturn(caseDetails);
        assertEquals(PREPARE_FOR_HEARING_STATE, generalApplicationDirectionsService.getEventPostState(finremCaseDetails, AUTH_TOKEN));
    }

    @Test
    public void givenHearingRequired_whenGeneralApplicationDirectionsSubmitted_thenHearingNoticeIsPrintedForIntervener1() {
        List<BulkPrintDocument> documents = new ArrayList<>();
        documents.add(getCaseDocumentAsBulkPrintDocument(
            convertToCaseDocument(caseDetails.getData().get(GENERAL_APPLICATION_DOCUMENT_LATEST))));
        GeneralApplicationWrapper wrapper = GeneralApplicationWrapper.builder().generalApplicationDirectionsHearingRequired(YesOrNo.NO)
            .generalApplicationPreState("applicationIssued").generalApplicationReferDetail("intervener1-referdetails").build();
        FinremCaseData data = FinremCaseData.builder().generalApplicationWrapper(wrapper).build();
        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
            .data(data).build();
        generalApplicationDirectionsService.submitCollectionGeneralApplicationDirections(finremCaseDetails, documents, AUTH_TOKEN);

        verify(bulkPrintService, times(1)).printIntervenerDocuments(
            any(IntervenerWrapper.class), any(FinremCaseDetails.class), eq(AUTH_TOKEN), any());
    }

    @Test
    public void givenHearingRequired_whenGeneralApplicationDirectionsSubmitted_thenHearingNoticeIsPrintedForIntervener2() {
        List<BulkPrintDocument> documents = new ArrayList<>();
        documents.add(getCaseDocumentAsBulkPrintDocument(
            convertToCaseDocument(caseDetails.getData().get(GENERAL_APPLICATION_DOCUMENT_LATEST))));
        GeneralApplicationWrapper wrapper = GeneralApplicationWrapper.builder().generalApplicationDirectionsHearingRequired(YesOrNo.NO)
            .generalApplicationPreState("applicationIssued").generalApplicationReferDetail("intervener2-referdetails").build();
        FinremCaseData data = FinremCaseData.builder().generalApplicationWrapper(wrapper).build();
        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
            .data(data).build();
        generalApplicationDirectionsService.submitCollectionGeneralApplicationDirections(finremCaseDetails, documents, AUTH_TOKEN);

        verify(bulkPrintService, times(1)).printIntervenerDocuments(
            any(IntervenerWrapper.class), any(FinremCaseDetails.class), eq(AUTH_TOKEN), any());
    }

    @Test
    public void givenHearingRequired_whenGeneralApplicationDirectionsSubmitted_thenHearingNoticeIsPrintedForIntervener3() {
        List<BulkPrintDocument> documents = new ArrayList<>();
        documents.add(getCaseDocumentAsBulkPrintDocument(
            convertToCaseDocument(caseDetails.getData().get(GENERAL_APPLICATION_DOCUMENT_LATEST))));
        GeneralApplicationWrapper wrapper = GeneralApplicationWrapper.builder().generalApplicationDirectionsHearingRequired(YesOrNo.NO)
            .generalApplicationPreState("applicationIssued").generalApplicationReferDetail("intervener3-referdetails").build();
        FinremCaseData data = FinremCaseData.builder().generalApplicationWrapper(wrapper).build();
        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
            .data(data).build();
        generalApplicationDirectionsService.submitCollectionGeneralApplicationDirections(finremCaseDetails, documents, AUTH_TOKEN);

        verify(bulkPrintService, times(1)).printIntervenerDocuments(
            any(IntervenerWrapper.class), any(FinremCaseDetails.class), eq(AUTH_TOKEN), any());
    }

    @Test
    public void givenHearingRequired_whenGeneralApplicationDirectionsSubmitted_thenHearingNoticeIsPrintedForIntervener4() {
        List<BulkPrintDocument> documents = new ArrayList<>();
        documents.add(getCaseDocumentAsBulkPrintDocument(
            convertToCaseDocument(caseDetails.getData().get(GENERAL_APPLICATION_DOCUMENT_LATEST))));
        GeneralApplicationWrapper wrapper = GeneralApplicationWrapper.builder().generalApplicationDirectionsHearingRequired(YesOrNo.NO)
            .generalApplicationPreState("applicationIssued").generalApplicationReferDetail("intervener4-referdetails").build();
        FinremCaseData data = FinremCaseData.builder().generalApplicationWrapper(wrapper).build();
        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
            .data(data).build();
        generalApplicationDirectionsService.submitCollectionGeneralApplicationDirections(finremCaseDetails, documents, AUTH_TOKEN);

        verify(bulkPrintService, times(1)).printIntervenerDocuments(
            any(IntervenerWrapper.class), any(FinremCaseDetails.class), eq(AUTH_TOKEN), any());
    }

    @Test
    public void givenHearingRequired_whenGeneralApplicationDirectionsSubmitted_thenHearingNoticeIsPrintedForApplicant() {
        List<BulkPrintDocument> documents = new ArrayList<>();
        documents.add(getCaseDocumentAsBulkPrintDocument(
            convertToCaseDocument(caseDetails.getData().get(GENERAL_APPLICATION_DOCUMENT_LATEST))));
        GeneralApplicationWrapper wrapper = GeneralApplicationWrapper.builder().generalApplicationDirectionsHearingRequired(YesOrNo.NO)
            .generalApplicationPreState("applicationIssued").generalApplicationReferDetail("applicant-referdetails").build();
        FinremCaseData data = FinremCaseData.builder().generalApplicationWrapper(wrapper).build();
        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
            .data(data).build();
        generalApplicationDirectionsService.submitCollectionGeneralApplicationDirections(finremCaseDetails, documents, AUTH_TOKEN);

        verify(bulkPrintService, times(1)).printApplicantDocuments(any(FinremCaseDetails.class), eq(AUTH_TOKEN), any());
    }

    @Test
    public void givenHearingRequired_whenGeneralApplicationDirectionsSubmitted_thenHearingNoticeIsPrintedForRespondent() {
        List<BulkPrintDocument> documents = new ArrayList<>();
        documents.add(getCaseDocumentAsBulkPrintDocument(
            convertToCaseDocument(caseDetails.getData().get(GENERAL_APPLICATION_DOCUMENT_LATEST))));
        GeneralApplicationWrapper wrapper = GeneralApplicationWrapper.builder().generalApplicationDirectionsHearingRequired(YesOrNo.NO)
            .generalApplicationPreState("applicationIssued").generalApplicationReferDetail("respondent-referdetails").build();
        FinremCaseData data = FinremCaseData.builder().generalApplicationWrapper(wrapper).build();
        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
            .data(data).build();
        generalApplicationDirectionsService.submitCollectionGeneralApplicationDirections(finremCaseDetails, documents, AUTH_TOKEN);

        verify(bulkPrintService, times(1)).printRespondentDocuments(any(FinremCaseDetails.class), eq(AUTH_TOKEN), any());
    }

    @Test
    public void givenNoHearingRequired_whenGeneralApplicationDirectionsSubmitted_thenGeneralOrderIsPrinted() {
        caseDetails.getData().put(GENERAL_APPLICATION_DIRECTIONS_HEARING_REQUIRED, NO_VALUE);
        generalApplicationDirectionsService.submitGeneralApplicationDirections(caseDetails, AUTH_TOKEN);

        assertCaseDataHasGeneralApplicationDirectionsDocument();

        verify(genericDocumentService, times(1)).generateDocument(
            eq(AUTH_TOKEN),
            documentGenerationRequestCaseDetailsCaptor.capture(),
            eq(documentConfiguration.getGeneralApplicationOrderTemplate(caseDetails)),
            eq(documentConfiguration.getGeneralApplicationOrderFileName()));
        verify(bulkPrintService, times(1)).printApplicantDocuments(any(CaseDetails.class), eq(AUTH_TOKEN),
            printDocumentsRequestDocumentListCaptor.capture());
        verify(bulkPrintService, times(1)).printRespondentDocuments(any(CaseDetails.class), eq(AUTH_TOKEN), any());

        Map<String, Object> data = documentGenerationRequestCaseDetailsCaptor.getValue().getData();
        assertThat(data, allOf(
            hasEntry("courtDetails", ImmutableMap.of(
                "courtName", "Kingston-Upon-Thames County Court And Family Court",
                "courtAddress", "Kingston upon Thames County Court, St James Road, Kingston-upon-Thames, KT1 2AD",
                "phoneNumber", "0208 972 8700",
                "email", "enquiries.kingston.countycourt@justice.gov.uk")),
            Matchers.<String, Object>hasEntry("applicantName", "Poor Guy"),
            Matchers.<String, Object>hasEntry("respondentName", "test Korivi"),
            hasKey("letterDate")));

        assertDocumentPrintRequestContainsExpectedDocuments();
    }

    @Test
    public void givenPaperApplicationInterimHearingRequired_thenInterimHearingNoticeIsPrinted() {
        caseDetails = caseDetailsFromResource("/fixtures/contested-interim-hearing.json", objectMapper);
        when(genericDocumentService.convertDocumentIfNotPdfAlready(any(), any(), any())).thenReturn(caseDocument(INTE_DOC_URL, INTE_FILE_NAME,
            INTE_BINARY_URL));
        generalApplicationDirectionsService.submitInterimHearing(caseDetails, AUTH_TOKEN);

        verify(genericDocumentService, times(1)).generateDocument(
            eq(AUTH_TOKEN),
            documentGenerationRequestCaseDetailsCaptor.capture(),
            eq(documentConfiguration.getGeneralApplicationInterimHearingNoticeTemplate(caseDetails)),
            eq(documentConfiguration.getGeneralApplicationInterimHearingNoticeFileName()));
        verify(bulkPrintService, times(1)).printApplicantDocuments(any(CaseDetails.class), eq(AUTH_TOKEN),
            printDocumentsRequestDocumentListCaptor.capture());
        verify(bulkPrintService, times(1)).printRespondentDocuments(any(CaseDetails.class), eq(AUTH_TOKEN), any());
        verify(genericDocumentService, times(1)).convertDocumentIfNotPdfAlready(any(), eq(AUTH_TOKEN), any());

        Map<String, Object> data = documentGenerationRequestCaseDetailsCaptor.getValue().getData();
        assertThat(data, allOf(
            hasEntry("courtDetails", ImmutableMap.of(
                "courtName", "Kingston-Upon-Thames County Court And Family Court",
                "courtAddress", "Kingston upon Thames County Court, St James Road, Kingston-upon-Thames, KT1 2AD",
                "phoneNumber", "0208 972 8700",
                "email", "enquiries.kingston.countycourt@justice.gov.uk")),
            Matchers.<String, Object>hasEntry("applicantName", "Poor Guy"),
            Matchers.<String, Object>hasEntry("respondentName", "test Korivi"),
            Matchers.<String, Object>hasEntry("applicantRepresented", "No"),
            Matchers.<String, Object>hasEntry("respondentRepresented", "No"),
            Matchers.<String, Object>hasEntry("interim_cfcCourtList", "FR_s_CFCList_4"),
            Matchers.<String, Object>hasEntry("interimHearingDate", "2020-06-01"),
            Matchers.<String, Object>hasEntry("interimHearingTime", "2:00 pm"),
            Matchers.<String, Object>hasEntry("interimHearingTimeEstimate", "30 minutes"),
            Matchers.<String, Object>hasEntry("interimAdditionalInformationAboutHearing", "refreshments will be provided"),
            hasKey("letterDate")));

        assertCaseDataHasInterimDocument();
    }

    @Test
    public void givenApplicationIsNotPaperInterimHearingRequired_thenInterimHearingNoticeIsPrinted() {
        caseDetails = caseDetailsFromResource("/fixtures/contested-interim-hearing-nopaper.json", objectMapper);
        when(genericDocumentService.convertDocumentIfNotPdfAlready(any(), any(), any())).thenReturn(caseDocument(INTE_DOC_URL, INTE_FILE_NAME,
            INTE_BINARY_URL));
        generalApplicationDirectionsService.submitInterimHearing(caseDetails, AUTH_TOKEN);

        verify(genericDocumentService, times(1)).generateDocument(
            eq(AUTH_TOKEN),
            documentGenerationRequestCaseDetailsCaptor.capture(),
            eq(documentConfiguration.getGeneralApplicationInterimHearingNoticeTemplate(caseDetails)),
            eq(documentConfiguration.getGeneralApplicationInterimHearingNoticeFileName()));
        verify(bulkPrintService, times(1)).printApplicantDocuments(any(CaseDetails.class), eq(AUTH_TOKEN),
            printDocumentsRequestDocumentListCaptor.capture());
        verify(bulkPrintService, times(1)).printRespondentDocuments(any(CaseDetails.class), eq(AUTH_TOKEN), any());
        verify(genericDocumentService, times(1)).convertDocumentIfNotPdfAlready(any(), eq(AUTH_TOKEN), any());

        Map<String, Object> data = documentGenerationRequestCaseDetailsCaptor.getValue().getData();
        assertThat(data, allOf(
            hasEntry("courtDetails", ImmutableMap.of(
                "courtName", "Kingston-Upon-Thames County Court And Family Court",
                "courtAddress", "Kingston upon Thames County Court, St James Road, Kingston-upon-Thames, KT1 2AD",
                "phoneNumber", "0208 972 8700",
                "email", "enquiries.kingston.countycourt@justice.gov.uk")),
            Matchers.<String, Object>hasEntry("applicantName", "Poor Guy"),
            Matchers.<String, Object>hasEntry("respondentName", "test Korivi"),
            Matchers.<String, Object>hasEntry("applicantRepresented", "No"),
            Matchers.<String, Object>hasEntry("respondentRepresented", "No"),
            Matchers.<String, Object>hasEntry("interim_cfcCourtList", "FR_s_CFCList_4"),
            Matchers.<String, Object>hasEntry("interimHearingDate", "2020-06-01"),
            Matchers.<String, Object>hasEntry("interimHearingTime", "2:00 pm"),
            Matchers.<String, Object>hasEntry("interimHearingTimeEstimate", "30 minutes"),
            Matchers.<String, Object>hasEntry("interimAdditionalInformationAboutHearing", "refreshments will be provided"),
            Matchers.<String, Object>hasEntry("applicantSolicitorConsentForEmails", "No"),
            Matchers.<String, Object>hasEntry("RespSolNotificationsEmailConsent", "No"),
            hasKey("letterDate")));

        assertCaseDataHasInterimDocument();
    }

    private void assertCaseDataHasInterimDocument() {
        assertThat(caseDetails.getData(), hasKey(INTERIM_HEARING_DOCUMENT));
        assertThat(((CaseDocument) caseDetails.getData().get(INTERIM_HEARING_DOCUMENT)).getDocumentBinaryUrl(),
            is(INTERIM_HEARING_DOCUMENT_BIN_URL));
    }


    private void assertCaseDataHasGeneralApplicationDirectionsDocument() {
        assertThat(caseDetails.getData(), hasKey(GENERAL_APPLICATION_DIRECTIONS_DOCUMENT));
        assertThat(((CaseDocument) caseDetails.getData().get(GENERAL_APPLICATION_DIRECTIONS_DOCUMENT)).getDocumentBinaryUrl(),
            is(GENERAL_APPLICATION_DIRECTIONS_DOCUMENT_BIN_URL));
    }

    private void assertDocumentPrintRequestContainsExpectedDocuments() {
        List<BulkPrintDocument> documentsToPrint = printDocumentsRequestDocumentListCaptor.getValue();
        assertThat(documentsToPrint, containsInAnyOrder(Stream.of(
             GENERAL_APPLICATION_DIRECTIONS_DOCUMENT_BIN_URL)
            .map(binaryFileUrl -> BulkPrintDocument.builder().binaryFileUrl(binaryFileUrl).fileName("app_docs.pdf").build())
            .toArray()));
    }

    private CaseDocument convertToCaseDocument(Object object) {
        return objectMapper.convertValue(object, CaseDocument.class);
    }

    private BulkPrintDocument getCaseDocumentAsBulkPrintDocument(CaseDocument caseDocument) {
        return BulkPrintDocument.builder()
            .binaryFileUrl(caseDocument.getDocumentBinaryUrl())
            .fileName(caseDocument.getDocumentFilename())
            .build();
    }
}
