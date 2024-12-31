package uk.gov.hmcts.reform.finrem.caseorchestration.service.processorders;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.processorder.ProcessOrderService;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessOrderServiceTest {

    @InjectMocks
    private ProcessOrderService underTest;

    @ParameterizedTest
    @MethodSource("provideIsAllLegacyApprovedOrdersRemovedTestCases")
    void testIsAllLegacyApprovedOrdersRemoved(FinremCaseData caseDataBefore, FinremCaseData caseData, boolean expectedResult) {
        boolean result = underTest.areAllLegacyApprovedOrdersRemoved(caseDataBefore, caseData);
        assertEquals(expectedResult, result);
    }

    private static Stream<Arguments> provideIsAllLegacyApprovedOrdersRemovedTestCases() {
        final FinremCaseData caseDataBeforeWithOrders = new FinremCaseData();
        final FinremCaseData caseDataWithOrders = new FinremCaseData();
        final FinremCaseData emptyCaseDataBefore = new FinremCaseData();
        final FinremCaseData emptyCaseData = new FinremCaseData();

        caseDataBeforeWithOrders.setUploadHearingOrder(List.of(DirectionOrderCollection.builder().build()));
        caseDataWithOrders.setUploadHearingOrder(List.of(DirectionOrderCollection.builder().build()));
        emptyCaseDataBefore.setUploadHearingOrder(List.of());
        emptyCaseData.setUploadHearingOrder(List.of());

        return Stream.of(
            // Legacy approved orders removed
            Arguments.of(caseDataBeforeWithOrders, emptyCaseData, true),

            // No legacy approved orders to remove
            Arguments.of(emptyCaseDataBefore, emptyCaseData, false),

            // Legacy approved orders still exist
            Arguments.of(caseDataBeforeWithOrders, caseDataWithOrders, false),

            // Legacy approved orders already removed
            Arguments.of(emptyCaseDataBefore, caseDataWithOrders, false)
        );
    }

    @ParameterizedTest
    @MethodSource("provideAreAllNewUploadedOrdersPdfDocumentsPresentTestCases")
    void testAreAllNewUploadedOrdersPdfDocumentsPresent(FinremCaseData caseDataBefore, FinremCaseData caseData, boolean expectedResult) {
        boolean result = underTest.areAllNewOrdersPdfFiles(caseDataBefore, caseData);
        assertEquals(expectedResult, result);
    }

    private static Stream<Arguments> provideAreAllNewUploadedOrdersPdfDocumentsPresentTestCases() {
        DirectionOrderCollection existingWordOrder = createDirectionOrder("http://example.com/document1.DOCX", true);
        DirectionOrderCollection legacyExistingPdfOrder = createDirectionOrder("http://example.com/document1.pdf");

        DirectionOrderCollection newPdfOrder1 = createDirectionOrder("http://example.com/document2.PDF");
        DirectionOrderCollection newPdfOrder2 = createDirectionOrder("http://example.com/document3.pdf");
        DirectionOrderCollection newWordOrder2 = createDirectionOrder("http://example.com/document4.doc");
        DirectionOrderCollection newWordOrder3 = createDirectionOrder("http://example.com/document5.docx");

        return Stream.of(
            // Valid Cases:
            // 1. All new documents are valid PDFs
            Arguments.of(createCaseData(List.of(existingWordOrder), false),
                createCaseData(List.of(existingWordOrder, newPdfOrder1, newPdfOrder2), false), true),
            Arguments.of(createCaseData(List.of(legacyExistingPdfOrder), true),
                createCaseData(List.of(legacyExistingPdfOrder, newPdfOrder1, newPdfOrder2), true), true),
            Arguments.of(createCaseData(List.of(legacyExistingPdfOrder), List.of(existingWordOrder)),
                createCaseData(List.of(legacyExistingPdfOrder), List.of(existingWordOrder, newPdfOrder1, newPdfOrder2)), true),
            Arguments.of(createCaseData(List.of(legacyExistingPdfOrder), List.of(existingWordOrder)),
                createCaseData(List.of(legacyExistingPdfOrder, newPdfOrder2), List.of(existingWordOrder, newPdfOrder1)), true),
            // 2. No new documents
            Arguments.of(createCaseData(List.of(existingWordOrder), false), createCaseData(List.of(existingWordOrder), false), true),
            Arguments.of(createCaseData(List.of(legacyExistingPdfOrder), true), createCaseData(List.of(legacyExistingPdfOrder), true), true),

            // Invalid cases:
            // New document(s) is/are not a PDF document
            Arguments.of(createCaseData(List.of(existingWordOrder), false),
                createCaseData(List.of(existingWordOrder, newWordOrder2), false), false),
            Arguments.of(createCaseData(List.of(legacyExistingPdfOrder), true),
                createCaseData(List.of(legacyExistingPdfOrder, newWordOrder2), true), false),
            Arguments.of(createCaseData(List.of(existingWordOrder), false),
                createCaseData(List.of(existingWordOrder, newWordOrder3), false), false),
            Arguments.of(createCaseData(List.of(legacyExistingPdfOrder), true),
                createCaseData(List.of(legacyExistingPdfOrder, newWordOrder3), true), false),
            Arguments.of(createCaseData(List.of(existingWordOrder), false),
                createCaseData(List.of(existingWordOrder, newWordOrder2, newWordOrder3), false), false),
            Arguments.of(createCaseData(List.of(legacyExistingPdfOrder), true),
                createCaseData(List.of(legacyExistingPdfOrder, newWordOrder2, newWordOrder3), true), false)
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void testAreAllModifyingUnprocessedOrdersWordDocuments(int scenario) {
        // Mocking the unprocessed approved documents
        DraftOrdersWrapper draftOrdersWrapper = mock(DraftOrdersWrapper.class);
        FinremCaseData caseData = mock(FinremCaseData.class);
        when(caseData.getDraftOrdersWrapper()).thenReturn(draftOrdersWrapper);

        // Conditionally set up the mock data based on expectedResult
        if (scenario == 0) {
            when(draftOrdersWrapper.getUnprocessedApprovedDocuments()).thenReturn(
                List.of(
                    createDirectionOrder("http://example.xyz/document.docx"),
                    createDirectionOrder("http://example.xyz/document.docx", true),
                    createDirectionOrder("http://example.xyz/document.docx", "http://example.xyz/document.doc")
                )
            );
        } else {
            if (scenario == 1) {
                when(draftOrdersWrapper.getUnprocessedApprovedDocuments()).thenReturn(
                    List.of(
                        createDirectionOrder("http://example.xyz/document.pdf")
                    )
                );
            } else {
                when(draftOrdersWrapper.getUnprocessedApprovedDocuments()).thenReturn(
                    List.of(
                        createDirectionOrder("http://example.xyz/documentX.docx", "http://example.xyz/document.txt")
                    )
                );
            }
        }

        // Call the method to test
        boolean result = underTest.areAllModifyingUnprocessedOrdersWordDocuments(caseData);

        // Assert the expected result
        if (scenario == 0) {
            assertTrue(result, "Expected all documents to have .doc or .docx extensions");
        } else {
            assertFalse(result, "Expected not all documents to have .doc or .docx extensions, but the method returned true.");
        }
    }

    private static String extractFileName(String url) {
        if (url == null || url.isEmpty()) {
            return null; // or throw an exception if you prefer
        }

        // Find the last '/' and extract the substring after it
        int lastSlashIndex = url.lastIndexOf('/');
        if (lastSlashIndex != -1 && lastSlashIndex < url.length() - 1) {
            return url.substring(lastSlashIndex + 1);
        }

        return null; // No file name found
    }

    private static DirectionOrderCollection createDirectionOrder(String documentUrl) {
        return createDirectionOrder(documentUrl, false);
    }

    private static DirectionOrderCollection createDirectionOrder(String documentUrl, boolean markOriginalDocument) {
        return DirectionOrderCollection.builder()
            .value(DirectionOrder.builder()
                .uploadDraftDocument(CaseDocument.builder().documentUrl(documentUrl).documentFilename(extractFileName(documentUrl)).build())
                .originalDocument(markOriginalDocument
                    ? CaseDocument.builder().documentUrl(documentUrl).documentFilename(extractFileName(documentUrl)).build() : null)
                .build())
            .build();
    }

    private static DirectionOrderCollection createDirectionOrder(String originalDocumentUrl, String newDocumentUrl) {
        return DirectionOrderCollection.builder()
            .value(DirectionOrder.builder()
                .uploadDraftDocument(CaseDocument.builder()
                    .documentUrl(originalDocumentUrl)
                    .documentFilename(extractFileName(newDocumentUrl))
                    .build())
                .originalDocument(CaseDocument.builder()
                    .documentUrl(originalDocumentUrl)
                    .documentFilename(extractFileName(originalDocumentUrl))
                    .build())
                .build())
            .build();
    }

    private static FinremCaseData createCaseData(List<DirectionOrderCollection> legacyTarget, List<DirectionOrderCollection> target) {
        return FinremCaseData.builder()
            .uploadHearingOrder(legacyTarget)
            .draftOrdersWrapper(DraftOrdersWrapper.builder().unprocessedApprovedDocuments(target).build())
            .build();
    }

    private static FinremCaseData createCaseData(List<DirectionOrderCollection> target, boolean legacy) {
        return legacy ? FinremCaseData.builder().uploadHearingOrder(target).build()
            : FinremCaseData.builder().draftOrdersWrapper(DraftOrdersWrapper.builder().unprocessedApprovedDocuments(target).build()).build();
    }
}
