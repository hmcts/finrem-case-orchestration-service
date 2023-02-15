package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.finremCaseDetailsFromResource;

@RunWith(MockitoJUnitRunner.class)
public class FinremApprovedOrderNoticeOfHearingCorresponderTest extends FinremHearingCorrespondenceBaseTest {

    private static final String LATEST_DRAFT_ORDER_DOCUMENT_BIN_URL = "http://dm-store/1frea-ldo-doc/binary";
    private static final String GENERAL_APPLICATION_DIRECTIONS_DOCUMENT_BIN_URL = "http://dm-store/1f3a-gads-doc/binary";

    private ObjectMapper objectMapper;

    @Before
    public void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        applicantAndRespondentMultiLetterCorresponder =
            new FinremApprovedOrderNoticeOfHearingCorresponder(bulkPrintService, notificationService,
                objectMapper, documentHelper);
        caseDetails = finremCaseDetailsFromResource("/fixtures/general-application-directions.json", objectMapper);
        caseDetails.getData().setHearingNoticeDocumentPack(buildHearingNoticePack());

    }

    @Test
    public void shouldGetDocumentsToPrint() {
        when(documentHelper.getCaseDocumentsAsBulkPrintDocuments(anyList())).thenReturn(List.of(getBulkPrintDocument(), getBulkPrintDocument()));
        List<BulkPrintDocument> documentsToPrint = applicantAndRespondentMultiLetterCorresponder.getDocumentsToPrint(caseDetails);
        assertEquals(2, documentsToPrint.size());
    }

    private List<DocumentCollection> buildHearingNoticePack() {
        return List.of(DocumentCollection.builder().value(CaseDocument.builder()
                .documentBinaryUrl(GENERAL_APPLICATION_DIRECTIONS_DOCUMENT_BIN_URL)
                .build()).build(),
            DocumentCollection.builder().value(CaseDocument.builder()
                .documentBinaryUrl(LATEST_DRAFT_ORDER_DOCUMENT_BIN_URL)
                .build()).build());
    }

}