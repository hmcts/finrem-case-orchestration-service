package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCaseDetailsBuilderFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AgreedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AgreedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftOrderReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftOrderReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrderStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionSharingAnnex;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionSharingAnnexCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class DraftOrderServiceTest {

    @InjectMocks
    private DraftOrderService draftOrderService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private static Stream<Arguments> draftOrderAndAnnexScenarios() {
        LocalDate now = LocalDate.now();

        // DraftOrder scenarios
        DraftOrder draftOrder1 = DraftOrder.builder().build();
        draftOrder1.setStatus(null); // null status
        draftOrder1.setSubmittedDate(now.minusDays(15)); // 15 days ago (valid)
        draftOrder1.setNotificationSentDate(null); // notification not sent

        DraftOrder draftOrder2 = DraftOrder.builder().build();
        draftOrder2.setStatus(OrderStatus.REVIEW_LATER); // REVIEW_LATER status
        draftOrder2.setSubmittedDate(now.minusDays(10)); // 10 days ago (invalid)
        draftOrder2.setNotificationSentDate(now.minusDays(5)); // notification sent (invalid)

        DraftOrder draftOrder3 = DraftOrder.builder().build();
        draftOrder3.setStatus(OrderStatus.APPROVED); // APPROVED status (invalid)
        draftOrder3.setSubmittedDate(now.minusDays(20)); // 20 days ago (invalid)
        draftOrder3.setNotificationSentDate(null); // notification not sent

        DraftOrder draftOrder4 = DraftOrder.builder().build();
        draftOrder4.setStatus(null); // null status
        draftOrder4.setSubmittedDate(now.minusDays(5)); // 5 days ago (too recent)
        draftOrder4.setNotificationSentDate(null); // notification not sent

        DraftOrder draftOrder5 = DraftOrder.builder().build();
        draftOrder5.setStatus(null); // null status
        draftOrder5.setSubmittedDate(now.minusDays(14)); // Boundary case: exactly 14 days ago
        draftOrder5.setNotificationSentDate(null); // notification not sent

        // PensionSharingAnnex scenarios
        PensionSharingAnnex annex1 = PensionSharingAnnex.builder().build();
        annex1.setStatus(null); // null status
        annex1.setSubmittedDate(now.minusDays(15)); // 15 days ago (valid)
        annex1.setNotificationSentDate(null); // notification not sent

        PensionSharingAnnex annex2 = PensionSharingAnnex.builder().build();
        annex2.setStatus(OrderStatus.REVIEW_LATER); // REVIEW_LATER status
        annex2.setSubmittedDate(now.minusDays(5)); // 5 days ago (too recent)
        annex2.setNotificationSentDate(now.minusDays(3)); // notification sent (invalid)

        PensionSharingAnnex annex3 = PensionSharingAnnex.builder().build();
        annex3.setStatus(OrderStatus.APPROVED); // APPROVED status (invalid)
        annex3.setSubmittedDate(now.minusDays(30)); // 30 days ago (invalid due to status)
        annex3.setNotificationSentDate(null); // notification not sent

        PensionSharingAnnex annex4 = PensionSharingAnnex.builder().build();
        annex4.setStatus(null); // null status
        annex4.setSubmittedDate(now.minusDays(14)); // Boundary case: exactly 14 days ago
        annex4.setNotificationSentDate(null); // notification not sent

        // Return multiple DraftOrderReviewCollection and PensionSharingAnnexCollection scenarios
        return Stream.of(
            // Test case 1: One valid draft order and one valid pension annex
            Arguments.of(List.of(
                List.of(draftOrder1),  // First collection (valid draft order)
                List.of(annex1)        // Second collection (valid pension annex)
            ), 2),

            // Test case 2: No valid draft orders or pension annexes
            Arguments.of(List.of(
                List.of(draftOrder2), // Invalid draft orders
                List.of(annex2)       // Invalid pension annexes
            ), 0),

            // Test case 3: Valid pension annex but invalid draft order
            Arguments.of(List.of(
                List.of(draftOrder3), // Invalid draft order (wrong status)
                List.of(annex1)       // Valid pension annex
            ), 1),

            // Test case 4: Multiple invalid draft orders and pension annexes
            Arguments.of(List.of(
                List.of(draftOrder3, draftOrder4), // Invalid draft orders
                List.of(annex2, annex3)            // Invalid pension annexes
            ), 0),

            // Edge case 1: Draft order exactly 14 days ago (should be excluded)
            Arguments.of(List.of(
                List.of(draftOrder5) // Exactly 14 days ago, but should be excluded
            ), 0),

            // Edge case 2: Pension annex exactly 14 days ago (should be excluded)
            Arguments.of(List.of(
                List.of(annex4) // Exactly 14 days ago, but should be excluded
            ), 0),

            // Edge case 3: Empty draft order and pension annex collections
            Arguments.of(List.of(
                List.of(), // Empty draft order collection
                List.of()  // Empty pension annex collection
            ), 0),

            // Edge case 4: Both draft orders and pension annexes too recent
            Arguments.of(List.of(
                List.of(draftOrder4), // Draft order too recent (5 days ago)
                List.of(annex2)       // Pension annex too recent (5 days ago)
            ), 0),

            // Edge case 5: Valid draft order and invalid pension annex
            Arguments.of(List.of(
                List.of(draftOrder1), // Valid draft order (15 days ago)
                List.of(annex3)       // Invalid pension annex (wrong status)
            ), 1),

            // Edge case 6: Invalid draft order but valid pension annex
            Arguments.of(List.of(
                List.of(draftOrder3), // Invalid draft order (wrong status)
                List.of(annex1)       // Valid pension annex (15 days ago)
            ), 1),

            // Edge case 7: Both draft orders and pension annexes with sent notifications
            Arguments.of(List.of(
                List.of(draftOrder2), // Draft order with notification sent
                List.of(annex2)       // Pension annex with notification sent
            ), 0),

            // Edge case 8: Valid draft order and pension annex with null notification sent date
            Arguments.of(List.of(
                List.of(draftOrder1), // Valid draft order
                List.of(annex1)       // Valid pension annex
            ), 2),

            // Edge case 9: Multiple valid draft orders and pension annexes
            Arguments.of(List.of(
                List.of(draftOrder1, draftOrder5), // One valid, one boundary case (should be excluded)
                List.of(annex1, annex4)            // One valid, one boundary case (should be excluded)
            ), 2)
        );
    }

    @ParameterizedTest
    @MethodSource("draftOrderAndAnnexScenarios")
    void testGetDraftOrderReviewsAndPensionAnnex(List<List<Object>> ordersInCollections, int expectedResults) {
        // Build the mock data structure for multiple DraftOrderReviewCollections and PensionSharingAnnexCollections
        List<DraftOrderReviewCollection> draftOrderReviewCollections = ordersInCollections.stream()
            .map(orders -> DraftOrderReviewCollection.builder()
                .value(DraftOrderReview.builder()
                    .agreedDraftOrderCollection(List.of(AgreedDraftOrderCollection.builder()
                        .value(AgreedDraftOrder.builder()
                            .draftOrderCollection(
                                orders.stream()
                                    .filter(order -> order instanceof DraftOrder)
                                    .map(order -> DraftOrderCollection.builder()
                                        .value((DraftOrder) order)
                                        .build())
                                    .toList()
                            )
                            .pensionSharingAnnexCollection(
                                orders.stream()
                                    .filter(order -> order instanceof PensionSharingAnnex)
                                    .map(order -> PensionSharingAnnexCollection.builder()
                                        .value((PensionSharingAnnex) order)
                                        .build())
                                    .toList()
                            )
                            .build())
                        .build()))
                    .build())
                .build())
            .toList();

        // Mock case details with multiple DraftOrderReviewCollection and PensionSharingAnnexCollection instances
        FinremCaseDetails finremCaseDetails = FinremCaseDetailsBuilderFactory.from(FinremCaseData.builder()
                .draftOrdersWrapper(DraftOrdersWrapper.builder()
                    .draftOrderReviewCollection(draftOrderReviewCollections)
                    .build()))
            .build();

        // Invoke the method to test
        List<DraftOrderReview> results = draftOrderService.getOutstandingDraftOrderReviews(finremCaseDetails, 14);

        // Assert the expected results
        assertEquals(expectedResults, results.size());
    }

}
