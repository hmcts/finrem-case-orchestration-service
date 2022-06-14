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
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_ADDITIONAL_INFORMATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_BEDFORDSHIRE_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_BIRMINGHAM_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_BRISTOL_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_CFC_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_CLEVELAND_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_COURT_ORDER_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_DEVON_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_DORSET_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_HEARING_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_HEARING_REGION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_HEARING_REQUIRED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_HEARING_TIME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_HEARING_TIME_ESTIMATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_HUMBER_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_JUDGE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_JUDGE_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_KENTSURREY_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_LANCASHIRE_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_LIVERPOOL_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_LONDON_FRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_MANCHESTER_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_MIDLANDS_FRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_NEWPORT_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_NORTHEAST_FRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_NORTHWEST_FRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_NOTTINGHAM_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_NWYORKSHIRE_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_RECITALS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_SOUTHEAST_FRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_SOUTHWEST_FRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_SWANSEA_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_TEXT_FROM_JUDGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_THAMESVALLEY_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_WALES_FRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_WALES_OTHER_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_DOCUMENT;

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
    public void whenGeneralApplicationDirectionsStarted_thenStateSpecificFieldAreSetToNull() {
        generalApplicationDirectionsService.startGeneralApplicationDirections(caseDetails);

        Stream.of(GENERAL_APPLICATION_DIRECTIONS_HEARING_REQUIRED,
            GENERAL_APPLICATION_DIRECTIONS_HEARING_DATE,
            GENERAL_APPLICATION_DIRECTIONS_HEARING_TIME,
            GENERAL_APPLICATION_DIRECTIONS_HEARING_TIME_ESTIMATE,
            GENERAL_APPLICATION_DIRECTIONS_HEARING_REGION,
            GENERAL_APPLICATION_DIRECTIONS_LONDON_FRC,
            GENERAL_APPLICATION_DIRECTIONS_MIDLANDS_FRC,
            GENERAL_APPLICATION_DIRECTIONS_NORTHEAST_FRC,
            GENERAL_APPLICATION_DIRECTIONS_NORTHWEST_FRC,
            GENERAL_APPLICATION_DIRECTIONS_SOUTHEAST_FRC,
            GENERAL_APPLICATION_DIRECTIONS_SOUTHWEST_FRC,
            GENERAL_APPLICATION_DIRECTIONS_WALES_FRC,
            GENERAL_APPLICATION_DIRECTIONS_BEDFORDSHIRE_COURT,
            GENERAL_APPLICATION_DIRECTIONS_BIRMINGHAM_COURT,
            GENERAL_APPLICATION_DIRECTIONS_BRISTOL_COURT,
            GENERAL_APPLICATION_DIRECTIONS_CFC_COURT,
            GENERAL_APPLICATION_DIRECTIONS_CLEVELAND_COURT,
            GENERAL_APPLICATION_DIRECTIONS_DEVON_COURT,
            GENERAL_APPLICATION_DIRECTIONS_DORSET_COURT,
            GENERAL_APPLICATION_DIRECTIONS_HUMBER_COURT,
            GENERAL_APPLICATION_DIRECTIONS_KENTSURREY_COURT,
            GENERAL_APPLICATION_DIRECTIONS_LANCASHIRE_COURT,
            GENERAL_APPLICATION_DIRECTIONS_LIVERPOOL_COURT,
            GENERAL_APPLICATION_DIRECTIONS_MANCHESTER_COURT,
            GENERAL_APPLICATION_DIRECTIONS_NEWPORT_COURT,
            GENERAL_APPLICATION_DIRECTIONS_NOTTINGHAM_COURT,
            GENERAL_APPLICATION_DIRECTIONS_NWYORKSHIRE_COURT,
            GENERAL_APPLICATION_DIRECTIONS_SWANSEA_COURT,
            GENERAL_APPLICATION_DIRECTIONS_THAMESVALLEY_COURT,
            GENERAL_APPLICATION_DIRECTIONS_WALES_OTHER_COURT,
            GENERAL_APPLICATION_DIRECTIONS_ADDITIONAL_INFORMATION,
            GENERAL_APPLICATION_DIRECTIONS_COURT_ORDER_DATE,
            GENERAL_APPLICATION_DIRECTIONS_JUDGE_TYPE,
            GENERAL_APPLICATION_DIRECTIONS_JUDGE_NAME,
            GENERAL_APPLICATION_DIRECTIONS_RECITALS,
            GENERAL_APPLICATION_DIRECTIONS_TEXT_FROM_JUDGE)
            .forEach(ccdFieldName -> assertThat(caseDetails.getData().get(ccdFieldName), is(nullValue())));
    }

    @Test
    public void givenHearingRequired_whenGeneralApplicationDirectionsSubmitted_thenHearingNoticeIsPrinted() {
        generalApplicationDirectionsService.submitGeneralApplicationDirections(caseDetails, AUTH_TOKEN);

        assertCaseDataHasGeneralApplicationDirectionsDocument();

        verify(genericDocumentService, times(1)).generateDocument(
            eq(AUTH_TOKEN),
            documentGenerationRequestCaseDetailsCaptor.capture(),
            eq(documentConfiguration.getGeneralApplicationHearingNoticeTemplate()),
            eq(documentConfiguration.getGeneralApplicationHearingNoticeFileName()));
        verify(bulkPrintService, times(1)).printApplicantDocuments(any(), eq(AUTH_TOKEN), any());
        verify(bulkPrintService, times(1)).printRespondentDocuments(any(), eq(AUTH_TOKEN),
            printDocumentsRequestDocumentListCaptor.capture());

        Map<String, Object> data = documentGenerationRequestCaseDetailsCaptor.getValue().getData();

        assertThat(data, allOf(
            Matchers.<String, Object>hasEntry("ccdCaseNumber", 1234567890L),
            hasEntry("courtDetails", ImmutableMap.of(
                "courtName", "Kingston-Upon-Thames County Court And Family Court",
                "courtAddress", "Kingston upon Thames County Court, St James Road, Kingston-upon-Thames, KT1 2AD",
                "phoneNumber", "0208 972 8700",
                "email", "enquiries.kingston.countycourt@justice.gov.uk")),
            Matchers.<String, Object>hasEntry("applicantName", "Poor Guy"),
            Matchers.<String, Object>hasEntry("respondentName", "test Korivi"),
            Matchers.<String, Object>hasEntry("hearingVenue",
                "Croydon County Court And Family Court, Croydon County Court, Altyre Road, Croydon, CR9 5AB"),
            hasKey("letterDate")));

        assertDocumentPrintRequestContainsExpectedDocuments();
    }

    @Test
    public void givenNoHearingRequired_whenGeneralApplicationDirectionsSubmitted_thenGeneralOrderIsPrinted() {
        caseDetails.getData().put(GENERAL_APPLICATION_DIRECTIONS_HEARING_REQUIRED, NO_VALUE);
        generalApplicationDirectionsService.submitGeneralApplicationDirections(caseDetails, AUTH_TOKEN);

        assertCaseDataHasGeneralApplicationDirectionsDocument();

        verify(genericDocumentService, times(1)).generateDocument(
            eq(AUTH_TOKEN),
            documentGenerationRequestCaseDetailsCaptor.capture(),
            eq(documentConfiguration.getGeneralApplicationOrderTemplate()),
            eq(documentConfiguration.getGeneralApplicationOrderFileName()));
        verify(bulkPrintService, times(1)).printApplicantDocuments(any(), eq(AUTH_TOKEN),
            printDocumentsRequestDocumentListCaptor.capture());
        verify(bulkPrintService, times(1)).printRespondentDocuments(any(), eq(AUTH_TOKEN), any());

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
        when(genericDocumentService.convertDocumentIfNotPdfAlready(any(), any())).thenReturn(caseDocument(INTE_DOC_URL, INTE_FILE_NAME,
            INTE_BINARY_URL));
        generalApplicationDirectionsService.submitInterimHearing(caseDetails, AUTH_TOKEN);

        verify(genericDocumentService, times(1)).generateDocument(
            eq(AUTH_TOKEN),
            documentGenerationRequestCaseDetailsCaptor.capture(),
            eq(documentConfiguration.getGeneralApplicationInterimHearingNoticeTemplate()),
            eq(documentConfiguration.getGeneralApplicationInterimHearingNoticeFileName()));
        verify(bulkPrintService, times(1)).printApplicantDocuments(any(), eq(AUTH_TOKEN),
            printDocumentsRequestDocumentListCaptor.capture());
        verify(bulkPrintService, times(1)).printRespondentDocuments(any(), eq(AUTH_TOKEN), any());
        verify(genericDocumentService, times(1)).convertDocumentIfNotPdfAlready(any(), eq(AUTH_TOKEN));

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
        when(genericDocumentService.convertDocumentIfNotPdfAlready(any(), any())).thenReturn(caseDocument(INTE_DOC_URL, INTE_FILE_NAME,
            INTE_BINARY_URL));
        generalApplicationDirectionsService.submitInterimHearing(caseDetails, AUTH_TOKEN);

        verify(genericDocumentService, times(1)).generateDocument(
            eq(AUTH_TOKEN),
            documentGenerationRequestCaseDetailsCaptor.capture(),
            eq(documentConfiguration.getGeneralApplicationInterimHearingNoticeTemplate()),
            eq(documentConfiguration.getGeneralApplicationInterimHearingNoticeFileName()));
        verify(bulkPrintService, times(1)).printApplicantDocuments(any(), eq(AUTH_TOKEN),
            printDocumentsRequestDocumentListCaptor.capture());
        verify(bulkPrintService, times(1)).printRespondentDocuments(any(), eq(AUTH_TOKEN), any());
        verify(genericDocumentService, times(1)).convertDocumentIfNotPdfAlready(any(), eq(AUTH_TOKEN));

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
        System.out.println(documentsToPrint);
        assertThat(documentsToPrint, containsInAnyOrder(Stream.of(
            GENERAL_APPLICATION_DIRECTIONS_DOCUMENT_BIN_URL,
            "http://dm-store/hijbb-general-application-latest-document/binary",
            "http://dm-store/hijbb-general-application-draft-order/binary")
            .map(binaryFileUrl -> BulkPrintDocument.builder().binaryFileUrl(binaryFileUrl).build())
            .toArray()));
    }

    private void assertDocumentPrintRequestContainsExpectedInterimDocuments() {
        List<BulkPrintDocument> documentsToPrint = printDocumentsRequestDocumentListCaptor.getValue();
        System.out.println(documentsToPrint);
        assertThat(documentsToPrint, containsInAnyOrder(Stream.of(
                INTERIM_HEARING_DOCUMENT_BIN_URL,
                "http://dm-store/hijbb-general-application-latest-document/binary",
                "http://dm-store/hijbb-general-application-draft-order/binary")
            .map(binaryFileUrl -> BulkPrintDocument.builder().binaryFileUrl(binaryFileUrl).build())
            .toArray()));
    }
}
