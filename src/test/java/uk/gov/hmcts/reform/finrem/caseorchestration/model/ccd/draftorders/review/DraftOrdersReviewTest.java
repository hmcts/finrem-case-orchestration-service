package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Reviewable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


class DraftOrdersReviewTest {

    private List<DraftOrderDocReviewCollection> draftOrderDocReviewCollection;

    private List<PsaDocReviewCollection> psaDocReviewCollection;

    private DraftOrdersReview underTest;

    @BeforeEach
    void setUp() {
        draftOrderDocReviewCollection = new ArrayList<>();
        psaDocReviewCollection = new ArrayList<>();
        underTest = DraftOrdersReview.builder()
            .draftOrderDocReviewCollection(draftOrderDocReviewCollection)
            .psaDocReviewCollection(psaDocReviewCollection)
            .build();
    }

    @Test
    void testGetLatestToBeReviewedOrder_withValidDates() {
        DraftOrderDocumentReview reviewable1 = DraftOrderDocumentReview.builder()
            .orderStatus(OrderStatus.TO_BE_REVIEWED)
            .submittedDate(LocalDateTime.of(2023, 5, 10, 10, 0))
            .build();

        PsaDocumentReview reviewable2 = PsaDocumentReview.builder()
            .orderStatus(OrderStatus.TO_BE_REVIEWED)
            .submittedDate(LocalDateTime.of(2023, 3, 15, 10, 0))
            .build();

        PsaDocumentReview reviewable3 = PsaDocumentReview.builder()
            .orderStatus(OrderStatus.TO_BE_REVIEWED)
            .submittedDate(LocalDateTime.of(2023, 4, 20, 10, 0))
            .build();

        DraftOrderDocReviewCollection draft1 = new DraftOrderDocReviewCollection();
        draft1.setValue(reviewable1);

        PsaDocReviewCollection psa1 = new PsaDocReviewCollection();
        psa1.setValue(reviewable2);

        PsaDocReviewCollection psa2 = new PsaDocReviewCollection();
        psa2.setValue(reviewable3);

        draftOrderDocReviewCollection.add(draft1);
        psaDocReviewCollection.add(psa1);
        psaDocReviewCollection.add(psa2);

        Reviewable foundReview = underTest.getLatestToBeReviewedOrder();

        assertEquals(LocalDateTime.of(2023, 5, 10, 10, 0), foundReview.getSubmittedDate());
    }

    @Test
    void testGetLatestToBeReviewedOrder_withNullDates() {
        DraftOrderDocumentReview reviewable1 = DraftOrderDocumentReview.builder()
            .orderStatus(OrderStatus.TO_BE_REVIEWED)
            .submittedDate(null)
            .build();

        PsaDocumentReview reviewable2 = PsaDocumentReview.builder()
            .orderStatus(OrderStatus.TO_BE_REVIEWED)
            .submittedDate(null)
            .build();

        DraftOrderDocReviewCollection draft1 = new DraftOrderDocReviewCollection();
        draft1.setValue(reviewable1);

        PsaDocReviewCollection psa1 = new PsaDocReviewCollection();
        psa1.setValue(reviewable2);

        draftOrderDocReviewCollection.add(draft1);
        psaDocReviewCollection.add(psa1);

        Reviewable foundReview = underTest.getLatestToBeReviewedOrder();

        assertNull(foundReview);
    }

    @Test
    void testGetLatestToBeReviewedOrder_withEmptyCollections() {
        Reviewable foundReview = underTest.getLatestToBeReviewedOrder();

        assertNull(foundReview);
    }

    @ParameterizedTest
    @MethodSource("provideMultipleReviewsWithStatusTestCases")
    void testGetEarliestToBeReviewedOrderDate_withMultipleReviewsAndStatus(List<Optional<LocalDateTime>> draftDates,
                                                                           List<Optional<OrderStatus>> draftStatuses,
                                                                           List<Optional<LocalDateTime>> psaDates,
                                                                           List<Optional<OrderStatus>> psaStatuses,
                                                                           LocalDateTime expected) {

        // Populate draft reviews
        for (int i = 0; i < draftDates.size(); i++) {
            DraftOrderDocumentReview reviewable = DraftOrderDocumentReview.builder()
                .submittedDate(draftDates.get(i).orElse(null)) // Use orElse(null) to handle Optional
                .orderStatus(draftStatuses.get(i).orElse(null)) // Set status
                .build();

            DraftOrderDocReviewCollection draftCollection = new DraftOrderDocReviewCollection();
            draftCollection.setValue(reviewable);
            draftOrderDocReviewCollection.add(draftCollection);
        }

        // Populate PSA reviews
        for (int i = 0; i < psaDates.size(); i++) {
            PsaDocumentReview reviewable = PsaDocumentReview.builder()
                .submittedDate(psaDates.get(i).orElse(null)) // Use orElse(null)
                .orderStatus(psaStatuses.get(i).orElse(null)) // Set status
                .build();

            PsaDocReviewCollection psaCollection = new PsaDocReviewCollection();
            psaCollection.setValue(reviewable);
            psaDocReviewCollection.add(psaCollection);
        }

        // Call the method under test
        Reviewable foundReview = underTest.getLatestToBeReviewedOrder();

        if (expected == null) {
            assertThat(foundReview).isNull();
        } else {
            assertEquals(expected, foundReview.getSubmittedDate());
        }
    }

