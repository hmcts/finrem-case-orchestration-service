package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.finremCaseDetailsFromResource;

@ExtendWith(MockitoExtension.class)
class FinremApprovedOrderNoticeOfHearingCorresponderTest extends FinremHearingCorrespondenceBaseTest {

    private static final String LATEST_DRAFT_ORDER_DOCUMENT_BIN_URL = "http://dm-store/1frea-ldo-doc/binary";
    private static final String GENERAL_APPLICATION_DIRECTIONS_DOCUMENT_BIN_URL = "http://dm-store/1f3a-gads-doc/binary";

    @BeforeEach
    public void setUp() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        applicantAndRespondentMultiLetterCorresponder =
            new FinremApprovedOrderNoticeOfHearingCorresponder(bulkPrintService, notificationService, documentHelper);
        caseDetails = finremCaseDetailsFromResource("/fixtures/general-application-directions.json", objectMapper);
        caseDetails.getData().setHearingNoticeDocumentPack(buildHearingNoticePack());

    }

    @Test
    void shouldGetDocumentsToPrint() {
        List<CaseDocument> documentsToPrint = applicantAndRespondentMultiLetterCorresponder.getCaseDocuments(caseDetails);
        assertEquals(2, documentsToPrint.size());
    }

    private List<DocumentCollectionItem> buildHearingNoticePack() {
        return List.of(DocumentCollectionItem.builder().value(CaseDocument.builder()
                .documentBinaryUrl(GENERAL_APPLICATION_DIRECTIONS_DOCUMENT_BIN_URL)
                .build()).build(),
            DocumentCollectionItem.builder().value(CaseDocument.builder()
                .documentBinaryUrl(LATEST_DRAFT_ORDER_DOCUMENT_BIN_URL)
                .build()).build());
    }

}
