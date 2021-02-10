package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintRequest;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.bulkPrintDocumentList;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_CONFIDENTIAL_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_COVER_SHEET_APP;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_COVER_SHEET_APP_CONFIDENTIAL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_COVER_SHEET_RES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_COVER_SHEET_RES_CONFIDENTIAL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_CONFIDENTIAL_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService.FINANCIAL_REMEDY_PACK_LETTER_TYPE;

public class BulkPrintServiceTest extends BaseServiceTest {

    @Autowired private BulkPrintService bulkPrintService;
    @Autowired private ObjectMapper mapper;
    @Autowired private DocumentHelper documentHelper;

    @MockBean private GenerateCoverSheetService coverSheetService;
    @MockBean private GenericDocumentService genericDocumentService;
    @MockBean private PaperNotificationService paperNotificationService;

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
            new CaseDocument(), caseDetails());

        assertThat(bulkPrintLetterId, is(letterId));
    }

    @Test
    public void whenPrintingDocument_thenDocumentIsSentToPrinting() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource("/fixtures/general-letter.json", mapper);
        UUID bulkPrintLetterId = bulkPrintService.sendDocumentForPrint(caseDocument(), caseDetails);

        assertThat(bulkPrintLetterId, is(letterId));
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

        assertThat(uuid, is(letterId));

        verify(coverSheetService).generateApplicantCoverSheet(caseDetails, AUTH_TOKEN);
        verify(genericDocumentService).bulkPrint(bulkPrintRequestArgumentCaptor.capture());

        assertThat(bulkPrintRequestArgumentCaptor.getValue().getBulkPrintDocuments().containsAll(bulkPrintDocuments), is(true));
        assertThat(bulkPrintRequestArgumentCaptor.getValue().getLetterType(), is(FINANCIAL_REMEDY_PACK_LETTER_TYPE));
        assertThat(bulkPrintRequestArgumentCaptor.getValue().getCaseId(), is(caseDetails.getId().toString()));
        assertThat(caseDetails.getData().containsKey(BULK_PRINT_COVER_SHEET_APP), is(true));
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

        assertThat(uuid, is(letterId));

        verify(coverSheetService).generateRespondentCoverSheet(caseDetails, AUTH_TOKEN);
        verify(genericDocumentService).bulkPrint(bulkPrintRequestArgumentCaptor.capture());

        assertThat(bulkPrintRequestArgumentCaptor.getValue().getBulkPrintDocuments().containsAll(bulkPrintDocuments), is(true));
        assertThat(bulkPrintRequestArgumentCaptor.getValue().getLetterType(), is(FINANCIAL_REMEDY_PACK_LETTER_TYPE));
        assertThat(bulkPrintRequestArgumentCaptor.getValue().getCaseId(), is(caseDetails.getId().toString()));
        assertThat(caseDetails.getData().containsKey(BULK_PRINT_COVER_SHEET_RES), is(true));
    }

    @Test
    public void shouldConvertCaseDocumentToBulkPrintDocument() {
        BulkPrintDocument bulkPrintDoc = documentHelper.getBulkPrintDocumentFromCaseDocument(caseDocument());
        assertThat(bulkPrintDoc.getBinaryFileUrl(), is(BINARY_URL));
    }

    @Test
    public void shouldNotPrintForApplicantIfRepresentedAgreedToEmailAndNotPaperCase() {
        final String json
            = "/fixtures/refusal-order-contested.json";
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(json, mapper);

        assertThat(paperNotificationService.shouldPrintForApplicant(caseDetails), is(false));
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

        assertThat(uuid, is(letterId));
        assertThat(caseDetails.getData().containsKey(BULK_PRINT_COVER_SHEET_APP_CONFIDENTIAL), is(true));
        assertThat(caseDetails.getData().containsKey(BULK_PRINT_COVER_SHEET_APP), is(false));
    }

    @Test
    public void shouldSaveRespondentDocumentsToConfidentialCollectionWhenAddressIsConfidential() {
        final String consentedBulkPrintConsentOrderNotApprovedJson
            = "/fixtures/contested/bulk_print_consent_order_not_approved.json";
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(consentedBulkPrintConsentOrderNotApprovedJson, mapper);
        caseDetails.getData().put(RESPONDENT_CONFIDENTIAL_ADDRESS, "Yes");
        List<BulkPrintDocument> bulkPrintDocuments = bulkPrintDocumentList();

        when(coverSheetService.generateRespondentCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(caseDocument);
        when(genericDocumentService.bulkPrint(bulkPrintRequestArgumentCaptor.capture())).thenReturn(letterId);

        UUID uuid = bulkPrintService.printRespondentDocuments(caseDetails, AUTH_TOKEN, bulkPrintDocuments);

        assertThat(uuid, is(letterId));
        assertThat(caseDetails.getData().containsKey(BULK_PRINT_COVER_SHEET_RES_CONFIDENTIAL), is(true));
        assertThat(caseDetails.getData().containsKey(BULK_PRINT_COVER_SHEET_RES), is(false));
    }

    private CaseDetails caseDetails() {
        return TestSetUpUtils.caseDetailsFromResource("/fixtures/bulkprint/bulk-print.json", mapper);
    }
}
