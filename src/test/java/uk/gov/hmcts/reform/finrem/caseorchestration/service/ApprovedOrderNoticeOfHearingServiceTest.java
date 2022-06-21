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

import static org.hamcrest.CoreMatchers.is;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDetailsFromResource;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ADDITIONAL_HEARING_DOCUMENT_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ANOTHER_HEARING_TO_BE_LISTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LATEST_DRAFT_HEARING_ORDER;

public class ApprovedOrderNoticeOfHearingServiceTest extends BaseServiceTest {
    private static final String LATEST_DRAFT_ORDER_DOCUMENT_BIN_URL = "http://dm-store/1frea-ldo-doc/binary";
    private static final String GENERAL_APPLICATION_DIRECTIONS_DOCUMENT_BIN_URL = "http://dm-store/1f3a-gads-doc/binary";
    static final String CASE_DATA = "case_data";
    static final String CASE_DETAILS = "caseDetails";

    @Autowired
    private ApprovedOrderNoticeOfHearingService approvedOrderNoticeOfHearingService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private DocumentConfiguration documentConfiguration;

    @MockBean
    private BulkPrintService bulkPrintService;
    @MockBean
    private GenericDocumentService genericDocumentService;

    @Captor
    ArgumentCaptor<Map<String, Object>> placeholdersMapCaptor;
    @Captor
    ArgumentCaptor<List<BulkPrintDocument>> printDocumentsRequestDocumentListCaptor;

    private CaseDetails caseDetails;

    @Before
    public void setup() {
        caseDetails = caseDetailsFromResource("/fixtures/general-application-directions.json", objectMapper);
        when(genericDocumentService.generateDocumentFromPlaceholdersMap(any(), any(), any(), any())).thenReturn(caseDocument(DOC_URL, FILE_NAME,
            GENERAL_APPLICATION_DIRECTIONS_DOCUMENT_BIN_URL));
    }

    @Test
    public void givenHearingRequired_whenSubmitNoticeOfHearing_thenHearingNoticeIsPrinted() {
        caseDetails.getData().put(ANOTHER_HEARING_TO_BE_LISTED, YES_VALUE);
        approvedOrderNoticeOfHearingService.submitNoticeOfHearing(caseDetails, AUTH_TOKEN);

        assertCaseDataHasHearingNoticesCollection();
        assertDocumentServiceInteraction();

        Map<String, Object> caseDetailsMap = (Map) placeholdersMapCaptor.getValue().get(CASE_DETAILS);
        Map<String, Object> data = (Map) caseDetailsMap.get(CASE_DATA);
        assertCaseData(data);

        assertThat(printDocumentsRequestDocumentListCaptor.getValue(), containsInAnyOrder(BulkPrintDocument
            .builder()
            .binaryFileUrl(GENERAL_APPLICATION_DIRECTIONS_DOCUMENT_BIN_URL)
            .build()));
    }

    @Test
    public void givenDraftHearingOrderIsUploaded_whenSubmitNoticeOfHearing_thenOrderIsPrinted() {

        caseDetails.getData().put(ANOTHER_HEARING_TO_BE_LISTED, YES_VALUE);
        caseDetails.getData().put(LATEST_DRAFT_HEARING_ORDER, CaseDocument.builder().documentBinaryUrl(LATEST_DRAFT_ORDER_DOCUMENT_BIN_URL).build());
        approvedOrderNoticeOfHearingService.submitNoticeOfHearing(caseDetails, AUTH_TOKEN);

        assertCaseDataHasHearingNoticesCollection();

        assertDocumentServiceInteraction();

        Map<String, Object> caseDetailsMap = (Map) placeholdersMapCaptor.getValue().get(CASE_DETAILS);
        Map<String, Object> data = (Map) caseDetailsMap.get(CASE_DATA);

        assertCaseData(data);

        assertThat(printDocumentsRequestDocumentListCaptor.getValue(), containsInAnyOrder(BulkPrintDocument
                .builder()
                .binaryFileUrl(GENERAL_APPLICATION_DIRECTIONS_DOCUMENT_BIN_URL)
                .build(),
            BulkPrintDocument
                .builder()
                .binaryFileUrl(LATEST_DRAFT_ORDER_DOCUMENT_BIN_URL)
                .build())
        );
    }

    private void assertDocumentServiceInteraction() {
        verify(genericDocumentService, times(1)).generateDocumentFromPlaceholdersMap(
            eq(AUTH_TOKEN),
            placeholdersMapCaptor.capture(),
            eq(documentConfiguration.getAdditionalHearingTemplate()),
            eq(documentConfiguration.getAdditionalHearingFileName()));
        verify(bulkPrintService, times(1)).printApplicantDocuments(any(), eq(AUTH_TOKEN), any());
        verify(bulkPrintService, times(1)).printRespondentDocuments(any(), eq(AUTH_TOKEN),
            printDocumentsRequestDocumentListCaptor.capture());
    }

    private void assertCaseDataHasHearingNoticesCollection() {
        assertThat(caseDetails.getData(), hasKey(ADDITIONAL_HEARING_DOCUMENT_COLLECTION));
        List<CaseDocument> hearingNotices = (List<CaseDocument>) caseDetails.getData().get(ADDITIONAL_HEARING_DOCUMENT_COLLECTION);
        assertThat(hearingNotices.size(), is(1));
        assertThat(hearingNotices.get(0).getDocumentBinaryUrl(), is(GENERAL_APPLICATION_DIRECTIONS_DOCUMENT_BIN_URL));
    }

    private void assertCaseData(Map<String, Object> data) {
        assertThat(data, allOf(
            Matchers.<String, Object>hasEntry("ccdCaseNumber", 1234567890L),
            hasEntry("courtDetails", ImmutableMap.of(
                "courtName", "Hastings County Court And Family Court Hearing Centre",
                "courtAddress", "The Law Courts, Bohemia Road, Hastings, TN34 1QX",
                "phoneNumber", "01634 887900",
                "email", "FRCKSS@justice.gov.uk")),
            Matchers.<String, Object>hasEntry("applicantName", "Poor Guy"),
            Matchers.<String, Object>hasEntry("generalApplicationDirectionsHearingInformation", "Yes, many"),
            Matchers.<String, Object>hasEntry("hearingTime", "1pm"),
            Matchers.<String, Object>hasEntry("respondentName", "test Korivi"),
            Matchers.<String, Object>hasEntry("hearingVenue",
                "Hastings County Court And Family Court Hearing Centre, The Law Courts, Bohemia Road, Hastings, TN34 1QX")));
    }
}
