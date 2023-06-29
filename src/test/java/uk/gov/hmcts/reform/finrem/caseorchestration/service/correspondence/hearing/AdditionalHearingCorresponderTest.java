package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDocumentData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AdditionalHearingCorresponderTest extends HearingCorrespondenceBaseTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUpTest() {
        applicantAndRespondentMultiLetterCorresponder = new AdditionalHearingCorresponder(bulkPrintService,
            notificationService,
            finremCaseDetailsMapper,
            documentHelper);
        caseDetails = TestSetUpUtils.caseDetailsFromResource("/fixtures/bulkprint/bulk-print-additional-hearing.json", objectMapper);

        when(documentHelper.convertToAdditionalHearingDocumentData(any())).thenReturn(getAdditionalHearingDocumentData());
        when(documentHelper.getBulkPrintDocumentFromCaseDocument(any())).thenReturn(getBulkPrintDocument());
        when(documentHelper.convertToCaseDocument(any())).thenReturn(getCaseDocument());

    }

    @Test
    public void shouldGetDocumentsToPrint() {
        List<BulkPrintDocument> documentsToPrint = applicantAndRespondentMultiLetterCorresponder.getDocumentsToPrint(caseDetails);
        assertEquals(2, documentsToPrint.size());
    }

    private CaseDocument getCaseDocument() {
        return CaseDocument.builder().documentFilename("test").documentBinaryUrl("test").documentUrl("test").build();
    }


    private List<AdditionalHearingDocumentData> getAdditionalHearingDocumentData() {
        return List.of(AdditionalHearingDocumentData.builder().additionalHearingDocument(AdditionalHearingDocument.builder().build()).build(),
            AdditionalHearingDocumentData.builder().additionalHearingDocument(AdditionalHearingDocument.builder().build()).build());
    }
}