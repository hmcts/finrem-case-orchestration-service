package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOne;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintRequest;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.bulkPrintDocumentList;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_CONFIDENTIAL_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_COVER_SHEET_APP;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_COVER_SHEET_APP_CONFIDENTIAL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_COVER_SHEET_RES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_COVER_SHEET_RES_CONFIDENTIAL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_CONFIDENTIAL_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_ONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService.FINANCIAL_REMEDY_GENERAL_LETTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService.FINANCIAL_REMEDY_PACK_LETTER_TYPE;

public class BulkPrintServiceTest extends BaseServiceTest {

    @Autowired
    private BulkPrintService bulkPrintService;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private DocumentHelper documentHelper;

    @Autowired
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @MockitoBean
    private GenerateCoverSheetService coverSheetService;
    @MockitoBean
    private GenericDocumentService genericDocumentService;
    @MockitoBean
    private PaperNotificationService paperNotificationService;

    private final CaseDocument caseDocument = TestSetUpUtils.caseDocument();
    private UUID letterId;
    private ArgumentCaptor<BulkPrintRequest> bulkPrintRequestArgumentCaptor;

    @Before
    public void setUp() {
        letterId = UUID.randomUUID();
        bulkPrintRequestArgumentCaptor = ArgumentCaptor.forClass(BulkPrintRequest.class);
        when(genericDocumentService.bulkPrint(any())).thenReturn(letterId);
    }

    @Test
    public void shouldSendDocumentForBulkPrint() {
        UUID bulkPrintLetterId = bulkPrintService.sendDocumentForPrint(
            new CaseDocument(), caseDetails(), APPLICANT, AUTH_TOKEN);

        assertThat(bulkPrintLetterId).isEqualTo(letterId);

        verify(genericDocumentService).bulkPrint(bulkPrintRequestArgumentCaptor.capture());

        BulkPrintRequest bulkPrintRequest = bulkPrintRequestArgumentCaptor.getValue();
        verifyBulkPrintRequest(bulkPrintRequest, APPLICANT);
    }

    @Test
    public void shouldSendDocumentForBulkPrintFinRem() {
        UUID bulkPrintLetterId = bulkPrintService.sendDocumentForPrint(
            new CaseDocument(), finremCaseDetails(), RESPONDENT, AUTH_TOKEN);

         assertThat(bulkPrintLetterId).isEqualTo(letterId);

        verify(genericDocumentService).bulkPrint(bulkPrintRequestArgumentCaptor.capture());

        BulkPrintRequest bulkPrintRequest = bulkPrintRequestArgumentCaptor.getValue();
        verifyBulkPrintRequest(bulkPrintRequest, RESPONDENT);
    }

    @Test
    public void whenPrintingDocument_thenDocumentIsSentToPrinting() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource("/fixtures/general-letter.json", mapper);
        UUID bulkPrintLetterId = bulkPrintService.sendDocumentForPrint(caseDocument(), caseDetails, APPLICANT, AUTH_TOKEN);

