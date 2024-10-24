package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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
    void testGetEarliestToBeReviewedOrderDate_withValidDates() {
        // Create Reviewable objects with LocalDateTime dates
        DraftOrderDocumentReview reviewable1 = DraftOrderDocumentReview.builder()
            .submittedDate(LocalDateTime.of(2023, 5, 10, 10, 0))
            .build();

        PsaDocumentReview reviewable2 = PsaDocumentReview.builder()
            .submittedDate(LocalDateTime.of(2023, 3, 15, 10, 0))
            .build();

        PsaDocumentReview reviewable3 = PsaDocumentReview.builder()
            .submittedDate(LocalDateTime.of(2023, 4, 20, 10, 0))
            .build();

        // Populate the collections with real objects
        DraftOrderDocReviewCollection draft1 = new DraftOrderDocReviewCollection();
        draft1.setValue(reviewable1);

        PsaDocReviewCollection psa1 = new PsaDocReviewCollection();
        psa1.setValue(reviewable2);

        PsaDocReviewCollection psa2 = new PsaDocReviewCollection();
        psa2.setValue(reviewable3);

        draftOrderDocReviewCollection.add(draft1);
        psaDocReviewCollection.add(psa1);
        psaDocReviewCollection.add(psa2);

        // Call the method under test
        LocalDate earliestDate = underTest.getEarliestToBeReviewedOrderDate();

        // Assert the earliest date is the correct one
        assertEquals(LocalDate.of(2023, 3, 15), earliestDate);
    }

    @Test
    void testGetEarliestToBeReviewedOrderDate_withNullDates() {
        DraftOrderDocumentReview reviewable1 = DraftOrderDocumentReview.builder()
            .submittedDate(null)
            .build();

        PsaDocumentReview reviewable2 = PsaDocumentReview.builder()
            .submittedDate(null)
            .build();

        DraftOrderDocReviewCollection draft1 = new DraftOrderDocReviewCollection();
        draft1.setValue(reviewable1);

        PsaDocReviewCollection psa1 = new PsaDocReviewCollection();
        psa1.setValue(reviewable2);

        draftOrderDocReviewCollection.add(draft1);
        psaDocReviewCollection.add(psa1);

        LocalDate earliestDate = underTest.getEarliestToBeReviewedOrderDate();

        assertNull(earliestDate);
    }

    @Test
    void testGetEarliestToBeReviewedOrderDate_withEmptyCollections() {
        LocalDate earliestDate = underTest.getEarliestToBeReviewedOrderDate();

        assertNull(earliestDate);
    }

    @ParameterizedTest
    @MethodSource("provideMultipleReviewsWithStatusTestCases")
    void testGetEarliestToBeReviewedOrderDate_withMultipleReviewsAndStatus(List<Optional<LocalDateTime>> draftDates,
                                                                           List<Optional<OrderStatus>> draftStatuses,
                                                                           List<Optional<LocalDateTime>> psaDates,
                                                                           List<Optional<OrderStatus>> psaStatuses,
                                                                           List<Optional<LocalDateTime>> draftNotificationDates,
                                                                           List<Optional<LocalDateTime>> psaNotificationDates,
                                                                           LocalDate expected) {

        // Populate draft reviews
        for (int i = 0; i < draftDates.size(); i++) {
            DraftOrderDocumentReview reviewable = DraftOrderDocumentReview.builder()
                .submittedDate(draftDates.get(i).orElse(null)) // Use orElse(null) to handle Optional
                .orderStatus(draftStatuses.get(i).orElse(null)) // Set status
                .notificationSentDate(draftNotificationDates.get(i).orElse(null)) // Use orElse(null)
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
                .notificationSentDate(psaNotificationDates.get(i).orElse(null)) // Use orElse(null)
                .build();

            PsaDocReviewCollection psaCollection = new PsaDocReviewCollection();
            psaCollection.setValue(reviewable);
            psaDocReviewCollection.add(psaCollection);
        }

        // Call the method under test
        LocalDate earliestDate = underTest.getEarliestToBeReviewedOrderDate();

        // Assert the earliest date is the correct one
        assertEquals(expected, earliestDate);
    }

    static Stream<Arguments> provideMultipleReviewsWithStatusTestCases() {
        return Stream.of(
            Arguments.of(
                List.of(Optional.of(LocalDateTime.of(2023, 5, 10, 10, 0)), Optional.of(LocalDateTime.of(2023, 6, 1, 10, 0))),
                List.of(Optional.of(OrderStatus.TO_BE_REVIEWED), Optional.of(OrderStatus.PROCESSED_BY_ADMIN)),
                List.of(Optional.of(LocalDateTime.of(2023, 3, 15, 10, 0)), Optional.of(LocalDateTime.of(2023, 4, 20, 10, 0))),
                List.of(Optional.of(OrderStatus.TO_BE_REVIEWED), Optional.of(OrderStatus.APPROVED_BY_JUDGE)),
                List.of(Optional.empty(), Optional.of(LocalDateTime.of(2023, 4, 19, 10, 0))), // Notification dates
                List.of(Optional.empty(), Optional.of(LocalDateTime.of(2023, 4, 21, 10, 0))), // Notification dates
                LocalDate.of(2023, 3, 15)
            ),
            Arguments.of(
                List.of(Optional.of(LocalDateTime.of(2023, 1, 10, 10, 0)), Optional.of(LocalDateTime.of(2023, 1, 5, 10, 0))),
                List.of(Optional.of(OrderStatus.TO_BE_REVIEWED), Optional.of(OrderStatus.PROCESSED_BY_ADMIN)),
                List.of(Optional.of(LocalDateTime.of(2023, 1, 15, 10, 0)), Optional.of(LocalDateTime.of(2023, 1, 12, 10, 0))),
                List.of(Optional.of(OrderStatus.APPROVED_BY_JUDGE), Optional.of(OrderStatus.PROCESSED_BY_ADMIN)),
                List.of(Optional.empty(), Optional.of(LocalDateTime.of(2023, 1, 4, 10, 0))), // Notification dates
                List.of(Optional.empty(), Optional.empty()), // Notification dates
                LocalDate.of(2023, 1, 10)
            ),
            Arguments.of(
                List.of(Optional.of(LocalDateTime.of(2023, 2, 20, 10, 0))),
                List.of(Optional.of(OrderStatus.TO_BE_REVIEWED)),
                List.of(Optional.of(LocalDateTime.of(2023, 2, 22, 10, 0)), Optional.of(LocalDateTime.of(2023, 2, 21, 10, 0))),
                List.of(Optional.of(OrderStatus.APPROVED_BY_JUDGE), Optional.of(OrderStatus.PROCESSED_BY_ADMIN)),
                List.of(Optional.empty()), // Notification dates
                List.of(Optional.of(LocalDateTime.of(2023, 2, 22, 10, 0)), Optional.of(LocalDateTime.of(2023, 2, 21, 10, 0))), // Notification dates
                LocalDate.of(2023, 2, 20)
            ),
            Arguments.of(
                List.of(Optional.of(LocalDateTime.of(2023, 3, 1, 10, 0))),
                List.of(Optional.of(OrderStatus.TO_BE_REVIEWED)),
                List.of(Optional.of(LocalDateTime.of(2023, 3, 5, 10, 0)), Optional.of(LocalDateTime.of(2023, 3, 3, 10, 0))),
                List.of(Optional.of(OrderStatus.TO_BE_REVIEWED), Optional.of(OrderStatus.APPROVED_BY_JUDGE)),
                List.of(Optional.empty()), // Notification dates
                List.of(Optional.of(LocalDateTime.of(2023, 3, 4, 10, 0)), Optional.of(LocalDateTime.of(2023, 3, 2, 10, 0))), // Notification dates
                LocalDate.of(2023, 3, 1)
            ),
            Arguments.of(
                List.of(Optional.of(LocalDateTime.of(2023, 4, 1, 10, 0)), Optional.of(LocalDateTime.of(2023, 4, 3, 10, 0))),
                List.of(Optional.of(OrderStatus.TO_BE_REVIEWED), Optional.of(OrderStatus.PROCESSED_BY_ADMIN)),
                List.of(Optional.of(LocalDateTime.of(2023, 4, 2, 10, 0)), Optional.of(LocalDateTime.of(2023, 4, 4, 10, 0))),
                List.of(Optional.of(OrderStatus.APPROVED_BY_JUDGE), Optional.of(OrderStatus.PROCESSED_BY_ADMIN)),
                List.of(Optional.empty(), Optional.of(LocalDateTime.of(2023, 4, 3, 10, 0))), // Notification dates
                List.of(Optional.of(LocalDateTime.of(2023, 4, 2, 10, 0)), Optional.empty()), // Notification dates
                LocalDate.of(2023, 4, 1)
            ),
            Arguments.of(
                List.of(Optional.empty()), // Empty DraftOrderDocumentReview
                List.of(Optional.empty()), // No statuses
                List.of(Optional.of(LocalDateTime.of(2023, 5, 5, 10, 0)), Optional.of(LocalDateTime.of(2023, 5, 1, 10, 0))),
                List.of(Optional.of(OrderStatus.TO_BE_REVIEWED), Optional.of(OrderStatus.PROCESSED_BY_ADMIN)),
                List.of(Optional.empty(), Optional.of(LocalDateTime.of(2023, 5, 1, 10, 0))), // Notification dates
                List.of(Optional.empty(), Optional.of(LocalDateTime.of(2023, 5, 2, 10, 0))), // Notification dates
                LocalDate.of(2023, 5, 5)
            ),
            Arguments.of(
                List.of(Optional.empty()), // Empty DraftOrderDocumentReview
                List.of(Optional.empty()), // No statuses
                List.of(Optional.of(LocalDateTime.of(2023, 5, 5, 10, 0)), Optional.of(LocalDateTime.of(2023, 5, 1, 10, 0))),
                List.of(Optional.of(OrderStatus.TO_BE_REVIEWED), Optional.of(OrderStatus.PROCESSED_BY_ADMIN)),
                List.of(Optional.empty(), Optional.of(LocalDateTime.of(2023, 5, 1, 10, 0))), // Notification dates
                List.of(Optional.of(LocalDateTime.of(2023, 5, 2, 10, 0)), Optional.of(LocalDateTime.of(2023, 5, 2, 10, 0))), // Notification dates
                null
            ),
            Arguments.of(
                List.of(Optional.of(LocalDateTime.of(2023, 6, 1, 10, 0))), // Single DraftOrderDocumentReview
                List.of(Optional.of(OrderStatus.TO_BE_REVIEWED)),
                List.of(), // Empty PsaDocumentReview
                List.of(), // No statuses
                List.of(Optional.empty()), // Notification dates
                List.of(), // Notification dates
                LocalDate.of(2023, 6, 1)
            ),
            Arguments.of(
                List.of(), // Empty DraftOrderDocumentReview
                List.of(), // No statuses
                List.of(), // Empty PsaDocumentReview
                List.of(), // No statuses
                List.of(), // No notification dates
                List.of(), // No notification dates
                null // Expect null since there are no reviews
            )
        );
    }


}
