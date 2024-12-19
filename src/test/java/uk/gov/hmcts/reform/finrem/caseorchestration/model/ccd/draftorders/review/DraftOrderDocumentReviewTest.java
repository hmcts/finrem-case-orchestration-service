package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DraftOrderDocumentReviewTest {

    private final DraftOrderDocumentReview underTest = new DraftOrderDocumentReview();

    static Stream<Arguments> matchTestCases() {
        CaseDocument doc1 = CaseDocument.builder().documentUrl("url1").build();
        CaseDocument doc2 = CaseDocument.builder().documentUrl("url2").build();
        CaseDocument doc3 = CaseDocument.builder().documentUrl("url3").build();

        return Stream.of(
            Arguments.of(doc1, doc1, true),  // Target doc matches draft order document
            Arguments.of(doc2, doc1, false), // Target doc does not match draft order document
            Arguments.of(doc3, doc2, false), // Target doc does not match draft order document
            Arguments.of(null, doc2, false)   // Null target doc, should return false
        );
    }

    @Test
    void testReplaceDocument() {
        // Given an amended document
        CaseDocument amendedDocument = CaseDocument.builder().documentUrl("newUrl").build();

        // Call replaceDocument to set the draftOrderDocument
        underTest.replaceDocument(amendedDocument);

        // Assert that the draftOrderDocument was set to the amended document
        assertEquals(amendedDocument, underTest.getDraftOrderDocument(), "The draftOrderDocument should be replaced with the amended document.");
    }

    @ParameterizedTest
    @MethodSource("matchTestCases")
    void testMatch(CaseDocument targetDoc, CaseDocument draftOrderDocument, boolean expectedResult) {
        // Set the initial draftOrderDocument in the underTest object
        underTest.replaceDocument(draftOrderDocument);

        // Perform the match operation and assert the result
        boolean result = underTest.match(targetDoc);
        assertEquals(expectedResult, result, "The match result should be as expected.");
    }
}
