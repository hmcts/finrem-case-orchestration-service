package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.evidence.FileUploadResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementAuditService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.bulkPrintDocumentList;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_CONFIDENTIAL_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_CONFIDENTIAL_ADDRESS;

@ActiveProfiles("test-mock-feign-clients")
public class ConsentOrderPrintServiceTest extends BaseServiceTest {

    private static final UUID LETTER_ID = UUID.randomUUID();

    private final ArgumentCaptor<BulkPrintRequest> bulkPrintRequestArgumentCaptor = ArgumentCaptor.forClass(BulkPrintRequest.class);
    private final CaseDocument caseDocument = TestSetUpUtils.caseDocument();

    @Autowired private ObjectMapper mapper;
    @Autowired private ConsentOrderPrintService consentOrderPrintService;
    @MockBean private EvidenceManagementAuditService evidenceManagementAuditService;

    @MockBean
    private ConsentOrderNotApprovedDocumentService consentOrderNotApprovedDocumentService;
    @MockBean
    private GenerateCoverSheetService coverSheetService;
    @MockBean
    private NotificationService notificationService;
    @MockBean
    private GenericDocumentService genericDocumentService;
    @MockBean
    private ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;

    @Before
    public void init() {
        when(genericDocumentService.bulkPrint(any(), any(), any())).thenReturn(LETTER_ID);
    }

