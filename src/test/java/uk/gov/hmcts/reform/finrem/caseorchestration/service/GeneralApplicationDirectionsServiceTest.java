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
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.GeneralApplicationRegionWrapper;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.GeneralApplicationWrapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.INTE_BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.INTE_DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.INTE_FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.finremCaseDetailsFromResource;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.newDocument;

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
    ArgumentCaptor<Map<String, Object>> documentGenerationRequestCaseDetailsCaptor;
    @Captor
    ArgumentCaptor<List<BulkPrintDocument>> printDocumentsRequestDocumentListCaptor;

    private FinremCaseDetails caseDetails;

    @Before
    public void setup() {
        caseDetails = finremCaseDetailsFromResource("/fixtures/general-application-directions.json", objectMapper);

        when(genericDocumentService.generateDocumentFromPlaceholdersMap(any(), any(), any(), any()))
            .thenReturn(newDocument(DOC_URL, FILE_NAME,
            GENERAL_APPLICATION_DIRECTIONS_DOCUMENT_BIN_URL));
    }

    @Test
    public void whenGeneralApplicationDirectionsStarted_thenStateSpecificFieldAreSetToNull() {
        generalApplicationDirectionsService.startGeneralApplicationDirections(caseDetails);
        GeneralApplicationWrapper generalApplicationData = caseDetails.getCaseData().getGeneralApplicationWrapper();
        GeneralApplicationRegionWrapper generalApplicationRegionWrapper = caseDetails.getCaseData()
            .getRegionWrapper().getGeneralApplicationRegionWrapper();

        assertEquals(generalApplicationRegionWrapper, new GeneralApplicationRegionWrapper());

        Stream.of(generalApplicationData.getGeneralApplicationHearingRequired(),
                generalApplicationData.getGeneralApplicationDirectionsHearingDate(),
                generalApplicationData.getGeneralApplicationDirectionsHearingTime(),
                generalApplicationData.getGeneralApplicationDirectionsHearingTimeEstimate(),
                generalApplicationData.getGeneralApplicationDirectionsAdditionalInformation(),
                generalApplicationData.getGeneralApplicationDirectionsCourtOrderDate(),
                generalApplicationData.getGeneralApplicationDirectionsJudgeType(),
                generalApplicationData.getGeneralApplicationDirectionsJudgeName(),
                generalApplicationData.getGeneralApplicationDirectionsRecitals(),
                generalApplicationData.getGeneralApplicationDirectionsTextFromJudge())
            .forEach(field -> assertThat(field, is(nullValue())));
    }

    @Test
    public void givenHearingRequired_whenGeneralApplicationDirectionsSubmitted_thenHearingNoticeIsPrinted() {
        generalApplicationDirectionsService.submitGeneralApplicationDirections(caseDetails, AUTH_TOKEN);

        assertCaseDataHasGeneralApplicationDirectionsDocument();

        verify(genericDocumentService, times(1)).generateDocumentFromPlaceholdersMap(
            eq(AUTH_TOKEN),
            documentGenerationRequestCaseDetailsCaptor.capture(),
            eq(documentConfiguration.getGeneralApplicationHearingNoticeTemplate()),
            eq(documentConfiguration.getGeneralApplicationHearingNoticeFileName()));
        verify(bulkPrintService, times(1)).printApplicantDocuments(any(), eq(AUTH_TOKEN), any());
        verify(bulkPrintService, times(1)).printRespondentDocuments(any(), eq(AUTH_TOKEN),
            printDocumentsRequestDocumentListCaptor.capture());

        Map<String, Object> data = documentGenerationRequestCaseDetailsCaptor.getValue();

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
        caseDetails.getCaseData().getGeneralApplicationWrapper().setGeneralApplicationHearingRequired(YesOrNo.NO);
        generalApplicationDirectionsService.submitGeneralApplicationDirections(caseDetails, AUTH_TOKEN);

        assertCaseDataHasGeneralApplicationDirectionsDocument();

        verify(genericDocumentService, times(1)).generateDocumentFromPlaceholdersMap(
            eq(AUTH_TOKEN),
            documentGenerationRequestCaseDetailsCaptor.capture(),
            eq(documentConfiguration.getGeneralApplicationOrderTemplate()),
            eq(documentConfiguration.getGeneralApplicationOrderFileName()));
        verify(bulkPrintService, times(1)).printApplicantDocuments(any(), eq(AUTH_TOKEN),
            printDocumentsRequestDocumentListCaptor.capture());
        verify(bulkPrintService, times(1)).printRespondentDocuments(any(), eq(AUTH_TOKEN), any());

        Map<String, Object> data = documentGenerationRequestCaseDetailsCaptor.getValue();
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
        caseDetails = finremCaseDetailsFromResource("/fixtures/contested-interim-hearing.json", objectMapper);
        when(genericDocumentService.convertDocumentIfNotPdfAlready(isA(Document.class), any()))
            .thenReturn(newDocument(INTE_DOC_URL, INTE_FILE_NAME, INTE_BINARY_URL));
        generalApplicationDirectionsService.submitInterimHearing(caseDetails, AUTH_TOKEN);

        verify(genericDocumentService, times(1)).generateDocumentFromPlaceholdersMap(
            eq(AUTH_TOKEN),
            documentGenerationRequestCaseDetailsCaptor.capture(),
            eq(documentConfiguration.getGeneralApplicationInterimHearingNoticeTemplate()),
            eq(documentConfiguration.getGeneralApplicationInterimHearingNoticeFileName()));
        verify(bulkPrintService, times(1)).printApplicantDocuments(any(), eq(AUTH_TOKEN),
            printDocumentsRequestDocumentListCaptor.capture());
        verify(bulkPrintService, times(1)).printRespondentDocuments(any(), eq(AUTH_TOKEN), any());
        verify(genericDocumentService, times(1))
            .convertDocumentIfNotPdfAlready(isA(Document.class), eq(AUTH_TOKEN));

        Map<String, Object> data = documentGenerationRequestCaseDetailsCaptor.getValue();
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
        caseDetails = finremCaseDetailsFromResource("/fixtures/contested-interim-hearing-nopaper.json", objectMapper);
        when(genericDocumentService.convertDocumentIfNotPdfAlready(isA(Document.class), any()))
            .thenReturn(newDocument(INTE_DOC_URL, INTE_FILE_NAME, INTE_BINARY_URL));
        generalApplicationDirectionsService.submitInterimHearing(caseDetails, AUTH_TOKEN);

        verify(genericDocumentService, times(1)).generateDocumentFromPlaceholdersMap(
            eq(AUTH_TOKEN),
            documentGenerationRequestCaseDetailsCaptor.capture(),
            eq(documentConfiguration.getGeneralApplicationInterimHearingNoticeTemplate()),
            eq(documentConfiguration.getGeneralApplicationInterimHearingNoticeFileName()));
        verify(bulkPrintService, times(1)).printApplicantDocuments(any(), eq(AUTH_TOKEN),
            printDocumentsRequestDocumentListCaptor.capture());
        verify(bulkPrintService, times(1)).printRespondentDocuments(any(), eq(AUTH_TOKEN), any());
        verify(genericDocumentService, times(1))
            .convertDocumentIfNotPdfAlready(isA(Document.class), eq(AUTH_TOKEN));

        Map<String, Object> data = documentGenerationRequestCaseDetailsCaptor.getValue();
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
        assertThat(caseDetails.getCaseData().getInterimWrapper().getInterimHearingDirectionsDocument(), is(notNullValue()));
        assertThat(caseDetails.getCaseData().getInterimWrapper().getInterimHearingDirectionsDocument().getBinaryUrl(),
            is(INTERIM_HEARING_DOCUMENT_BIN_URL));
    }


    private void assertCaseDataHasGeneralApplicationDirectionsDocument() {
        assertThat(caseDetails.getCaseData().getGeneralApplicationWrapper().getGeneralApplicationDirectionsDocument(),
            is(notNullValue()));
        assertThat(caseDetails.getCaseData().getGeneralApplicationWrapper().getGeneralApplicationDirectionsDocument().getBinaryUrl(),
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
