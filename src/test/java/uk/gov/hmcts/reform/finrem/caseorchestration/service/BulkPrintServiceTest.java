package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintRequest;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@ActiveProfiles("test-mock-document-client")
public class BulkPrintServiceTest extends BaseServiceTest {

    @Autowired
    private DocumentClient documentClientMock;

    @Autowired
    private BulkPrintService bulkPrintService;

    @Value("${feature.approved-consent-order-notification-letter}")
    private boolean featureApprovedConsentOrderNotificationLetter;

    private ObjectMapper mapper = new ObjectMapper();

    private UUID letterId;

    private ArgumentCaptor<BulkPrintRequest> bulkPrintRequestArgumentCaptor;

    @Before
    public void setUp() {
        letterId = UUID.randomUUID();
        bulkPrintRequestArgumentCaptor = ArgumentCaptor.forClass(BulkPrintRequest.class);
        DocumentConfiguration config = new DocumentConfiguration();
        config.setApprovedConsentOrderTemplate("test_template");
        config.setApprovedConsentOrderFileName("test_file");
    }

    @Test
    public void shouldSendNotificationLetterForBulkPrint() throws Exception {
        when(documentClientMock.bulkPrint(bulkPrintRequestArgumentCaptor.capture())).thenReturn(letterId);

        UUID notificationLetterId = bulkPrintService.sendNotificationLetterForBulkPrint(
            new CaseDocument(), caseDetailsForNonApproved());

        assertThat(letterId, is(notificationLetterId));
        assertThat(bulkPrintRequestArgumentCaptor.getValue().getBulkPrintDocuments().size(), is(1));
    }

    @Test
    public void shouldSendOrdersForBulkPrintForApproved() throws Exception {
        when(documentClientMock.bulkPrint(bulkPrintRequestArgumentCaptor.capture())).thenReturn(letterId);

        UUID bulkPrintLetterId = bulkPrintService.sendOrdersForBulkPrint(
            new CaseDocument(), caseDetails());

        assertThat(bulkPrintLetterId, is(letterId));
        assertThat(bulkPrintRequestArgumentCaptor.getValue().getBulkPrintDocuments().size(),
            is(featureApprovedConsentOrderNotificationLetter ? 6 : 5));
    }

    @Test
    public void shouldSendOrdersForBulkPrintForNotApproved() throws Exception {
        when(documentClientMock.bulkPrint(bulkPrintRequestArgumentCaptor.capture())).thenReturn(letterId);

        UUID bulkPrintLetterId = bulkPrintService.sendOrdersForBulkPrint(
            new CaseDocument(), caseDetailsForNonApproved());

        assertThat(bulkPrintLetterId, is(letterId));
        assertThat(bulkPrintRequestArgumentCaptor.getValue().getBulkPrintDocuments().size(), is(2));
    }

    @Test
    public void shouldConvertDocument() throws Exception {
        List<BulkPrintDocument> bulkPrintDocuments = bulkPrintService.uploadOrder(caseDetails().getData());

        assertThat(bulkPrintDocuments.size(), is(1));
    }

    @Test
    public void shouldConvertCollectionDocument() throws Exception {
        List<BulkPrintDocument> bulkPrintDocuments = bulkPrintService.approvedOrderCollection(caseDetails().getData());

        assertThat(bulkPrintDocuments.size(), is(featureApprovedConsentOrderNotificationLetter ? 5 : 4));
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