    @Test
    public void shouldSendForBulkPrintPackWithRespondentAndApplicantAddress() {
        final String consentedBulkPrintConsentOrderNotApprovedJson
            = "/fixtures/contested/bulk_print_consent_order_not_approved.json";
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(consentedBulkPrintConsentOrderNotApprovedJson, mapper);

        when(coverSheetService.generateRespondentCoverSheet(any(FinremCaseDetails.class), eq(AUTH_TOKEN))).thenReturn(caseDocument);
        when(coverSheetService.generateApplicantCoverSheet(any(FinremCaseDetails.class), eq(AUTH_TOKEN))).thenReturn(caseDocument);
        when(consentOrderNotApprovedDocumentService.prepareApplicantLetterPack(any(FinremCaseDetails.class), eq(AUTH_TOKEN)))
            .thenReturn(bulkPrintDocumentList());
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(FinremCaseDetails.class))).thenReturn(false);

        FinremCaseDetails resultingCaseDetails = consentOrderPrintService.sendConsentOrderToBulkPrint(caseDetails, AUTH_TOKEN);

        FinremCaseData caseData = resultingCaseDetails.getData();
        assertNotNull(caseData.getBulkPrintCoverSheetRes());
        assertEquals(caseData.getBulkPrintLetterIdRes(), LETTER_ID.toString());
        assertEquals(caseData.getBulkPrintLetterIdApp(), LETTER_ID.toString());

        verify(coverSheetService).generateRespondentCoverSheet(any(FinremCaseDetails.class), eq(AUTH_TOKEN));
        verify(consentOrderNotApprovedDocumentService).prepareApplicantLetterPack(any(FinremCaseDetails.class), eq(AUTH_TOKEN));
        verify(genericDocumentService, times(2)).bulkPrint(any(), any(), any());
    }

    @Test
    public void shouldSendForBulkPrintPackWithRespondentAndApplicantAddressAsSolicitorEmailIsNo() {
        final String consentedBulkPrintSimpleJson = "/fixtures/contested/bulk_print_simple.json";
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(consentedBulkPrintSimpleJson, mapper);
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(FinremCaseDetails.class))).thenReturn(false);
        when(coverSheetService.generateRespondentCoverSheet(any(FinremCaseDetails.class), eq(AUTH_TOKEN))).thenReturn(caseDocument);
        when(coverSheetService.generateApplicantCoverSheet(any(FinremCaseDetails.class), eq(AUTH_TOKEN))).thenReturn(caseDocument);

        FinremCaseDetails resultingCaseDetails = consentOrderPrintService.sendConsentOrderToBulkPrint(caseDetails, AUTH_TOKEN);

        FinremCaseData caseData = resultingCaseDetails.getData();
        assertNotNull(caseData.getBulkPrintCoverSheetRes());
        assertEquals(caseData.getBulkPrintLetterIdRes(), LETTER_ID.toString());
        assertEquals(caseData.getBulkPrintLetterIdApp(), LETTER_ID.toString());

        verify(coverSheetService).generateRespondentCoverSheet(any(FinremCaseDetails.class), eq(AUTH_TOKEN));
    }

    @Test
    public void shouldSendForBulkPrintPackWithOnlyRespondentAddress() {
        final String consentedBulkPrintConsentOrderApprovedJson
            = "/fixtures/contested/bulk_print_consent_order_approved.json";
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(consentedBulkPrintConsentOrderApprovedJson, mapper);
        List<BulkPrintDocument> bulkPrintDocuments = emptyList();
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        when(coverSheetService.generateRespondentCoverSheet(any(FinremCaseDetails.class), eq(AUTH_TOKEN))).thenReturn(caseDocument);
        when(coverSheetService.generateApplicantCoverSheet(any(FinremCaseDetails.class), eq(AUTH_TOKEN))).thenReturn(caseDocument);
        when(consentOrderApprovedDocumentService.prepareApplicantLetterPack(any(FinremCaseDetails.class), eq(AUTH_TOKEN)))
            .thenReturn(bulkPrintDocuments);

        FinremCaseDetails finremCaseDetails = consentOrderPrintService.sendConsentOrderToBulkPrint(caseDetails, AUTH_TOKEN);
        FinremCaseData caseData = finremCaseDetails.getData();

        assertNotNull(caseData.getBulkPrintCoverSheetRes());
        assertEquals(caseData.getBulkPrintLetterIdRes(), LETTER_ID.toString());

        verify(coverSheetService).generateRespondentCoverSheet(any(FinremCaseDetails.class), eq(AUTH_TOKEN));
    }

    @Test
    public void givenOrderNotApprovedFirstAndThenOrderIsApproved_WhenBulkPrinting_ThenConsentOrderApprovedDocumentsArePrinted() {
        final String consentedBulkPrintConsentOrderApprovedJson
            = "/fixtures/contested/bulk_print_consent_order_approved.json";
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(consentedBulkPrintConsentOrderApprovedJson, mapper);
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        when(coverSheetService.generateRespondentCoverSheet(any(FinremCaseDetails.class), eq(AUTH_TOKEN))).thenReturn(caseDocument);
        when(coverSheetService.generateApplicantCoverSheet(any(FinremCaseDetails.class), eq(AUTH_TOKEN))).thenReturn(caseDocument);

        FinremCaseDetails finremCaseDetails = consentOrderPrintService.sendConsentOrderToBulkPrint(caseDetails, AUTH_TOKEN);
        FinremCaseData caseData = finremCaseDetails.getData();

        assertEquals(caseData.getBulkPrintCoverSheetRes(), caseDocument);
        assertEquals(caseData.getBulkPrintLetterIdApp(), LETTER_ID.toString());
        assertEquals(caseData.getBulkPrintLetterIdRes(), LETTER_ID.toString());
    }

    @Test
    public void shouldPrintRespondentOrdersIfNotApprovedOrderMissing() {
        final String consentedBulkPrintConsentOrderNotApprovedJson
            = "/fixtures/contested/bulk_print_consent_order_not_approved.json";
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(consentedBulkPrintConsentOrderNotApprovedJson, mapper);
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        when(consentOrderNotApprovedDocumentService.prepareApplicantLetterPack(any(FinremCaseDetails.class), anyString()))
            .thenReturn(emptyList());
        when(coverSheetService.generateRespondentCoverSheet(any(FinremCaseDetails.class), eq(AUTH_TOKEN))).thenReturn(caseDocument);

        consentOrderPrintService.sendConsentOrderToBulkPrint(caseDetails, AUTH_TOKEN);

        verify(genericDocumentService).bulkPrint(bulkPrintRequestArgumentCaptor.capture(), any(), eq(AUTH_TOKEN));
        assertThat(bulkPrintRequestArgumentCaptor.getValue().getBulkPrintDocuments().stream().map(BulkPrintDocument::getBinaryFileUrl)
            .collect(Collectors.toList()), hasItem("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d/binary"));
    }

    @Test
    public void shouldPrintLettersForApplicant() {
        final String consentedBulkPrintConsentOrderNotApprovedJson
            = "/fixtures/contested/bulk_print_consent_order_not_approved.json";
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(consentedBulkPrintConsentOrderNotApprovedJson, mapper);
        List<BulkPrintDocument> bulkPrintDocuments = bulkPrintDocumentList();
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        when(coverSheetService.generateRespondentCoverSheet(any(FinremCaseDetails.class), eq(AUTH_TOKEN))).thenReturn(caseDocument);
        when(coverSheetService.generateApplicantCoverSheet(any(FinremCaseDetails.class), eq(AUTH_TOKEN))).thenReturn(caseDocument);
        when(consentOrderNotApprovedDocumentService.prepareApplicantLetterPack(any(FinremCaseDetails.class), eq(AUTH_TOKEN)))
            .thenReturn(bulkPrintDocuments);

        FinremCaseDetails finremCaseDetails = consentOrderPrintService.sendConsentOrderToBulkPrint(caseDetails, AUTH_TOKEN);
        FinremCaseData caseData = finremCaseDetails.getData();
        assertEquals(caseData.getBulkPrintLetterIdApp(), LETTER_ID.toString());
    }

    @Test
    public void shouldNotPrintLettersForApplicantIfNoDocuments() {
        final String consentedBulkPrintConsentOrderNotApprovedJson
            = "/fixtures/contested/bulk_print_consent_order_not_approved.json";
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(consentedBulkPrintConsentOrderNotApprovedJson, mapper);
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        when(coverSheetService.generateRespondentCoverSheet(any(FinremCaseDetails.class), eq(AUTH_TOKEN))).thenReturn(caseDocument);
        when(coverSheetService.generateApplicantCoverSheet(any(FinremCaseDetails.class), eq(AUTH_TOKEN))).thenReturn(caseDocument);
        when(consentOrderNotApprovedDocumentService.prepareApplicantLetterPack(any(FinremCaseDetails.class), eq(AUTH_TOKEN)))
            .thenReturn(emptyList());

        FinremCaseDetails finremCaseDetails = consentOrderPrintService.sendConsentOrderToBulkPrint(caseDetails, AUTH_TOKEN);
        FinremCaseData caseData = finremCaseDetails.getData();
        assertNull(caseData.getBulkPrintLetterIdApp());
    }

    @Test
    public void shouldSendForBulkPrintPackForConsentInContestedApprovedOrder() {
        final String consentedBulkPrintConsentOrderNotApprovedJson
            = "/fixtures/contested/consent-in-contested-application-approved.json";
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(consentedBulkPrintConsentOrderNotApprovedJson, mapper);
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        when(consentOrderNotApprovedDocumentService.prepareApplicantLetterPack(any(FinremCaseDetails.class), eq(AUTH_TOKEN)))
            .thenReturn(bulkPrintDocumentList());
        when(coverSheetService.generateRespondentCoverSheet(any(FinremCaseDetails.class), eq(AUTH_TOKEN))).thenReturn(caseDocument);
        when(coverSheetService.generateApplicantCoverSheet(any(FinremCaseDetails.class), eq(AUTH_TOKEN))).thenReturn(caseDocument);

        FinremCaseDetails finremCaseDetails = consentOrderPrintService.sendConsentOrderToBulkPrint(caseDetails, AUTH_TOKEN);
        FinremCaseData caseData = finremCaseDetails.getData();

        assertNotNull(caseData.getBulkPrintCoverSheetRes());
        assertEquals(caseData.getBulkPrintLetterIdRes(), LETTER_ID.toString());
        assertEquals(caseData.getBulkPrintLetterIdApp(), LETTER_ID.toString());

        verify(coverSheetService).generateRespondentCoverSheet(any(FinremCaseDetails.class), eq(AUTH_TOKEN));
        verify(genericDocumentService, times(2)).bulkPrint(any(), any(), any());
    }

    @Test
    public void shouldNotSendApplicantPackForPrintingIfConsentedForEmail() {
        final String consentedBulkPrintConsentOrderNotApprovedJson
            = "/fixtures/contested/consent-in-contested-application-approved.json";
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(consentedBulkPrintConsentOrderNotApprovedJson, mapper);
        caseDetails.getData().put("applicantSolicitorConsentForEmails", "Yes");
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(FinremCaseDetails.class))).thenReturn(true);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(any(FinremCaseDetails.class))).thenReturn(false);
        when(consentOrderNotApprovedDocumentService.prepareApplicantLetterPack(any(FinremCaseDetails.class), eq(AUTH_TOKEN)))
            .thenReturn(bulkPrintDocumentList());
        when(coverSheetService.generateRespondentCoverSheet(any(FinremCaseDetails.class), eq(AUTH_TOKEN))).thenReturn(caseDocument);
        when(coverSheetService.generateApplicantCoverSheet(any(FinremCaseDetails.class), eq(AUTH_TOKEN))).thenReturn(caseDocument);

        FinremCaseDetails finremCaseDetails = consentOrderPrintService.sendConsentOrderToBulkPrint(caseDetails, AUTH_TOKEN);
        FinremCaseData caseData = finremCaseDetails.getData();

        assertEquals(caseData.getBulkPrintCoverSheetRes(), caseDocument);
        assertEquals(caseData.getBulkPrintLetterIdRes(), LETTER_ID.toString());
        assertNull(caseData.getBulkPrintLetterIdApp());

        verify(coverSheetService).generateRespondentCoverSheet(any(FinremCaseDetails.class), eq(AUTH_TOKEN));
        verify(genericDocumentService, times(1)).bulkPrint(any(), any(), any());
    }

    @Test
    public void givenGeneralOrderIssuedAfterNotApprovedConsentOrder_whenSendOrderToBulkPrint_generalOrderIsPrinted() {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(FinremCaseDetails.class))).thenReturn(false);
        when(coverSheetService.generateRespondentCoverSheet(any(FinremCaseDetails.class), eq(AUTH_TOKEN))).thenReturn(caseDocument);
        when(coverSheetService.generateApplicantCoverSheet(any(FinremCaseDetails.class), eq(AUTH_TOKEN))).thenReturn(caseDocument);
        when(consentOrderNotApprovedDocumentService.prepareApplicantLetterPack(any(FinremCaseDetails.class), eq(AUTH_TOKEN)))
            .thenReturn(bulkPrintDocumentList());
        when(consentOrderNotApprovedDocumentService.notApprovedConsentOrder(any())).thenReturn(singletonList(caseDocument));
        when(evidenceManagementAuditService.audit(any(), eq(AUTH_TOKEN))).thenReturn(asList(
            FileUploadResponse.builder().modifiedOn(LocalDateTime.now().toString()).build(),
            FileUploadResponse.builder().modifiedOn(LocalDateTime.now().minusDays(2).toString()).build()));
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/contested/bulk_print_consent_order_not_approved.json", mapper);
        consentOrderPrintService.sendConsentOrderToBulkPrint(caseDetails, AUTH_TOKEN);

        verify(genericDocumentService, times(2)).bulkPrint(bulkPrintRequestArgumentCaptor.capture(), any(), eq(AUTH_TOKEN));
        assertThat(bulkPrintRequestArgumentCaptor.getValue().getBulkPrintDocuments().stream().map(BulkPrintDocument::getBinaryFileUrl)
            .collect(Collectors.toList()), hasItem("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d/binary"));
    }

    @Test
    public void shouldStoreConfidentialCoversheetsWhenAddressesAreConfidential() {
        final String consentedBulkPrintConsentOrderNotApprovedJson
            = "/fixtures/contested/bulk_print_consent_order_not_approved.json";
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(consentedBulkPrintConsentOrderNotApprovedJson, mapper);
        caseDetails.getData().put(APPLICANT_CONFIDENTIAL_ADDRESS, "Yes");
        caseDetails.getData().put(RESPONDENT_CONFIDENTIAL_ADDRESS, "Yes");
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(FinremCaseDetails.class))).thenReturn(false);
        when(coverSheetService.generateRespondentCoverSheet(any(FinremCaseDetails.class), eq(AUTH_TOKEN))).thenReturn(caseDocument);
        when(coverSheetService.generateApplicantCoverSheet(any(FinremCaseDetails.class), eq(AUTH_TOKEN))).thenReturn(caseDocument);
        when(consentOrderNotApprovedDocumentService.prepareApplicantLetterPack(any(FinremCaseDetails.class), eq(AUTH_TOKEN)))
            .thenReturn(bulkPrintDocumentList());

        FinremCaseDetails finremCaseDetails = consentOrderPrintService.sendConsentOrderToBulkPrint(caseDetails, AUTH_TOKEN);

        FinremCaseData caseData = finremCaseDetails.getData();

        assertEquals(caseData.getBulkPrintCoverSheetResConfidential(), caseDocument);
        assertNull(caseData.getBulkPrintCoverSheetRes());
        assertEquals(caseData.getBulkPrintCoverSheetAppConfidential(), caseDocument);
        assertNull(caseData.getBulkPrintCoverSheetApp());
    }

    @Test
    public void shouldNotPrintLettersForBothPartiesIfTheyAreDigital() {
        final String consentedBulkPrintConsentOrderNotApprovedJson
            = "/fixtures/contested/bulk_print_consent_order_not_approved.json";
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(consentedBulkPrintConsentOrderNotApprovedJson, mapper);

        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(FinremCaseDetails.class))).thenReturn(true);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(any(FinremCaseDetails.class))).thenReturn(true);

        consentOrderPrintService.sendConsentOrderToBulkPrint(caseDetails, AUTH_TOKEN);

        verify(coverSheetService, never()).generateApplicantCoverSheet(any(FinremCaseDetails.class), any());
        verify(coverSheetService, never()).generateRespondentCoverSheet(any(FinremCaseDetails.class), any());
    }
}
