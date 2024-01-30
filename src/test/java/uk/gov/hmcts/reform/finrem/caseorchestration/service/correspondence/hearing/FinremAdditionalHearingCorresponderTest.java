package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class FinremAdditionalHearingCorresponderTest extends FinremHearingCorrespondenceBaseTest {

    private ObjectMapper objectMapper;

    @Before
    public void setUpTest() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        applicantAndRespondentMultiLetterCorresponder =
            new FinremAdditionalHearingCorresponder(bulkPrintService, notificationService, documentHelper);
        caseDetails = TestSetUpUtils.finremCaseDetailsFromResource("/fixtures/bulkprint/bulk-print-additional-hearing.json", objectMapper);

    }

    @Test
    public void shouldGetDocumentsToPrint() {
        List<CaseDocument> documentsToPrint = applicantAndRespondentMultiLetterCorresponder.getCaseDocuments(caseDetails);
        assertEquals(2, documentsToPrint.size());
    }

    @Test
    public void shouldGetDocumentsToPrintIfgetAdditionalHearingDocumentDateIsNull() {
        AdditionalHearingDocumentCollection document1 = AdditionalHearingDocumentCollection.builder()
            .value(AdditionalHearingDocument.builder()
                .document(CaseDocument.builder().build())
                .additionalHearingDocumentDate(null).build())
            .build();
        AdditionalHearingDocumentCollection document2 = AdditionalHearingDocumentCollection.builder()
            .value(AdditionalHearingDocument.builder()
                .document(CaseDocument.builder().build())
                .additionalHearingDocumentDate(null).build())
            .build();
        AdditionalHearingDocumentCollection document3 = AdditionalHearingDocumentCollection.builder()
            .value(AdditionalHearingDocument.builder()
                .document(CaseDocument.builder().build())
                .additionalHearingDocumentDate(LocalDateTime.now()).build())
            .build();

        caseDetails.getData().setAdditionalHearingDocuments(List.of(document1, document2, document3));
        List<CaseDocument> documentsToPrint = applicantAndRespondentMultiLetterCorresponder.getCaseDocuments(caseDetails);
        assertEquals(2, documentsToPrint.size());
    }

}
