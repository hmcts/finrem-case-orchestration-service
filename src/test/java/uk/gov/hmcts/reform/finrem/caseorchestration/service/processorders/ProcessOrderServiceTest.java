package uk.gov.hmcts.reform.finrem.caseorchestration.service.processorders;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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

@ExtendWith(MockitoExtension.class)
class ProcessOrderServiceTest {

    @InjectMocks
    private ProcessOrderService underTest;

    @ParameterizedTest
    @MethodSource("provideIsAllLegacyApprovedOrdersRemovedTestCases")
    void testIsAllLegacyApprovedOrdersRemoved(FinremCaseData caseDataBefore, FinremCaseData caseData, boolean expectedResult) {
        boolean result = underTest.isAllLegacyApprovedOrdersRemoved(caseDataBefore, caseData);
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
    @MethodSource("provideIsAllNewUploadedOrdersArePdfDocumentsTestCases")
    void testIsAllNewUploadedOrdersArePdfDocuments(FinremCaseData caseDataBefore, FinremCaseData caseData, boolean expectedResult) {
        boolean result = underTest.isAllNewUploadedOrdersArePdfDocuments(caseDataBefore, caseData);
        assertEquals(expectedResult, result);
    }

    private static Stream<Arguments> provideIsAllNewUploadedOrdersArePdfDocumentsTestCases() {
        DirectionOrderCollection existingOrder = createDirectionOrder("http://example.com/document1.DOCX");
        DirectionOrderCollection newNonWordOrder1 = createDirectionOrder("http://example.com/document2.PDF");
        DirectionOrderCollection newNonWordOrder2 = createDirectionOrder("http://example.com/document3.pdf");
        DirectionOrderCollection newWordOrder = createDirectionOrder("http://example.com/document4.doc");
        DirectionOrderCollection newMixedCaseOrder = createDirectionOrder("http://example.com/document5.Pdf");

        FinremCaseData caseDataBefore1 = createCaseData(List.of(existingOrder));
        FinremCaseData caseDataAfter1 = createCaseData(List.of(existingOrder, newNonWordOrder1, newNonWordOrder2));

        FinremCaseData caseDataBefore2 = createCaseData(List.of(existingOrder));
        FinremCaseData caseDataAfter2 = createCaseData(List.of(existingOrder, newNonWordOrder1, newWordOrder));

        FinremCaseData caseDataBefore3 = createCaseData(List.of(existingOrder));
        FinremCaseData caseDataAfter3 = createCaseData(List.of(existingOrder));

        FinremCaseData caseDataBefore4 = createCaseData(List.of(existingOrder));
        FinremCaseData caseDataAfter4 = createCaseData(List.of(existingOrder, newMixedCaseOrder));

        FinremCaseData caseDataBefore5 = createCaseData(List.of());
        FinremCaseData caseDataAfter5 = createCaseData(List.of());

        return Stream.of(
            Arguments.of(caseDataBefore1, caseDataAfter1, true),  // All new documents are valid Word documents
            Arguments.of(caseDataBefore2, caseDataAfter2, false), // One new document is not a PDF document
            Arguments.of(caseDataBefore3, caseDataAfter3, true),  // No new documents
            Arguments.of(caseDataBefore4, caseDataAfter4, true),   // Mixed-case Word document extensions are valid
            Arguments.of(caseDataBefore5, caseDataAfter5, true)   //
        );
    }

    private static DirectionOrderCollection createDirectionOrder(String documentUrl) {
        return DirectionOrderCollection.builder()
            .value(DirectionOrder.builder()
                .originalDocument(CaseDocument.builder().documentUrl(documentUrl).build())
                .build())
            .build();
    }

    private static FinremCaseData createCaseData(List<DirectionOrderCollection> unprocessedApprovedDocuments) {
        return FinremCaseData.builder()
            .draftOrdersWrapper(DraftOrdersWrapper.builder()
                .unprocessedApprovedDocuments(unprocessedApprovedDocuments)
                .build())
            .build();
    }
}
