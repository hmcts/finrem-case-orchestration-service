package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;

public class DirectionOrderTest {

    @Test
    void getUploadingDocuments_shouldReturnListWithDocument_whenUploadDraftDocumentIsNotNull() {
        CaseDocument document = caseDocument();

        DirectionOrder myClass = DirectionOrder.builder().build();
        myClass.setUploadDraftDocument(document);

        List<CaseDocument> result = myClass.getUploadingDocuments();

        assertThat(result).containsExactly(document);
    }

    @Test
    void getUploadingDocuments_shouldReturnEmptyList_whenUploadDraftDocumentIsNull() {
        DirectionOrder myClass = DirectionOrder.builder().build();
        myClass.setUploadDraftDocument(null);

        List<CaseDocument> result = myClass.getUploadingDocuments();

        assertThat(result).isEmpty();
    }
}
