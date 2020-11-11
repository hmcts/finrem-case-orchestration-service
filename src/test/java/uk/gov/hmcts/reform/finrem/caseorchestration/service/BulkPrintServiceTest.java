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
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintRequest;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.bulkPrintDocumentList;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_COVER_SHEET_APP;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_COVER_SHEET_RES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService.FINANCIAL_REMEDY_PACK_LETTER_TYPE;

@ActiveProfiles("test-mock-document-client")
public class BulkPrintServiceTest extends BaseServiceTest {

    @Autowired
    private DocumentClient documentClient;
    @Autowired
    private BulkPrintService bulkPrintService;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private DocumentHelper documentHelper;

    @MockBean
    private ConsentOrderNotApprovedDocumentService consentOrderNotApprovedDocumentService;
    @MockBean
    private GeneralOrderService generalOrderService;
    @MockBean
    private GenerateCoverSheetService coverSheetService;
    @MockBean
    private GenericDocumentService genericDocumentService;
    @MockBean
    private ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;

    private UUID letterId;
    private ArgumentCaptor<BulkPrintRequest> bulkPrintRequestArgumentCaptor;
    private CaseDocument caseDocument = TestSetUpUtils.caseDocument();
    private BulkPrintDocument bulkPrintDocument;

    @Before
    public void setUp() {
        letterId = UUID.randomUUID();
        bulkPrintRequestArgumentCaptor = ArgumentCaptor.forClass(BulkPrintRequest.class);
        bulkPrintDocument = documentHelper.getCaseDocumentAsBulkPrintDocument(caseDocument);
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

        verify(coverSheetService, times(1)).generateApplicantCoverSheet(caseDetails, AUTH_TOKEN);
        verify(genericDocumentService, times(1)).bulkPrint(bulkPrintRequestArgumentCaptor.capture());

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

        verify(coverSheetService, times(1)).generateRespondentCoverSheet(caseDetails, AUTH_TOKEN);
        verify(genericDocumentService, times(1)).bulkPrint(bulkPrintRequestArgumentCaptor.capture());

        assertThat(bulkPrintRequestArgumentCaptor.getValue().getBulkPrintDocuments().containsAll(bulkPrintDocuments), is(true));
        assertThat(bulkPrintRequestArgumentCaptor.getValue().getLetterType(), is(FINANCIAL_REMEDY_PACK_LETTER_TYPE));
        assertThat(bulkPrintRequestArgumentCaptor.getValue().getCaseId(), is(caseDetails.getId().toString()));
        assertThat(caseDetails.getData().containsKey(BULK_PRINT_COVER_SHEET_RES), is(true));
    }

    @Test
    public void shouldConvertCaseDocumentToBulkPrintDocument() {
        BulkPrintDocument bulkPrintDoc = bulkPrintService.getBulkPrintDocumentFromCaseDocument(caseDocument());
        assertThat(bulkPrintDoc.getBinaryFileUrl(), is(BINARY_URL));
    }

    @Test
    public void shouldNotPrintForApplicantIfRepresentedAgreedToEmailAndNotPaperCase() {
        final String json
            = "/fixtures/refusal-order-contested.json";
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(json, mapper);

        assertThat(bulkPrintService.shouldPrintForApplicant(caseDetails), is(false));
    }

    @Test
    public void shouldPrintForApplicantIfNotRepresented() {
        final String json
            = "/fixtures/refusal-order-contested.json";
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(json, mapper);
        caseDetails.getData().put("applicantRepresented", "No");
        caseDetails.getData().remove("applicantSolicitorConsentForEmails");
        caseDetails.getData().put("paperApplication", "No");

        assertThat(bulkPrintService.shouldPrintForApplicant(caseDetails), is(true));
    }

    @Test
    public void shouldPrintForApplicantIfRepresentedButNotAgreedToEmail() {
        final String json
            = "/fixtures/refusal-order-contested.json";
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(json, mapper);
        caseDetails.getData().put("applicantRepresented", "Yes");
        caseDetails.getData().put("applicantSolicitorConsentForEmails", "No");
        caseDetails.getData().put("paperApplication", "No");

        assertThat(bulkPrintService.shouldPrintForApplicant(caseDetails), is(true));
    }

    @Test
    public void shouldPrintForApplicantIfPaperCase() {
        final String json
            = "/fixtures/refusal-order-contested.json";
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(json, mapper);
        caseDetails.getData().put("paperApplication", "YES");

        assertThat(bulkPrintService.shouldPrintForApplicant(caseDetails), is(true));
    }

    private CaseDetails caseDetails() {
        return TestSetUpUtils.caseDetailsFromResource("/fixtures/bulkprint/bulk-print.json", mapper);
    }
}
