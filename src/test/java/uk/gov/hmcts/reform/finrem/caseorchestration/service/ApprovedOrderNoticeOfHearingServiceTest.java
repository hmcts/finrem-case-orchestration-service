package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.CheckApplicantSolicitorIsDigitalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.CheckRespondentSolicitorIsDigitalService;
import uk.gov.hmcts.reform.finrem.ccd.domain.AdditionalHearingDocumentCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.DocumentCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.finremCaseDetailsFromResource;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.newDocument;

public class ApprovedOrderNoticeOfHearingServiceTest extends BaseServiceTest {
    private static final String LATEST_DRAFT_ORDER_DOCUMENT_BIN_URL = "http://dm-store/1frea-ldo-doc/binary";
    private static final String GENERAL_APPLICATION_DIRECTIONS_DOCUMENT_BIN_URL = "http://dm-store/1f3a-gads-doc/binary";
    public static final String GENERAL_APPLICATION_DIRECTIONS_JSON = "/fixtures/general-application-directions.json";

    @Autowired
    private ApprovedOrderNoticeOfHearingService approvedOrderNoticeOfHearingService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private DocumentConfiguration documentConfiguration;
    @Autowired
    private AdditionalHearingDocumentService additionalHearingDocumentService;

    @MockBean
    private BulkPrintService bulkPrintService;
    @MockBean
    private GenericDocumentService genericDocumentService;
    @MockBean
    private CheckApplicantSolicitorIsDigitalService checkApplicantSolicitorIsDigitalService;
    @MockBean
    private CheckRespondentSolicitorIsDigitalService checkRespondentSolicitorIsDigitalService;
    @MockBean
    private NotificationService notificationService;
    @MockBean
    private DocumentHelper documentHelper;

    @Captor
    ArgumentCaptor<Map<String, Object>> placeholdersMapCaptor;
    @Captor
    ArgumentCaptor<List<BulkPrintDocument>> printDocumentsRequestDocumentListCaptor;

    private FinremCaseDetails caseDetails;

    @Before
    public void setup() throws IOException {
        caseDetails = finremCaseDetailsFromResource(getResource(GENERAL_APPLICATION_DIRECTIONS_JSON), objectMapper);
        when(genericDocumentService.generateDocumentFromPlaceholdersMap(any(), any(), any(), any())).thenReturn(newDocument(DOC_URL, FILE_NAME,
            GENERAL_APPLICATION_DIRECTIONS_DOCUMENT_BIN_URL));
    }

    @Test
    public void givenHearingRequired_whenSubmitNoticeOfHearing_thenHearingNoticeIsPrinted() {
        caseDetails.getCaseData().getContactDetailsWrapper().setApplicantFmName("Poor");
        caseDetails.getCaseData().getContactDetailsWrapper().setApplicantLname("Guy");
        caseDetails.getCaseData().getContactDetailsWrapper().setRespondentFmName("test");
        caseDetails.getCaseData().getContactDetailsWrapper().setRespondentLname("Korivi");

        approvedOrderNoticeOfHearingService.createAndStoreHearingNoticeDocumentPack(caseDetails, AUTH_TOKEN);

        assertCaseDataHasHearingNoticesCollection();
        assertDocumentServiceInteraction();

        Map<String, Object> data = getDataFromCaptor(placeholdersMapCaptor);
        assertCaseData(data);
    }

    @Test
    public void givenDraftHearingOrderIsUploaded_whenSubmitNoticeOfHearing_thenOrderIsPrinted() {
        caseDetails.getCaseData().getContactDetailsWrapper().setApplicantFmName("Poor");
        caseDetails.getCaseData().getContactDetailsWrapper().setApplicantLname("Guy");
        caseDetails.getCaseData().getContactDetailsWrapper().setRespondentFmName("test");
        caseDetails.getCaseData().getContactDetailsWrapper().setRespondentLname("Korivi");

        caseDetails.getCaseData().setLatestDraftHearingOrder(Document.builder().binaryUrl(LATEST_DRAFT_ORDER_DOCUMENT_BIN_URL).build());
        approvedOrderNoticeOfHearingService.createAndStoreHearingNoticeDocumentPack(caseDetails, AUTH_TOKEN);

        assertCaseDataHasHearingNoticesCollection();

        assertDocumentServiceInteraction();

        Map<String, Object> data = getDataFromCaptor(placeholdersMapCaptor);

        assertCaseData(data);
    }

