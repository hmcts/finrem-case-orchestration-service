package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

class DocumentCollectionItemTest {

    @Test
    void shouldReturnNullWhenCaseDocumentIsNull() {
        assertNull(DocumentCollectionItem.fromCaseDocument(null));
    }

    @Test
    void shouldWrapCaseDocumentWhenNotNull() {
        CaseDocument caseDocument = mock(CaseDocument.class);
        DocumentCollectionItem result = DocumentCollectionItem.fromCaseDocument(caseDocument);
        assertNotNull(result);
        assertEquals(caseDocument, result.getValue());
    }
}

