package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;

public class DraftDirectionOrderTest {

    @Test
    void getUploadingDocuments_shouldReturnListWithDocument_whenUploadDraftDocumentIsNotNull() {
        CaseDocument document = caseDocument();

        DraftDirectionOrder underTest = DraftDirectionOrder.builder().build();
        underTest.setUploadDraftDocument(document);

        List<CaseDocument> result = underTest.getUploadingDocuments();

        assertThat(result).containsExactly(document);
    }

    @Test
    void getUploadingDocuments_shouldReturnEmptyList_whenUploadDraftDocumentIsNull() {
        DraftDirectionOrder underTest = DraftDirectionOrder.builder().build();
        underTest.setUploadDraftDocument(null);

        List<CaseDocument> result = underTest.getUploadingDocuments();

        assertThat(result).isEmpty();
    }
}
