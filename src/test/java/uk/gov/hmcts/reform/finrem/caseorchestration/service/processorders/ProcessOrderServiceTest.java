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
    @MethodSource("provideAreAllNewUploadedOrdersPdfDocumentsPresentTestCases")
    void testAreAllNewUploadedOrdersPdfDocumentsPresent(FinremCaseData caseDataBefore, FinremCaseData caseData, boolean expectedResult) {
        boolean result = underTest.areAllNewUploadedOrdersPdfDocumentsPresent(caseDataBefore, caseData);
        assertEquals(expectedResult, result);
    }

    private static Stream<Arguments> provideAreAllNewUploadedOrdersPdfDocumentsPresentTestCases() {
        DirectionOrderCollection existingOrder = createDirectionOrder("http://example.com/document1.DOCX");
        DirectionOrderCollection newNonWordOrder1 = createDirectionOrder("http://example.com/document2.PDF");
        DirectionOrderCollection newNonWordOrder2 = createDirectionOrder("http://example.com/document3.pdf");
        DirectionOrderCollection newWordOrder = createDirectionOrder("http://example.com/document4.doc");
        DirectionOrderCollection newMixedCaseNonWordOrder = createDirectionOrder("http://example.com/document5.Pdf");

        return Stream.of(
            // All new documents are valid Word documents
            Arguments.of(createCaseData(List.of(existingOrder)), createCaseData(List.of(existingOrder, newNonWordOrder1, newNonWordOrder2)), true),
            Arguments.of(createCaseData(List.of(existingOrder), true),
                createCaseData(List.of(existingOrder, newNonWordOrder1, newNonWordOrder2), true), true),
            // One new document is not a PDF document
            Arguments.of(createCaseData(List.of(existingOrder)), createCaseData(List.of(existingOrder, newNonWordOrder1, newWordOrder)), false),
            Arguments.of(createCaseData(List.of(existingOrder), true),
                createCaseData(List.of(existingOrder, newNonWordOrder1, newWordOrder), true), false),
            // No new documents
            Arguments.of(createCaseData(List.of(existingOrder)), createCaseData(List.of(existingOrder)), true),
            Arguments.of(createCaseData(List.of(existingOrder), true), createCaseData(List.of(existingOrder), true), true),
            // Mixed-case Word document extensions are valid
            Arguments.of(createCaseData(List.of(existingOrder)), createCaseData(List.of(existingOrder, newMixedCaseNonWordOrder)), true),
            Arguments.of(createCaseData(List.of(existingOrder), true), createCaseData(List.of(existingOrder, newMixedCaseNonWordOrder), true), true),
            // no documents
            Arguments.of(createCaseData(List.of()), createCaseData(List.of()), true),
            Arguments.of(createCaseData(List.of(), true), createCaseData(List.of(), true), true)
        );
    }

    private static DirectionOrderCollection createDirectionOrder(String documentUrl) {
        return createDirectionOrder(documentUrl, false);
    }

    private static DirectionOrderCollection createDirectionOrder(String documentUrl, boolean legacy) {
        return DirectionOrderCollection.builder()
            .value(DirectionOrder.builder()
                .uploadDraftDocument(CaseDocument.builder().documentUrl(documentUrl).build())
                .originalDocument(legacy ? null : CaseDocument.builder().documentUrl(documentUrl).build())
                .build())
            .build();
    }

    private static FinremCaseData createCaseData(List<DirectionOrderCollection> target) {
        return createCaseData(target, false);
    }

    private static FinremCaseData createCaseData(List<DirectionOrderCollection> target, boolean legacy) {
        return legacy ? FinremCaseData.builder().uploadHearingOrder(target).build()
            : FinremCaseData.builder().draftOrdersWrapper(DraftOrdersWrapper.builder().unprocessedApprovedDocuments(target).build()).build();
    }
}
