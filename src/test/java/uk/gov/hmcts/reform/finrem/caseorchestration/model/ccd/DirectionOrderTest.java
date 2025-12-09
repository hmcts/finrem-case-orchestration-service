package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;

class DirectionOrderTest {

    @Test
    void getUploadingDocuments_shouldReturnListWithDocument_whenUploadDraftDocumentIsNotNull() {
        CaseDocument document = caseDocument();

        DirectionOrder underTest = DirectionOrder.builder().build();
        underTest.setUploadDraftDocument(document);

        List<CaseDocument> result = underTest.getUploadingDocuments();

        assertThat(result).containsExactly(document);
    }

    @Test
    void getUploadingDocuments_shouldReturnEmptyList_whenUploadDraftDocumentIsNull() {
        DirectionOrder underTest = DirectionOrder.builder().build();
        underTest.setUploadDraftDocument(null);

        List<CaseDocument> result = underTest.getUploadingDocuments();

        assertThat(result).isEmpty();
    }

    @Test
    void givenUploadDraftDocument_whenGetApprovedOrder_shouldReturnUploadDraftDocument() {
        CaseDocument mockedDocument = mock(CaseDocument.class);

        DirectionOrder underTest = DirectionOrder.builder().build();
        underTest.setUploadDraftDocument(mockedDocument);

        assertThat(underTest.getApprovedOrder()).isEqualTo(mockedDocument);
    }
}
