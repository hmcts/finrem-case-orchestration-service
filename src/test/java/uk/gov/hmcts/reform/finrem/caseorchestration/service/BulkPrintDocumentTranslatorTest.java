package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;

import java.io.InputStream;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class BulkPrintDocumentTranslatorTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void shouldConvertDocument() throws Exception {
        List<BulkPrintDocument> bulkPrintDocuments = BulkPrintDocumentTranslator
            .uploadOrder(caseDetails().getData());

        assertThat(bulkPrintDocuments.size(), is(1));
    }

    @Test
    public void shouldConvertCollectionDocument() throws Exception {
        List<BulkPrintDocument> bulkPrintDocuments = BulkPrintDocumentTranslator
                .approvedOrderCollection(caseDetails().getData());

        assertThat(bulkPrintDocuments.size(), is(4));
    }

    private CaseDetails caseDetails() throws Exception {
        try (InputStream resourceAsStream =
                 getClass().getResourceAsStream("/fixtures/bulk-print.json")) {
            return mapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
        }
    }
}
