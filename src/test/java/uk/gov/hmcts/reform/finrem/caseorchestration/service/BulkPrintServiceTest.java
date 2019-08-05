package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintRequest;

import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BulkPrintServiceTest {

    @Mock
    private DocumentClient documentClientMock;

    private DocumentConfiguration config;

    private ObjectMapper mapper = new ObjectMapper();

    private BulkPrintService service;

    private UUID letterId;

    private ArgumentCaptor<BulkPrintRequest> bulkPrintRequestArgumentCaptor;

    @Before
    public void setUp() {
        letterId = UUID.randomUUID();
        bulkPrintRequestArgumentCaptor = ArgumentCaptor.forClass(BulkPrintRequest.class);
        config = new DocumentConfiguration();
        config.setApprovedConsentOrderTemplate("test_template");
        config.setApprovedConsentOrderFileName("test_file");
        documentClientMock = mock(DocumentClient.class);
        service = new BulkPrintService(documentClientMock, config, mapper);
    }

    @Test
    public void shouldSendForBulkPrintForApproved() throws Exception {
        Map<String, Object> caseData = ImmutableMap.of();

        when(documentClientMock.bulkPrint(bulkPrintRequestArgumentCaptor.capture()))
            .thenReturn(letterId);

        UUID bulkPrintLetterId = service.sendForBulkPrint(
            new CaseDocument(), caseDetails());

        assertThat(letterId, is(bulkPrintLetterId));
        assertThat(bulkPrintRequestArgumentCaptor.getValue().getBulkPrintDocuments().size(), is(5));
    }

    @Test
    public void shouldSendForBulkPrintForNotApproved() throws Exception {
        Map<String, Object> caseData = ImmutableMap.of();

        when(documentClientMock.bulkPrint(bulkPrintRequestArgumentCaptor.capture()))
            .thenReturn(letterId);

        UUID bulkPrintLetterId = service.sendForBulkPrint(
            new CaseDocument(), caseDetailsForNonApproved());

        assertThat(letterId, is(bulkPrintLetterId));
        assertThat(bulkPrintRequestArgumentCaptor.getValue().getBulkPrintDocuments().size(), is(2));
    }

    private CaseDetails caseDetails() throws Exception {
        try (InputStream resourceAsStream =
                 getClass().getResourceAsStream("/fixtures/bulk-print.json")) {
            return mapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
        }
    }

    private CaseDetails caseDetailsForNonApproved() throws Exception {
        try (InputStream resourceAsStream =
                 getClass().getResourceAsStream("/fixtures/bulk-print-not-approved.json")) {
            return mapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
        }
    }

    private CaseDetails caseDetailsWithoutData() throws Exception {
        try (InputStream resourceAsStream =
                 getClass().getResourceAsStream("/fixtures/hwf.json")) {
            return mapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
        }
    }


}
