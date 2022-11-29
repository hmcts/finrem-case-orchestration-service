package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Element;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.CheckApplicantSolicitorIsDigitalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.CheckRespondentSolicitorIsDigitalService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasKey;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CASE_TYPE_ID_CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CASE_TYPE_ID_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDetailsFromResource;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ADDITIONAL_HEARING_DOCUMENT_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ANOTHER_HEARING_TO_BE_LISTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_NOTICE_DOCUMENT_PACK;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LATEST_DRAFT_HEARING_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Element.element;

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
    private CaseDataService caseDataService;
    @MockBean
    private NotificationService notificationService;
    @MockBean
    private DocumentHelper documentHelper;

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
    public void givenHearingRequired_whenSubmitNoticeOfHearing_thenHearingNoticeIsPrintedForContestedCase() {
        when(documentHelper.getApplicantFullName(caseDetails)).thenReturn("Poor Guy");
        when(caseDataService.buildFullRespondentName(caseDetails)).thenReturn("test Korivi");
        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(false);

        caseDetails.getData().put(ANOTHER_HEARING_TO_BE_LISTED, YES_VALUE);
        caseDetails.setCaseTypeId(CASE_TYPE_ID_CONTESTED);
        approvedOrderNoticeOfHearingService.createAndStoreHearingNoticeDocumentPack(caseDetails, AUTH_TOKEN);

        assertCaseDataHasHearingNoticesCollection();
        assertDocumentServiceInteraction();

        Map<String, Object> caseDetailsMap = (Map) placeholdersMapCaptor.getValue().get(CASE_DETAILS);
        Map<String, Object> data = (Map) caseDetailsMap.get(CASE_DATA);
        assertCaseData(data);
    }

    @Test
    public void givenHearingRequired_whenSubmitNoticeOfHearing_thenHearingNoticeIsPrintedForConsentedCase() {
        when(documentHelper.getApplicantFullName(caseDetails)).thenReturn("Poor Guy");
        when(caseDataService.buildFullRespondentName(caseDetails)).thenReturn("test Korivi");
        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(true);

        caseDetails.getData().put(ANOTHER_HEARING_TO_BE_LISTED, YES_VALUE);
        caseDetails.setCaseTypeId(CASE_TYPE_ID_CONSENTED);
        approvedOrderNoticeOfHearingService.createAndStoreHearingNoticeDocumentPack(caseDetails, AUTH_TOKEN);

        assertCaseDataHasHearingNoticesCollection();
        assertDocumentServiceInteraction();

        Map<String, Object> caseDetailsMap = (Map) placeholdersMapCaptor.getValue().get(CASE_DETAILS);
        Map<String, Object> data = (Map) caseDetailsMap.get(CASE_DATA);
        assertCaseData(data);
    }

    @Test
    public void givenDraftHearingOrderIsUploaded_whenSubmitNoticeOfHearing_thenOrderIsPrinted() {
        when(documentHelper.getApplicantFullName(caseDetails)).thenReturn("Poor Guy");
        when(caseDataService.buildFullRespondentName(caseDetails)).thenReturn("test Korivi");

        caseDetails.getData().put(ANOTHER_HEARING_TO_BE_LISTED, YES_VALUE);
        caseDetails.getData().put(LATEST_DRAFT_HEARING_ORDER, CaseDocument.builder().documentBinaryUrl(LATEST_DRAFT_ORDER_DOCUMENT_BIN_URL).build());
        approvedOrderNoticeOfHearingService.createAndStoreHearingNoticeDocumentPack(caseDetails, AUTH_TOKEN);

        assertCaseDataHasHearingNoticesCollection();

        assertDocumentServiceInteraction();

        Map<String, Object> caseDetailsMap = (Map) placeholdersMapCaptor.getValue().get(CASE_DETAILS);
        Map<String, Object> data = (Map) caseDetailsMap.get(CASE_DATA);

        assertCaseData(data);
    }

    @Test
    public void givenSubmittedCallbackReceived_whenSubmitNotice_thenSendNoticeOfHearingToAppAndResp() {
        caseDetails.getData().put(HEARING_NOTICE_DOCUMENT_PACK, buildHearingNoticePack());
        when(checkApplicantSolicitorIsDigitalService.isSolicitorDigital(caseDetails)).thenReturn(false);
        when(checkRespondentSolicitorIsDigitalService.isSolicitorDigital(caseDetails)).thenReturn(false);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(caseDetails)).thenReturn(true);
        when(caseDataService.isRespondentSolicitorAgreeToReceiveEmails(caseDetails)).thenReturn(true);
        when(documentHelper.getCaseDocumentsAsBulkPrintDocuments(any())).thenReturn(List.of(BulkPrintDocument
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
        caseDetails.getData().put(HEARING_NOTICE_DOCUMENT_PACK, buildHearingNoticePack());
        when(checkApplicantSolicitorIsDigitalService.isSolicitorDigital(caseDetails)).thenReturn(true);
        when(checkRespondentSolicitorIsDigitalService.isSolicitorDigital(caseDetails)).thenReturn(true);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(caseDetails)).thenReturn(true);
        when(caseDataService.isRespondentSolicitorAgreeToReceiveEmails(caseDetails)).thenReturn(true);
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
        assertThat(caseDetails.getData(), hasKey(ADDITIONAL_HEARING_DOCUMENT_COLLECTION));
        List<Element<AdditionalHearingDocument>> additionalHearingDocuments = objectMapper.convertValue(
            caseDetails.getData().get(ADDITIONAL_HEARING_DOCUMENT_COLLECTION), new TypeReference<>() {
            });
        assertThat(additionalHearingDocuments.size(), is(1));
        assertThat(additionalHearingDocuments.get(0).getValue().getDocument().getDocumentBinaryUrl(),
            is(GENERAL_APPLICATION_DIRECTIONS_DOCUMENT_BIN_URL));
    }

    private void assertCaseData(Map<String, Object> data) {
        assertThat(data, allOf(
            Matchers.<String, Object>hasEntry("CCDCaseNumber", 1234567890L),
            Matchers.hasEntry("CourtName", "Hastings County Court And Family Court Hearing Centre"),
            Matchers.hasEntry("CourtAddress", "The Law Courts, Bohemia Road, Hastings, TN34 1QX"),
            Matchers.hasEntry("CourtPhone", "0300 1235577"),
            Matchers.hasEntry("CourtEmail", "hastingsfamily@justice.gov.uk"),
            Matchers.hasEntry("ApplicantName", "Poor Guy"),
            Matchers.<String, Object>hasEntry("HearingTime", "1pm"),
            Matchers.<String, Object>hasEntry("RespondentName", "test Korivi"),
            Matchers.<String, Object>hasEntry("HearingVenue",
                "Hastings County Court And Family Court Hearing Centre, The Law Courts, Bohemia Road, Hastings, TN34 1QX")));
    }

    private List<Element<CaseDocument>> buildHearingNoticePack() {
        return List.of(element(UUID.randomUUID(), CaseDocument.builder()
                .documentBinaryUrl(GENERAL_APPLICATION_DIRECTIONS_DOCUMENT_BIN_URL)
                .build()),
            element(UUID.randomUUID(), CaseDocument.builder()
                .documentBinaryUrl(LATEST_DRAFT_ORDER_DOCUMENT_BIN_URL)
                .build()));
    }
}
