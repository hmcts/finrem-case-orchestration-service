package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgreedDraftOrderTest {

    private final AgreedDraftOrder underTest = new AgreedDraftOrder();

    static Stream<Arguments> matchTestCases() {
        CaseDocument doc1 = CaseDocument.builder().documentUrl("url1").build();
        CaseDocument doc2 = CaseDocument.builder().documentUrl("url2").build();
        CaseDocument doc3 = CaseDocument.builder().documentUrl("url3").build();

        return Stream.of(
            Arguments.of(doc1, doc1, doc2, true),  // Target doc matches draft order
            Arguments.of(doc2, doc1, doc2, true),  // Target doc matches pension sharing annex
            Arguments.of(doc3, doc1, doc2, false), // Target doc does not match either
            Arguments.of(null, doc1, doc2, false)  // Null target doc, should return false
        );
    }

    @ParameterizedTest
    @MethodSource("matchTestCases")
    void testMatch(CaseDocument targetDoc, CaseDocument draftOrder, CaseDocument pensionSharingAnnex, boolean expectedResult) {
        // Set the fields for the class under test
        underTest.setDraftOrder(draftOrder);
        underTest.setPensionSharingAnnex(pensionSharingAnnex);

        // Perform the test and assert the result
        boolean result = underTest.match(targetDoc);
        if (expectedResult) {
            assertTrue(result);
        } else {
            assertFalse(result);
        }
    }

    static Stream<Arguments> replaceDocumentTestCases() {
        CaseDocument doc1 = CaseDocument.builder().documentUrl("url1").build();
        CaseDocument doc2 = CaseDocument.builder().documentUrl("url2").build();
        CaseDocument doc3 = CaseDocument.builder().documentUrl("url3").build();

        return Stream.of(
            Arguments.of(doc1, doc1, doc2, doc1),  // Replace draft order with doc1
            Arguments.of(doc2, null, doc2, doc2),  // Replace pension sharing annex with doc2
            Arguments.of(doc3, doc1, null, doc3),  // Replace draft order with doc3 when pensionSharingAnnex is null
            Arguments.of(doc1, null, null, null)   // Should throw IllegalArgumentException
        );
    }

    @ParameterizedTest
    @MethodSource("replaceDocumentTestCases")
    void testReplaceDocument(CaseDocument amendedDocument, CaseDocument initialDraftOrder, CaseDocument initialPensionSharingAnnex,
                             CaseDocument expectedDocument) {
        // Set the fields for the class under test
        underTest.setDraftOrder(initialDraftOrder);
        underTest.setPensionSharingAnnex(initialPensionSharingAnnex);

        // Perform the replace document operation
        if (expectedDocument == null) {
            assertThrows(IllegalArgumentException.class, () -> underTest.replaceDocument(amendedDocument));
        } else {
            underTest.replaceDocument(amendedDocument);
            // Assert the replaced document
            if (initialDraftOrder != null) {
                assertEquals(expectedDocument, underTest.getDraftOrder());
            } else {
                assertEquals(expectedDocument, underTest.getPensionSharingAnnex());
            }
        }
    }

}