    static Stream<Arguments> provideMultipleReviewsWithStatusTestCases() {
        return Stream.of(
            Arguments.of(
                List.of(Optional.of(LocalDateTime.of(2023, 5, 10, 10, 0)),
                    Optional.of(LocalDateTime.of(2023, 6, 1, 10, 0))),
                List.of(Optional.of(OrderStatus.TO_BE_REVIEWED), Optional.of(OrderStatus.PROCESSED_BY_ADMIN)),
                List.of(Optional.of(LocalDateTime.of(2023, 3, 15, 10, 0)),
                    Optional.of(LocalDateTime.of(2023, 4, 20, 10, 0))),
                List.of(Optional.of(OrderStatus.TO_BE_REVIEWED), Optional.of(OrderStatus.APPROVED_BY_JUDGE)),
                LocalDateTime.of(2023, 5, 10, 10, 0)
            ),
            Arguments.of(
                List.of(Optional.of(LocalDateTime.of(2023, 1, 10, 10, 0)),
                    Optional.of(LocalDateTime.of(2023, 1, 5, 10, 0))),
                List.of(Optional.of(OrderStatus.TO_BE_REVIEWED), Optional.of(OrderStatus.PROCESSED_BY_ADMIN)),
                List.of(Optional.of(LocalDateTime.of(2023, 1, 15, 10, 0)),
                    Optional.of(LocalDateTime.of(2023, 1, 12, 10, 0))),
                List.of(Optional.of(OrderStatus.APPROVED_BY_JUDGE), Optional.of(OrderStatus.PROCESSED_BY_ADMIN)),
                LocalDateTime.of(2023, 1, 10, 10, 0)
            ),
            Arguments.of(
                List.of(Optional.of(LocalDateTime.of(2023, 2, 20, 10, 0))),
                List.of(Optional.of(OrderStatus.TO_BE_REVIEWED)),
                List.of(Optional.of(LocalDateTime.of(2023, 2, 22, 10, 0)),
                    Optional.of(LocalDateTime.of(2023, 2, 21, 10, 0))),
                List.of(Optional.of(OrderStatus.APPROVED_BY_JUDGE), Optional.of(OrderStatus.PROCESSED_BY_ADMIN)),
                LocalDateTime.of(2023, 2, 20, 10, 0)
            ),
            Arguments.of(
                List.of(Optional.of(LocalDateTime.of(2023, 3, 1, 10, 0))),
                List.of(Optional.of(OrderStatus.TO_BE_REVIEWED)),
                List.of(Optional.of(LocalDateTime.of(2023, 3, 5, 10, 0)),
                    Optional.of(LocalDateTime.of(2023, 3, 3, 10, 0))),
                List.of(Optional.of(OrderStatus.TO_BE_REVIEWED), Optional.of(OrderStatus.APPROVED_BY_JUDGE)),
                LocalDateTime.of(2023, 3, 5, 10, 0)
            ),
            Arguments.of(
                List.of(Optional.of(LocalDateTime.of(2023, 4, 1, 10, 0)),
                    Optional.of(LocalDateTime.of(2023, 4, 3, 10, 0))),
                List.of(Optional.of(OrderStatus.TO_BE_REVIEWED), Optional.of(OrderStatus.PROCESSED_BY_ADMIN)),
                List.of(Optional.of(LocalDateTime.of(2023, 4, 2, 10, 0)),
                    Optional.of(LocalDateTime.of(2023, 4, 4, 10, 0))),
                List.of(Optional.of(OrderStatus.APPROVED_BY_JUDGE), Optional.of(OrderStatus.PROCESSED_BY_ADMIN)),
                LocalDateTime.of(2023, 4, 1, 10, 0)
            ),
            Arguments.of(
                List.of(Optional.empty()), // Empty DraftOrderDocumentReview
                List.of(Optional.empty()), // No statuses
                List.of(Optional.of(LocalDateTime.of(2023, 5, 5, 10, 0)),
                    Optional.of(LocalDateTime.of(2023, 5, 1, 10, 0))),
                List.of(Optional.of(OrderStatus.TO_BE_REVIEWED), Optional.of(OrderStatus.PROCESSED_BY_ADMIN)),
                LocalDateTime.of(2023, 5, 5, 10, 0)
            ),
            Arguments.of(
                List.of(Optional.empty()), // Empty DraftOrderDocumentReview
                List.of(Optional.empty()), // No statuses
                List.of(Optional.of(LocalDateTime.of(2023, 5, 5, 10, 0)),
                    Optional.of(LocalDateTime.of(2023, 5, 1, 10, 0))),
                List.of(Optional.of(OrderStatus.TO_BE_REVIEWED), Optional.of(OrderStatus.PROCESSED_BY_ADMIN)),
                LocalDateTime.of(2023, 5, 5, 10, 0)
            ),
            Arguments.of(
                List.of(Optional.of(LocalDateTime.of(2023, 6, 1, 10, 0))), // Single DraftOrderDocumentReview
                List.of(Optional.of(OrderStatus.TO_BE_REVIEWED)),
                List.of(), // Empty PsaDocumentReview
                List.of(), // No statuses
                LocalDateTime.of(2023, 6, 1, 10, 0)
            ),
            Arguments.of(
                List.of(), // Empty DraftOrderDocumentReview
                List.of(), // No statuses
                List.of(), // Empty PsaDocumentReview
                List.of(), // No statuses
                null // Expect null since there are no reviews
            )
        );
    }

}