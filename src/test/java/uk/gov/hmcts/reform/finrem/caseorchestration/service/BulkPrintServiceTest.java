package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintRequest;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.document;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.matchDocumentGenerationRequestTemplateAndFilename;

@ActiveProfiles("test-mock-document-client")
@SpringBootTest(properties = {"feature.toggle.approved_consent_order_notification_letter=true"})
public class BulkPrintServiceTest extends BaseServiceTest {

    @Autowired private DocumentClient documentClient;
    @Autowired private BulkPrintService bulkPrintService;
    @Autowired private ObjectMapper mapper;

    @Value("${document.bulkPrintTemplate}") private String documentBulkPrintTemplate;
    @Value("${document.bulkPrintFileName}") private String documentBulkPrintFileName;

    @Value("${document.approvedConsentOrderNotificationTemplate}") private String documentApprovedConsentOrderNotificationTemplate;
    @Value("${document.approvedConsentOrderNotificationFileName}") private String documentApprovedConsentOrderNotificationFileName;

    @Autowired
    private DocumentClient documentClientMock;

    private UUID letterId;
    private ArgumentCaptor<BulkPrintRequest> bulkPrintRequestArgumentCaptor;

    @Before
    public void setUp() {
        letterId = UUID.randomUUID();
        bulkPrintRequestArgumentCaptor = ArgumentCaptor.forClass(BulkPrintRequest.class);
    }

    @Test
    public void shouldSendAssignedToJudgeNotificationLetterForBulkPrint() throws Exception {
        when(documentClient.bulkPrint(bulkPrintRequestArgumentCaptor.capture())).thenReturn(letterId);

        UUID bulkPrintLetterId = bulkPrintService.sendNotificationLetterForBulkPrint(
            new CaseDocument(), caseDetails());

        assertThat(bulkPrintLetterId, is(letterId));
        assertThat(bulkPrintRequestArgumentCaptor.getValue().getBulkPrintDocuments(), hasSize(1));
    }

    @Test
    public void shouldSendOrderDocumentsForBulkPrintForApproved() throws Exception {
        when(documentClientMock.generatePdf(matchDocumentGenerationRequestTemplateAndFilename(
            documentBulkPrintTemplate, documentBulkPrintFileName), anyString())).thenReturn(document());
        when(documentClientMock.generatePdf(matchDocumentGenerationRequestTemplateAndFilename(
            documentApprovedConsentOrderNotificationTemplate, documentApprovedConsentOrderNotificationFileName), anyString()))
            .thenReturn(document());
        when(documentClient.bulkPrint(bulkPrintRequestArgumentCaptor.capture())).thenReturn(letterId);

        UUID bulkPrintLetterId = bulkPrintService.printApplicantConsentOrderApprovedDocuments(caseDetails(), AUTH_TOKEN);

        assertThat(bulkPrintLetterId, is(letterId));
        assertThat(bulkPrintRequestArgumentCaptor.getValue().getBulkPrintDocuments(), hasSize(6));
    }

    @Test
    public void shouldSendForBulkPrintForNotApproved() throws Exception {
        when(documentClient.bulkPrint(bulkPrintRequestArgumentCaptor.capture())).thenReturn(letterId);

        UUID bulkPrintLetterId = bulkPrintService.sendOrderForBulkPrintRespondent(
            new CaseDocument(), caseDetailsForNonApproved());

        assertThat(bulkPrintLetterId, is(letterId));
        assertThat(bulkPrintRequestArgumentCaptor.getValue().getBulkPrintDocuments(), hasSize(2));
    }

    @Test
    public void shouldConvertCollectionDocument() throws Exception {
        List<BulkPrintDocument> bulkPrintDocuments = bulkPrintService.approvedOrderCollection(caseDetails().getData());

        assertThat(bulkPrintDocuments, hasSize(4));
    }

    private CaseDetails caseDetails() throws Exception {
        try (InputStream resourceAsStream =
                 getClass().getResourceAsStream("/fixtures/bulkprint/bulk-print.json")) {
            return mapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
        }
    }

    private CaseDetails caseDetailsForNonApproved() throws Exception {
        try (InputStream resourceAsStream =
                 getClass().getResourceAsStream("/fixtures/bulkprint/bulk-print-not-approved.json")) {
            return mapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
        }
    }
}
