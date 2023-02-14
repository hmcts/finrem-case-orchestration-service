package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FinremAdditionalHearingCorresponderTest extends FinremHearingCorrespondenceBaseTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUpTest() {
        applicantAndRespondentMultiLetterCorresponder =
            new FinremAdditionalHearingCorresponder(bulkPrintService, notificationService, documentHelper);
        caseDetails = TestSetUpUtils.finremCaseDetailsFromResource("/fixtures/bulkprint/bulk-print-additional-hearing.json", objectMapper);

        when(documentHelper.getBulkPrintDocumentFromCaseDocument(any())).thenReturn(getBulkPrintDocument());

    }

    @Test
    public void shouldGetDocumentsToPrint() {
        List<BulkPrintDocument> documentsToPrint = applicantAndRespondentMultiLetterCorresponder.getDocumentsToPrint(caseDetails);
        assertEquals(2, documentsToPrint.size());
    }

}