         assertThat(bulkPrintLetterId).isEqualTo(letterId);
    }

    @Test
    public void getRecipientInCamelCase() {
        String recipient = bulkPrintService.getRecipient("APPLICANT_CONFIDENTIAL_SOLICITOR");
        assertEquals("ApplicantConfidentialSolicitor", recipient);
    }

    @Test
    public void whenPrintingDocument_thenDocumentIsSentToPrintingFinrem() {
        FinremCaseDetails caseDetails = TestSetUpUtils.finremCaseDetailsFromResource("/fixtures/general-letter.json", mapper);
        UUID bulkPrintLetterId = bulkPrintService.sendDocumentForPrint(caseDocument(), caseDetails, APPLICANT, AUTH_TOKEN);

         assertThat(bulkPrintLetterId).isEqualTo(letterId);
    }

    @Test
    public void shouldPrintApplicantDocuments() {
        final String consentedBulkPrintConsentOrderNotApprovedJson
            = "/fixtures/contested/bulk_print_consent_order_not_approved.json";
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(consentedBulkPrintConsentOrderNotApprovedJson, mapper);
        List<BulkPrintDocument> bulkPrintDocuments = bulkPrintDocumentList();

        when(coverSheetService.generateApplicantCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(caseDocument);
        when(genericDocumentService.bulkPrint(bulkPrintRequestArgumentCaptor.capture())).thenReturn(letterId);

        UUID uuid = bulkPrintService.printApplicantDocuments(caseDetails, AUTH_TOKEN, bulkPrintDocuments);

        assertThat(uuid).isEqualTo(letterId);

        verify(coverSheetService).generateApplicantCoverSheet(caseDetails, AUTH_TOKEN);
        verify(genericDocumentService).bulkPrint(bulkPrintRequestArgumentCaptor.capture());

        BulkPrintRequest bulkPrintRequest = bulkPrintRequestArgumentCaptor.getValue();
        verifyBulkPrintRequest(bulkPrintRequest, bulkPrintDocuments, caseDetails.getId(), APPLICANT);

        assertThat(caseDetails.getData().containsKey(BULK_PRINT_COVER_SHEET_APP)).isTrue();
    }

    @Test
    public void shouldPrintApplicantDocumentsFinRem() {
        final String consentedBulkPrintConsentOrderNotApprovedJson
            = "/fixtures/contested/bulk_print_consent_order_not_approved.json";
        FinremCaseDetails caseDetails = TestSetUpUtils.finremCaseDetailsFromResource(consentedBulkPrintConsentOrderNotApprovedJson, mapper);
        List<BulkPrintDocument> bulkPrintDocuments = bulkPrintDocumentList();

        when(coverSheetService.generateApplicantCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(caseDocument);
        when(genericDocumentService.bulkPrint(bulkPrintRequestArgumentCaptor.capture())).thenReturn(letterId);

        UUID uuid = bulkPrintService.printApplicantDocuments(caseDetails, AUTH_TOKEN, bulkPrintDocuments);

        assertThat(uuid).isEqualTo(letterId);

        verify(coverSheetService).generateApplicantCoverSheet(caseDetails, AUTH_TOKEN);
        verify(genericDocumentService).bulkPrint(bulkPrintRequestArgumentCaptor.capture());

        BulkPrintRequest bulkPrintRequest = bulkPrintRequestArgumentCaptor.getValue();
        verifyBulkPrintRequest(bulkPrintRequest, bulkPrintDocuments, caseDetails.getId(), APPLICANT);

        assertThat(caseDetails.getData().getBulkPrintCoversheetWrapper().getBulkPrintCoverSheetApp()).isEqualTo(caseDocument); 
    }

    @Test
    public void shouldPrintRespondentDocuments() {
        final String consentedBulkPrintConsentOrderNotApprovedJson
            = "/fixtures/contested/bulk_print_consent_order_not_approved.json";
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(consentedBulkPrintConsentOrderNotApprovedJson, mapper);
        List<BulkPrintDocument> bulkPrintDocuments = bulkPrintDocumentList();

        when(coverSheetService.generateRespondentCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(caseDocument);
        when(genericDocumentService.bulkPrint(bulkPrintRequestArgumentCaptor.capture())).thenReturn(letterId);

        UUID uuid = bulkPrintService.printRespondentDocuments(caseDetails, AUTH_TOKEN, bulkPrintDocuments);

        assertThat(uuid).isEqualTo(letterId);

        verify(coverSheetService).generateRespondentCoverSheet(caseDetails, AUTH_TOKEN);
        verify(genericDocumentService).bulkPrint(bulkPrintRequestArgumentCaptor.capture());

        BulkPrintRequest bulkPrintRequest = bulkPrintRequestArgumentCaptor.getValue();
        verifyBulkPrintRequest(bulkPrintRequest, bulkPrintDocuments, caseDetails.getId(), RESPONDENT);

        assertThat(caseDetails.getData().containsKey(BULK_PRINT_COVER_SHEET_RES)).isTrue();
    }

    @Test
    public void shouldPrintRespondentDocumentsFinrem() {
        final String consentedBulkPrintConsentOrderNotApprovedJson
            = "/fixtures/contested/bulk_print_consent_order_not_approved.json";
        FinremCaseDetails caseDetails = TestSetUpUtils.finremCaseDetailsFromResource(consentedBulkPrintConsentOrderNotApprovedJson, mapper);
        List<BulkPrintDocument> bulkPrintDocuments = bulkPrintDocumentList();

        when(coverSheetService.generateRespondentCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(caseDocument);
        when(genericDocumentService.bulkPrint(bulkPrintRequestArgumentCaptor.capture())).thenReturn(letterId);

        UUID uuid = bulkPrintService.printRespondentDocuments(caseDetails, AUTH_TOKEN, bulkPrintDocuments);

        assertThat(uuid).isEqualTo(letterId);

        verify(coverSheetService).generateRespondentCoverSheet(caseDetails, AUTH_TOKEN);
        verify(genericDocumentService).bulkPrint(bulkPrintRequestArgumentCaptor.capture());

        BulkPrintRequest bulkPrintRequest = bulkPrintRequestArgumentCaptor.getValue();
        verifyBulkPrintRequest(bulkPrintRequest, bulkPrintDocuments, caseDetails.getId(), RESPONDENT);

        assertThat(caseDetails.getData().getBulkPrintCoversheetWrapper().getBulkPrintCoverSheetRes()).isEqualTo(caseDocument);
    }

    @Test
    public void shouldConvertCaseDocumentToBulkPrintDocument() {
        BulkPrintDocument bulkPrintDoc = documentHelper.getBulkPrintDocumentFromCaseDocument(caseDocument());
        assertThat(bulkPrintDoc.getBinaryFileUrl()).isEqualTo(BINARY_URL);
        assertThat(bulkPrintDoc.getFileName()).isEqualTo(FILE_NAME);
    }

    @Test
    public void shouldNotPrintForApplicantIfRepresentedAgreedToEmailAndNotPaperCase() {
        final String json
            = "/fixtures/refusal-order-contested.json";
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(json, mapper);

        assertThat(paperNotificationService.shouldPrintForApplicant(caseDetails)).isFalse();
    }

    @Test
    public void shouldSaveApplicantDocumentsToConfidentialCollectionWhenAddressIsConfidential() {
        final String consentedBulkPrintConsentOrderNotApprovedJson
            = "/fixtures/contested/bulk_print_consent_order_not_approved.json";
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(consentedBulkPrintConsentOrderNotApprovedJson, mapper);
        caseDetails.getData().put(APPLICANT_CONFIDENTIAL_ADDRESS, "Yes");
        List<BulkPrintDocument> bulkPrintDocuments = bulkPrintDocumentList();

        when(coverSheetService.generateApplicantCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(caseDocument);
        when(genericDocumentService.bulkPrint(bulkPrintRequestArgumentCaptor.capture())).thenReturn(letterId);

        UUID uuid = bulkPrintService.printApplicantDocuments(caseDetails, AUTH_TOKEN, bulkPrintDocuments);

        assertThat(uuid).isEqualTo(letterId);
        assertThat(caseDetails.getData().containsKey(BULK_PRINT_COVER_SHEET_APP_CONFIDENTIAL)).isTrue();
        assertThat(caseDetails.getData().containsKey(BULK_PRINT_COVER_SHEET_APP)).isFalse();
    }

    @Test
    public void shouldSaveApplicantDocumentsToConfidentialCollectionWhenAddressIsConfidentialFinrem() {
        final String consentedBulkPrintConsentOrderNotApprovedJson
            = "/fixtures/contested/bulk_print_consent_order_not_approved.json";
        FinremCaseDetails caseDetails = TestSetUpUtils.finremCaseDetailsFromResource(consentedBulkPrintConsentOrderNotApprovedJson, mapper);
        caseDetails.getData().getContactDetailsWrapper().setApplicantAddressHiddenFromRespondent(YesOrNo.YES);
        List<BulkPrintDocument> bulkPrintDocuments = bulkPrintDocumentList();

        when(coverSheetService.generateApplicantCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(caseDocument);
        when(genericDocumentService.bulkPrint(bulkPrintRequestArgumentCaptor.capture())).thenReturn(letterId);

        UUID uuid = bulkPrintService.printApplicantDocuments(caseDetails, AUTH_TOKEN, bulkPrintDocuments);

        assertThat(uuid).isEqualTo(letterId);
        assertThat(caseDetails.getData().getBulkPrintCoversheetWrapper().getBulkPrintCoverSheetAppConfidential()).isEqualTo(caseDocument);
        assertNull(caseDetails.getData().getBulkPrintCoversheetWrapper().getBulkPrintCoverSheetApp());
    }

    @Test
    public void shouldSaveRespondentDocumentsToConfidentialCollectionWhenAddressIsConfidential() {
        final String consentedBulkPrintConsentOrderNotApprovedJson
            = "/fixtures/contested/bulk_print_consent_order_not_approved.json";
        FinremCaseDetails caseDetails = TestSetUpUtils.finremCaseDetailsFromResource(consentedBulkPrintConsentOrderNotApprovedJson, mapper);
        caseDetails.getData().getContactDetailsWrapper().setRespondentAddressHiddenFromApplicant(YesOrNo.YES);
        List<BulkPrintDocument> bulkPrintDocuments = bulkPrintDocumentList();

        when(coverSheetService.generateRespondentCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(caseDocument);
        when(genericDocumentService.bulkPrint(bulkPrintRequestArgumentCaptor.capture())).thenReturn(letterId);

        UUID uuid = bulkPrintService.printRespondentDocuments(caseDetails, AUTH_TOKEN, bulkPrintDocuments);

        assertThat(uuid).isEqualTo(letterId);
        assertThat(caseDetails.getData().getBulkPrintCoversheetWrapper().getBulkPrintCoverSheetResConfidential()).isEqualTo(caseDocument);
        assertNull(caseDetails.getData().getBulkPrintCoversheetWrapper().getBulkPrintCoverSheetRes());
    }

    @Test
    public void shouldSaveRespondentDocumentsToConfidentialCollectionWhenAddressIsConfidentialFinrem() {
        final String consentedBulkPrintConsentOrderNotApprovedJson
            = "/fixtures/contested/bulk_print_consent_order_not_approved.json";
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(consentedBulkPrintConsentOrderNotApprovedJson, mapper);
        caseDetails.getData().put(RESPONDENT_CONFIDENTIAL_ADDRESS, "Yes");
        List<BulkPrintDocument> bulkPrintDocuments = bulkPrintDocumentList();

        when(coverSheetService.generateRespondentCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(caseDocument);
        when(genericDocumentService.bulkPrint(bulkPrintRequestArgumentCaptor.capture())).thenReturn(letterId);

        UUID uuid = bulkPrintService.printRespondentDocuments(caseDetails, AUTH_TOKEN, bulkPrintDocuments);

        assertThat(uuid).isEqualTo(letterId);
        assertThat(caseDetails.getData().containsKey(BULK_PRINT_COVER_SHEET_RES_CONFIDENTIAL)).isTrue();
        assertThat(caseDetails.getData().containsKey(BULK_PRINT_COVER_SHEET_RES)).isFalse();
    }

    @Test
    public void shouldPrintIntervenerDocumentsFinrem() {
        final String contestedBulkPrintConsentIntervener1Json
            = "/fixtures/bulkprint/bulk-print-intervener1-notrepresented.json";
        FinremCaseDetails caseDetails = TestSetUpUtils.finremCaseDetailsFromResource(contestedBulkPrintConsentIntervener1Json, mapper);
        List<BulkPrintDocument> bulkPrintDocuments = bulkPrintDocumentList();

        when(coverSheetService.generateIntervenerCoverSheet(caseDetails, AUTH_TOKEN, DocumentHelper.PaperNotificationRecipient.INTERVENER_ONE))
            .thenReturn(caseDocument);
        when(genericDocumentService.bulkPrint(bulkPrintRequestArgumentCaptor.capture())).thenReturn(letterId);

        IntervenerOne intervenerOne = caseDetails.getData().getIntervenerOne();

        UUID uuid = bulkPrintService.printIntervenerDocuments(intervenerOne, caseDetails, AUTH_TOKEN, bulkPrintDocuments);

        assertThat(uuid).isEqualTo(letterId);

        verify(coverSheetService).generateIntervenerCoverSheet(caseDetails, AUTH_TOKEN, DocumentHelper.PaperNotificationRecipient.INTERVENER_ONE);
        verify(genericDocumentService).bulkPrint(bulkPrintRequestArgumentCaptor.capture());

        BulkPrintRequest bulkPrintRequest = bulkPrintRequestArgumentCaptor.getValue();
        verifyBulkPrintRequest(bulkPrintRequest, bulkPrintDocuments, caseDetails.getId(), INTERVENER_ONE);
    }

    @Test
    public void shouldThrowExceptionWhenNonPdfDocumentIsProvided() {
        final String consentedBulkPrintConsentOrderNotApprovedJson
            = "/fixtures/contested/bulk_print_consent_order_not_approved.json";
        FinremCaseDetails caseDetails = TestSetUpUtils.finremCaseDetailsFromResource(consentedBulkPrintConsentOrderNotApprovedJson, mapper);
        caseDetails.getData().getContactDetailsWrapper().setApplicantAddressHiddenFromRespondent(YesOrNo.YES);
        List<BulkPrintDocument> bulkPrintDocuments = List.of(BulkPrintDocument.builder()
            .fileName("Word Document.docx")
            .binaryFileUrl("http://dm-store-aat.service.core-compute-aat.internal/documents/967103ad-0b95-4f0f-9712-4bf5770fb196/binary").build());

        when(coverSheetService.generateApplicantCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(caseDocument);
        when(genericDocumentService.bulkPrint(bulkPrintRequestArgumentCaptor.capture())).thenReturn(letterId);

        assertThrows(IllegalArgumentException.class, () -> {
            bulkPrintService.printApplicantDocuments(caseDetails, AUTH_TOKEN, bulkPrintDocuments);
        });
    }

    @Test
    public void shouldConvertCaseDocumentsToBulkPrintDocumentsSuccessfully() {
        CaseDocument caseDocument1 = CaseDocument.builder()
            .documentFilename("test-file-1.pdf")
            .documentBinaryUrl("http://test.url/1")
            .build();

        CaseDocument caseDocument2 = CaseDocument.builder()
            .documentFilename("test-file-2.pdf")
            .documentBinaryUrl("http://test.url/2")
            .build();

        List<CaseDocument> caseDocuments = List.of(caseDocument1, caseDocument2);

        when(genericDocumentService.convertDocumentIfNotPdfAlready(caseDocument1, AUTH_TOKEN, CaseType.CONSENTED))
            .thenReturn(caseDocument1);
        when(genericDocumentService.convertDocumentIfNotPdfAlready(caseDocument2, AUTH_TOKEN, CaseType.CONSENTED))
            .thenReturn(caseDocument2);

        List<BulkPrintDocument> bulkPrintDocuments = bulkPrintService.convertCaseDocumentsToBulkPrintDocuments(caseDocuments, AUTH_TOKEN, CaseType.CONSENTED);

        assertThat(bulkPrintDocuments).hasSize(2);
        assertThat(bulkPrintDocuments.getFirst().getFileName()).isEqualTo("test-file-1.pdf");
        assertThat(bulkPrintDocuments.getFirst().getBinaryFileUrl()).isEqualTo("http://test.url/1");
        assertThat(bulkPrintDocuments.getLast().getFileName()).isEqualTo("test-file-2.pdf");
        assertThat(bulkPrintDocuments.getLast().getBinaryFileUrl()).isEqualTo("http://test.url/2");

        verify(genericDocumentService).convertDocumentIfNotPdfAlready(caseDocument1, AUTH_TOKEN, CaseType.CONSENTED);
        verify(genericDocumentService).convertDocumentIfNotPdfAlready(caseDocument2, AUTH_TOKEN, CaseType.CONSENTED);
    }

    private CaseDetails caseDetails() {
        return TestSetUpUtils.caseDetailsFromResource("/fixtures/bulkprint/bulk-print.json", mapper);
    }

    private FinremCaseDetails finremCaseDetails() {
        return TestSetUpUtils.finremCaseDetailsFromResource("/fixtures/bulkprint/bulk-print.json", mapper);
    }

    private void verifyBulkPrintRequest(BulkPrintRequest actual, String expectedRecipientParty) {
        assertThat(actual.getBulkPrintDocuments()).hasSize(1);
        assertThat(actual.getLetterType()).isEqualTo(FINANCIAL_REMEDY_GENERAL_LETTER);
        assertThat(actual.getCaseId()).isEqualTo(String.valueOf(1234567890));
        assertThat(actual.getAuthorisationToken()).isEqualTo(AUTH_TOKEN);
        assertThat(actual.getRecipientParty()).isEqualTo(expectedRecipientParty);
        assertThat(actual.getRequestId()).isEqualTo("1234567890:" + expectedRecipientParty + ":12");
    }

    private void verifyBulkPrintRequest(BulkPrintRequest actual, List<BulkPrintDocument> expectedBulkPrintDocuments,
                                        long caseId, String expectedRecipientParty) {
        assertThat(actual.getBulkPrintDocuments()).containsAll(expectedBulkPrintDocuments);
        assertThat(actual.getLetterType()).isEqualTo(FINANCIAL_REMEDY_PACK_LETTER_TYPE);
        assertThat(actual.getCaseId()).isEqualTo(String.valueOf(caseId));
        assertThat(actual.getAuthorisationToken()).isEqualTo(AUTH_TOKEN);
        assertThat(actual.getRecipientParty()).isEqualTo(expectedRecipientParty);
        assertThat(actual.getRequestId()).isEqualTo(caseId + ":" + expectedRecipientParty + ":12");
    }
}
