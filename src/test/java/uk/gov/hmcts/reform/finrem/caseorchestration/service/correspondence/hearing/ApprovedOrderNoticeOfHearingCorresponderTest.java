package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Element;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDetailsFromResource;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_NOTICE_DOCUMENT_PACK;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Element.element;

@RunWith(MockitoJUnitRunner.class)
public class ApprovedOrderNoticeOfHearingCorresponderTest extends HearingCorrespondenceBaseTest {

    private static final String LATEST_DRAFT_ORDER_DOCUMENT_BIN_URL = "http://dm-store/1frea-ldo-doc/binary";
    private static final String GENERAL_APPLICATION_DIRECTIONS_DOCUMENT_BIN_URL = "http://dm-store/1f3a-gads-doc/binary";


    private ObjectMapper objectMapper = new ObjectMapper();


    @Before
    public void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        applicantAndRespondentMultiLetterCorresponder =
            new ApprovedOrderNoticeOfHearingCorresponder(bulkPrintService, notificationService, notificationRequestMapper,
                objectMapper, documentHelper);
        caseDetails = caseDetailsFromResource("/fixtures/general-application-directions.json", objectMapper);
        caseDetails.getData().put(HEARING_NOTICE_DOCUMENT_PACK, buildHearingNoticePack());

    }

    @Test
    public void shouldGetDocumentsToPrint() {
        when(documentHelper.getCaseDocumentsAsBulkPrintDocuments(anyList())).thenReturn(List.of(getBulkPrintDocument(), getBulkPrintDocument()));
        List<BulkPrintDocument> documentsToPrint = applicantAndRespondentMultiLetterCorresponder.getDocumentsToPrint(caseDetails);
        assertEquals(2, documentsToPrint.size());
    }


    private List<Element<CaseDocument>> buildHearingNoticePack() {
        return List.of(element(UUID.randomUUID(), CaseDocument.builder()
                .documentBinaryUrl(GENERAL_APPLICATION_DIRECTIONS_DOCUMENT_BIN_URL)
                .build()),
            element(UUID.randomUUID(), CaseDocument.builder()
                .documentBinaryUrl(LATEST_DRAFT_ORDER_DOCUMENT_BIN_URL)
                .build()));
    }


}