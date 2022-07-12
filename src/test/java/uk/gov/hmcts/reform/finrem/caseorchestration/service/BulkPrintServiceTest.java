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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintRequest;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.bulkPrintDocumentList;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.finremCaseDetailsFromResource;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService.FINANCIAL_REMEDY_PACK_LETTER_TYPE;

public class BulkPrintServiceTest extends BaseServiceTest {

    @Autowired private BulkPrintService bulkPrintService;
    @Autowired private ObjectMapper mapper;
    @Autowired private DocumentHelper documentHelper;

    @MockBean private GenerateCoverSheetService coverSheetService;
    @MockBean private GenericDocumentService genericDocumentService;
    @MockBean private PaperNotificationService paperNotificationService;

    private final Document caseDocument = TestSetUpUtils.newDocument();
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
            new Document(), caseDetails());

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
        FinremCaseDetails caseDetails = finremCaseDetailsFromResource(consentedBulkPrintConsentOrderNotApprovedJson, mapper);
        List<BulkPrintDocument> bulkPrintDocuments = bulkPrintDocumentList();

        when(coverSheetService.generateApplicantCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(caseDocument);
        when(genericDocumentService.bulkPrint(bulkPrintRequestArgumentCaptor.capture())).thenReturn(letterId);

        UUID uuid = bulkPrintService.printApplicantDocuments(caseDetails, AUTH_TOKEN, bulkPrintDocuments);

        assertThat(uuid, is(letterId));

        verify(coverSheetService).generateApplicantCoverSheet(caseDetails, AUTH_TOKEN);
        verify(genericDocumentService).bulkPrint(bulkPrintRequestArgumentCaptor.capture());

        assertThat(bulkPrintRequestArgumentCaptor.getValue().getBulkPrintDocuments().containsAll(bulkPrintDocuments), is(true));
        assertThat(bulkPrintRequestArgumentCaptor.getValue().getLetterType(), is(FINANCIAL_REMEDY_PACK_LETTER_TYPE));
        assertThat(bulkPrintRequestArgumentCaptor.getValue().getCaseId(), is(String.valueOf(caseDetails.getId())));
        assertThat(caseDetails.getCaseData().getBulkPrintCoverSheetApp(), is(notNullValue()));
    }

    @Test
    public void shouldPrintRespondentDocuments() {
        final String consentedBulkPrintConsentOrderNotApprovedJson
            = "/fixtures/contested/bulk_print_consent_order_not_approved.json";
        FinremCaseDetails caseDetails = finremCaseDetailsFromResource(consentedBulkPrintConsentOrderNotApprovedJson, mapper);
        List<BulkPrintDocument> bulkPrintDocuments = bulkPrintDocumentList();

        when(coverSheetService.generateRespondentCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(caseDocument);
        when(genericDocumentService.bulkPrint(bulkPrintRequestArgumentCaptor.capture())).thenReturn(letterId);

        UUID uuid = bulkPrintService.printRespondentDocuments(caseDetails, AUTH_TOKEN, bulkPrintDocuments);

        assertThat(uuid, is(letterId));

        verify(coverSheetService).generateRespondentCoverSheet(caseDetails, AUTH_TOKEN);
        verify(genericDocumentService).bulkPrint(bulkPrintRequestArgumentCaptor.capture());

        assertThat(bulkPrintRequestArgumentCaptor.getValue().getBulkPrintDocuments().containsAll(bulkPrintDocuments), is(true));
        assertThat(bulkPrintRequestArgumentCaptor.getValue().getLetterType(), is(FINANCIAL_REMEDY_PACK_LETTER_TYPE));
        assertThat(bulkPrintRequestArgumentCaptor.getValue().getCaseId(), is(String.valueOf(caseDetails.getId())));
        assertThat(caseDetails.getCaseData().getBulkPrintCoverSheetRes(), is(notNullValue()));
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
        FinremCaseDetails caseDetails = finremCaseDetailsFromResource(json, mapper);

        assertThat(paperNotificationService.shouldPrintForApplicant(caseDetails), is(false));
    }

    @Test
    public void shouldSaveApplicantDocumentsToConfidentialCollectionWhenAddressIsConfidential() {
        final String consentedBulkPrintConsentOrderNotApprovedJson
            = "/fixtures/contested/bulk_print_consent_order_not_approved.json";
        FinremCaseDetails caseDetails = finremCaseDetailsFromResource(consentedBulkPrintConsentOrderNotApprovedJson, mapper);
        caseDetails.getCaseData().getContactDetailsWrapper().setApplicantAddressConfidential(YesOrNo.YES);
        List<BulkPrintDocument> bulkPrintDocuments = bulkPrintDocumentList();

        when(coverSheetService.generateApplicantCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(caseDocument);
        when(genericDocumentService.bulkPrint(bulkPrintRequestArgumentCaptor.capture())).thenReturn(letterId);

        UUID uuid = bulkPrintService.printApplicantDocuments(caseDetails, AUTH_TOKEN, bulkPrintDocuments);

        assertThat(uuid, is(letterId));
        assertThat(caseDetails.getCaseData().getBulkPrintCoverSheetAppConfidential(), is(notNullValue()));
        assertThat(caseDetails.getCaseData().getBulkPrintCoverSheetApp(), is(nullValue()));
    }

    @Test
    public void shouldSaveRespondentDocumentsToConfidentialCollectionWhenAddressIsConfidential() {
        final String consentedBulkPrintConsentOrderNotApprovedJson
            = "/fixtures/contested/bulk_print_consent_order_not_approved.json";
        FinremCaseDetails caseDetails = finremCaseDetailsFromResource(consentedBulkPrintConsentOrderNotApprovedJson, mapper);
        caseDetails.getCaseData().getContactDetailsWrapper().setRespondentAddressConfidential(YesOrNo.YES);
        List<BulkPrintDocument> bulkPrintDocuments = bulkPrintDocumentList();

        when(coverSheetService.generateRespondentCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(caseDocument);
        when(genericDocumentService.bulkPrint(bulkPrintRequestArgumentCaptor.capture())).thenReturn(letterId);

        UUID uuid = bulkPrintService.printRespondentDocuments(caseDetails, AUTH_TOKEN, bulkPrintDocuments);

        assertThat(uuid, is(letterId));
        assertThat(caseDetails.getCaseData().getBulkPrintCoverSheetResConfidential(), is(notNullValue()));
        assertThat(caseDetails.getCaseData().getBulkPrintCoverSheetRes(), is(nullValue()));
    }

    private FinremCaseDetails caseDetails() {
        return finremCaseDetailsFromResource("/fixtures/bulkprint/bulk-print.json", mapper);
    }
}