    @Test
    public void givenSubmittedCallbackReceived_whenSubmitNotice_thenSendNoticeOfHearingToAppAndResp() {
        caseDetails.getCaseData().setHearingNoticeDocumentPack(buildHearingNoticePack());
        when(checkApplicantSolicitorIsDigitalService.isSolicitorDigital(caseDetails)).thenReturn(false);
        when(checkRespondentSolicitorIsDigitalService.isSolicitorDigital(caseDetails)).thenReturn(false);
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(caseDetails.getCaseData())).thenReturn(true);
        caseDetails.getCaseData().getContactDetailsWrapper().setApplicantSolicitorConsentForEmails(YesOrNo.YES);
        caseDetails.getCaseData().setRespSolNotificationsEmailConsent(YesOrNo.YES);
        when(documentHelper.getDocumentsAsBulkPrintDocuments(any())).thenReturn(List.of(BulkPrintDocument
                .builder()
                .binaryFileUrl(GENERAL_APPLICATION_DIRECTIONS_DOCUMENT_BIN_URL)
                .build(),
            BulkPrintDocument
                .builder()
                .binaryFileUrl(LATEST_DRAFT_ORDER_DOCUMENT_BIN_URL)
                .build()));
        approvedOrderNoticeOfHearingService.printHearingNoticePackAndSendToApplicantAndRespondent(caseDetails, AUTH_TOKEN);

        assertBulkPrintServiceInteraction();

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

    @Test
    public void givenSubmittedCallbackReceived_whenSubmitNotice_thenSendNoticeOfHearingEmailToAppAndResp() {
        caseDetails.getCaseData().setHearingNoticeDocumentPack(buildHearingNoticePack());

        when(checkApplicantSolicitorIsDigitalService.isSolicitorDigital(caseDetails)).thenReturn(true);
        when(checkRespondentSolicitorIsDigitalService.isSolicitorDigital(caseDetails)).thenReturn(true);
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(caseDetails.getCaseData())).thenReturn(true);
        caseDetails.getCaseData().getContactDetailsWrapper().setApplicantSolicitorConsentForEmails(YesOrNo.YES);
        caseDetails.getCaseData().setRespSolNotificationsEmailConsent(YesOrNo.YES);
        approvedOrderNoticeOfHearingService.printHearingNoticePackAndSendToApplicantAndRespondent(caseDetails, AUTH_TOKEN);

        assertNotificationServiceInteraction();
    }

    private void assertDocumentServiceInteraction() {
        verify(genericDocumentService, times(1)).generateDocumentFromPlaceholdersMap(
            eq(AUTH_TOKEN),
            placeholdersMapCaptor.capture(),
            eq(documentConfiguration.getAdditionalHearingTemplate()),
            eq(documentConfiguration.getAdditionalHearingFileName()));
    }

    private void assertBulkPrintServiceInteraction() {
        verify(bulkPrintService, times(1)).printApplicantDocuments(any(), eq(AUTH_TOKEN), any());
        verify(bulkPrintService, times(1)).printRespondentDocuments(any(), eq(AUTH_TOKEN),
            printDocumentsRequestDocumentListCaptor.capture());
    }

    private void assertNotificationServiceInteraction() {
        verify(notificationService, times(1)).sendPrepareForHearingEmailApplicant(caseDetails);
        verify(notificationService, times(1)).sendPrepareForHearingEmailRespondent(caseDetails);
    }

    private void assertCaseDataHasHearingNoticesCollection() {
        assertThat(caseDetails.getCaseData().getAdditionalHearingDocuments(), is(notNullValue()));
        List<AdditionalHearingDocumentCollection> additionalHearingDocuments = caseDetails.getCaseData().getAdditionalHearingDocuments();
        assertThat(additionalHearingDocuments.size(), is(1));
        assertThat(additionalHearingDocuments.get(0).getValue().getAdditionalHearingDocument().getBinaryUrl(),
            is(GENERAL_APPLICATION_DIRECTIONS_DOCUMENT_BIN_URL));
    }

    private void assertCaseData(Map<String, Object> data) {
        assertThat(data, allOf(
            Matchers.<String, Object>hasEntry("CCDCaseNumber", 1234567890L),
            Matchers.hasEntry("CourtName", "Hastings County Court And Family Court Hearing Centre"),
            Matchers.hasEntry("CourtAddress", "The Law Courts, Bohemia Road, Hastings, TN34 1QX"),
            Matchers.hasEntry("CourtPhone", "01634 887900"),
            Matchers.hasEntry("CourtEmail", "FRCKSS@justice.gov.uk"),
            Matchers.hasEntry("ApplicantName", "Poor Guy"),
            Matchers.hasEntry("HearingTime", "1pm"),
            Matchers.hasEntry("RespondentName", "test Korivi"),
            Matchers.hasEntry("HearingVenue",
                "Hastings County Court And Family Court Hearing Centre, The Law Courts, Bohemia Road, Hastings, TN34 1QX")));
    }

    private List<DocumentCollection> buildHearingNoticePack() {
        return List.of(DocumentCollection.builder()
                .value(Document.builder().binaryUrl(GENERAL_APPLICATION_DIRECTIONS_DOCUMENT_BIN_URL).build())
                .build(),
            DocumentCollection.builder().value(Document.builder()
                .binaryUrl(LATEST_DRAFT_ORDER_DOCUMENT_BIN_URL)
                .build()).build());
    }
}
