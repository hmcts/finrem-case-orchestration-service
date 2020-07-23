package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintRequest;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.document;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.matchDocumentGenerationRequestTemplateAndFilename;

@ActiveProfiles("test-mock-document-client")
public class BulkPrintServiceTest extends BaseServiceTest {

    @Autowired private DocumentClient documentClient;
    @Autowired private BulkPrintService bulkPrintService;
    @Autowired private ObjectMapper mapper;
    @MockBean private ConsentOrderNotApprovedDocumentService consentOrderNotApprovedDocumentService;
    @MockBean private GeneralOrderService generalOrderService;
    @MockBean private FeatureToggleService featureToggleService;

    @Value("${document.bulkPrintTemplate}") private String documentBulkPrintTemplate;
    @Value("${document.bulkPrintFileName}") private String documentBulkPrintFileName;

    @Value("${document.approvedConsentOrderNotificationTemplate}") private String documentApprovedConsentOrderNotificationTemplate;
    @Value("${document.approvedConsentOrderNotificationFileName}") private String documentApprovedConsentOrderNotificationFileName;

    private UUID letterId;
    private ArgumentCaptor<BulkPrintRequest> bulkPrintRequestArgumentCaptor;

    @Before
    public void setUp() {
        letterId = UUID.randomUUID();
        bulkPrintRequestArgumentCaptor = ArgumentCaptor.forClass(BulkPrintRequest.class);
    }

    @Test
    public void shouldSendAssignedToJudgeNotificationLetterForBulkPrint() {
        when(documentClient.bulkPrint(bulkPrintRequestArgumentCaptor.capture())).thenReturn(letterId);

        UUID bulkPrintLetterId = bulkPrintService.sendNotificationLetterForBulkPrint(
            new CaseDocument(), caseDetails());

        assertThat(bulkPrintLetterId, is(letterId));
        assertThat(bulkPrintRequestArgumentCaptor.getValue().getBulkPrintDocuments(), hasSize(1));
    }

    @Test
    public void shouldSendOrderDocumentsForBulkPrintForApproved() {
        when(documentClient.generatePdf(matchDocumentGenerationRequestTemplateAndFilename(
            documentBulkPrintTemplate, documentBulkPrintFileName), anyString())).thenReturn(document());
        when(documentClient.generatePdf(matchDocumentGenerationRequestTemplateAndFilename(
            documentApprovedConsentOrderNotificationTemplate, documentApprovedConsentOrderNotificationFileName), anyString()))
            .thenReturn(document());
        when(documentClient.bulkPrint(bulkPrintRequestArgumentCaptor.capture())).thenReturn(letterId);

        UUID bulkPrintLetterId = bulkPrintService.printApplicantConsentOrderApprovedDocuments(caseDetails(), AUTH_TOKEN);

        assertThat(bulkPrintLetterId, is(letterId));
        assertThat(bulkPrintRequestArgumentCaptor.getValue().getBulkPrintDocuments(), hasSize(6));
    }

    @Test
    public void shouldSendForBulkPrintForNotApproved() {
        when(documentClient.bulkPrint(bulkPrintRequestArgumentCaptor.capture())).thenReturn(letterId);

        UUID bulkPrintLetterId = bulkPrintService.sendOrderForBulkPrintRespondent(
            new CaseDocument(), caseDetailsForNonApproved());

        assertThat(bulkPrintLetterId, is(letterId));
        assertThat(bulkPrintRequestArgumentCaptor.getValue().getBulkPrintDocuments(), hasSize(2));
    }

    @Test
    public void shouldConvertCollectionDocument() {
        List<BulkPrintDocument> bulkPrintDocuments = bulkPrintService.approvedOrderCollection(caseDetails().getData());

        assertThat(bulkPrintDocuments, hasSize(4));
    }

    @Test
    public void givenOrderNotApprovedFirst_WhenOrderIsApproved_ThenConsentOrderApprovedDocumentsAreSent() {
        when(documentClient.bulkPrint(bulkPrintRequestArgumentCaptor.capture())).thenReturn(letterId);
        bulkPrintService.sendOrderForBulkPrintRespondent(new CaseDocument(), caseDetails());

        List<BulkPrintDocument> printedDocuments = bulkPrintRequestArgumentCaptor.getValue().getBulkPrintDocuments();
        assertThat(printedDocuments.get(1).getBinaryFileUrl(), is("http://localhost:4506/documents/d4c4c071-3f14-4cff-a9c4-243f787a9fb8/binary"));
    }

    @Test
    public void whenPrintingGeneralLetter_thenLatestGeneralLetterIsSentToPrinting() {
        when(documentClient.bulkPrint(bulkPrintRequestArgumentCaptor.capture())).thenReturn(letterId);

        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource("/fixtures/general-letter.json", mapper);
        bulkPrintService.printLatestGeneralLetter(caseDetails);

        List<BulkPrintDocument> printedDocuments = bulkPrintRequestArgumentCaptor.getValue().getBulkPrintDocuments();
        assertThat(printedDocuments.get(0).getBinaryFileUrl(), is("http://dm-store/lhjbyuivu87y989hijbb/binary"));
    }

    @Test
    public void shouldAddGeneralOrdersIfToggleOn() {
        final String consentedBulkPrintConsentOrderNotApprovedJson
            = "/fixtures/contested/bulk_print_consent_order_not_approved.json";
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(consentedBulkPrintConsentOrderNotApprovedJson, mapper);
        List<BulkPrintDocument> bulkPrintDocuments = Collections.emptyList();

        when(featureToggleService.isPrintGeneralOrderEnabled()).thenReturn(true);
        when(consentOrderNotApprovedDocumentService.prepareApplicantLetterPack(any(CaseDetails.class), anyString())).thenReturn(bulkPrintDocuments);
        when(documentClient.bulkPrint(bulkPrintRequestArgumentCaptor.capture())).thenReturn(letterId);

        UUID uuid = bulkPrintService.printApplicantConsentOrderNotApprovedDocuments(caseDetails, AUTH_TOKEN);
        assertThat(uuid, is(letterId));
        verify(generalOrderService).getGeneralOrdersForPrintingConsented(caseDetails.getData());
    }

    @Test
    public void shouldNotAddGeneralOrdersIfToggleOff() {
        final String consentedBulkPrintConsentOrderNotApprovedJson
            = "/fixtures/contested/bulk_print_consent_order_not_approved.json";
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(consentedBulkPrintConsentOrderNotApprovedJson, mapper);
        List<BulkPrintDocument> bulkPrintDocuments = Collections.emptyList();

        when(featureToggleService.isPrintGeneralOrderEnabled()).thenReturn(false);
        when(consentOrderNotApprovedDocumentService.prepareApplicantLetterPack(caseDetails, AUTH_TOKEN)).thenReturn(bulkPrintDocuments);
        when(documentClient.bulkPrint(bulkPrintRequestArgumentCaptor.capture())).thenReturn(letterId);

        UUID uuid = bulkPrintService.printApplicantConsentOrderNotApprovedDocuments(caseDetails, AUTH_TOKEN);

        assertThat(uuid, is(letterId));

        verify(generalOrderService, times(0)).getGeneralOrdersForPrintingConsented(caseDetails.getData());
    }

    private CaseDetails caseDetails() {
        return TestSetUpUtils.caseDetailsFromResource("/fixtures/bulkprint/bulk-print.json", mapper);
    }

    private CaseDetails caseDetailsForNonApproved() {
        return TestSetUpUtils.caseDetailsFromResource("/fixtures/bulkprint/bulk-print-not-approved.json", mapper);
    }
}
