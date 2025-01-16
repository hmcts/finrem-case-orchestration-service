package uk.gov.hmcts.reform.finrem.caseorchestration.service.judgeapproval;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.JudgeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.CaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeApproval;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.RefusalOrderInstruction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocumentReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.PsaDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.PsaDocumentReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.RefusedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.RefusedOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.draftorders.HasApprovableCollectionReader;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeDecision.LEGAL_REP_NEEDS_TO_MAKE_CHANGE;

@ExtendWith(MockitoExtension.class)
class RefusedOrderProcessorTest {

    private static final CaseDocument TARGET_DOCUMENT = CaseDocument.builder().documentUrl("targetUrl").build();
    private static final CaseDocument GENERATED_REFUSED_ORDER = CaseDocument.builder().documentUrl("generatedRefusedOrder.pdf").build();
    private static final String JUDGE_FEEDBACK = "Refusal reason";
    private static final String APPROVED_JUDGE_NAME = "Mary Chapman";
    private static final LocalDateTime FIXED_DATE_TIME = LocalDateTime.of(2024, 11, 4, 9, 0, 0);
    private static final LocalDate HEARING_DATE = LocalDate.of(2024, 1, 1);
    private static final String SUBMITTED_BY = "Claire";
    private static final String SUBMITTED_BY_EMAIL = "claire@solicitor.com";
    private static final LocalDateTime SUBMITTED_DATE = LocalDateTime.of(1989, 6, 4, 0, 0);
    private static final List<CaseDocumentCollection> ATTACHMENTS = List.of(
        CaseDocumentCollection.builder().value(CaseDocument.builder().documentUrl("randomDoc.pdf").build()).build()
    );

    @InjectMocks
    private RefusedOrderProcessor underTest;

    @Mock
    private RefusedOrderGenerator refusedOrderGenerator;

    @Spy
    private HasApprovableCollectionReader hasApprovableCollectionReader;

    @ParameterizedTest
    @MethodSource("provideProcessRefusedDocumentsAndUpdateTheirState")
    void shouldProcessRefusedDocumentsAndUpdateTheirState(JudgeApproval judgeApproval,
                                                          RefusalOrderInstruction refusalOrderInstruction,
                                                          List<AgreedDraftOrderCollection> agreedDraftOrderCollections,
                                                          List<DraftOrdersReviewCollection> draftOrdersReviewCollection,
                                                          List<RefusedOrderCollection> existingRefusedOrders,
                                                          AgreedDraftOrderCollection targetAgreedDraftOrderCollection,
                                                          List<DraftOrderDocReviewCollection> targetDraftOrderDocReviewCollection,
                                                          PsaDocReviewCollection targetPsaDocReviewCollection,
                                                          RefusedOrder expectedRefusedOrder,
                                                          int expectedRefusedOrdersSize,
                                                          int expectedRefusedOrderIdsSize) {
        DraftOrdersWrapper draftOrdersWrapper = DraftOrdersWrapper.builder()
            .agreedDraftOrderCollection(agreedDraftOrderCollections)
            .draftOrdersReviewCollection(draftOrdersReviewCollection)
            .refusedOrdersCollection(existingRefusedOrders)
            .refusalOrderInstruction(refusalOrderInstruction)
            .build();

        lenient().when(refusedOrderGenerator.generateRefuseOrder(any(FinremCaseDetails.class), eq(JUDGE_FEEDBACK), eq(FIXED_DATE_TIME),
            eq(APPROVED_JUDGE_NAME), refusalOrderInstruction == null ? isNull() : eq(refusalOrderInstruction.getJudgeType()), eq(AUTH_TOKEN)))
            .thenReturn(GENERATED_REFUSED_ORDER);

        try (MockedStatic<LocalDateTime> mockedStatic = Mockito.mockStatic(LocalDateTime.class, Mockito.CALLS_REAL_METHODS)) {
            mockedStatic.when(LocalDateTime::now).thenReturn(FIXED_DATE_TIME);

            underTest.processRefusedOrders(FinremCaseDetails.builder()
                    .id(Long.valueOf(CASE_ID))
                    .data(FinremCaseData.builder()
                        .draftOrdersWrapper(draftOrdersWrapper)
                        .build())
                    .build(),
                draftOrdersWrapper, judgeApproval, AUTH_TOKEN);

            assertNull(draftOrdersWrapper.getGeneratedOrderReason());
            assertNull(draftOrdersWrapper.getGeneratedOrderRefusedDate());
            assertNull(draftOrdersWrapper.getGeneratedOrderJudgeName());
            assertNull(draftOrdersWrapper.getGeneratedOrderJudgeType());

            // expected refusal order count
            int result = Stream.of(
                Optional.ofNullable(targetAgreedDraftOrderCollection),
                Optional.ofNullable(targetDraftOrderDocReviewCollection),
                Optional.ofNullable(targetPsaDocReviewCollection)
            ).allMatch(Optional::isEmpty) ? 0 : 1;

            assertThat(draftOrdersWrapper.getRefusedOrdersCollection()).hasSize(expectedRefusedOrdersSize);
            assertThat(draftOrdersWrapper.getRefusalOrderIdsToBeSent()).hasSize(expectedRefusedOrderIdsSize);

            if (result != 0) {
                // verify agreedDraftOrderToBeExamined is removed
                assertTrue(isAgreedDraftOrderAbsent(draftOrdersWrapper.getAgreedDraftOrderCollection(), targetAgreedDraftOrderCollection));

                List<DraftOrdersReviewCollection> newDraftOrdersReviewCollections = draftOrdersWrapper.getDraftOrdersReviewCollection();
                if (targetDraftOrderDocReviewCollection != null) {
                    targetDraftOrderDocReviewCollection.forEach(a ->
                        assertTrue(isDraftOrderDocumentReviewAbsent(newDraftOrdersReviewCollections, a)));
                }
                if (targetPsaDocReviewCollection != null) {
                    assertTrue(isPsaDocumentReviewAbsent(newDraftOrdersReviewCollections, targetPsaDocReviewCollection));
                }
                draftOrdersWrapper.getRefusedOrdersCollection().stream()
                    .max(Comparator.comparing(r -> r.getValue().getRefusedDate(), Comparator.nullsFirst(Comparator.naturalOrder())))
                    .ifPresentOrElse(r -> {
                        assertEquals(expectedRefusedOrder, r.getValue());
                        assertThat(draftOrdersWrapper.getRefusalOrderIdsToBeSent()).hasSize(1);
                        draftOrdersWrapper.getRefusalOrderIdsToBeSent().stream().findFirst()
                            .ifPresentOrElse(u -> assertEquals(r.getId(), u.getValue()), () -> fail("Unexpected missing refused order id"));
                    }, () -> fail("Unexpected missing refused order"));
            }
        }
    }

    static Stream<Arguments> provideProcessRefusedDocumentsAndUpdateTheirState() {
        JudgeApproval judgeApproval = JudgeApproval.builder()
            .hearingDate(HEARING_DATE)
            .judgeDecision(LEGAL_REP_NEEDS_TO_MAKE_CHANGE)
            .changesRequestedByJudge(JUDGE_FEEDBACK)
            .document(TARGET_DOCUMENT)
            .build();
        return Stream.of(
            targetDocNotFound(judgeApproval),
            refuseTargetedDraftOrder(judgeApproval, 1, true),
            refuseTargetedDraftOrder(judgeApproval, 1, false),
            refuseTargetedDraftOrder(judgeApproval, 3, true),
            refuseTargetedDraftOrder(judgeApproval, 3, false),
            refuseTargetedPsa(judgeApproval, true),
            refuseTargetedPsa(judgeApproval, false),
            refuseTargetedPsaWithExistingRefusedOrders(judgeApproval)
        );
    }

    static Arguments targetDocNotFound(JudgeApproval judgeApproval) {
        return Arguments.of(judgeApproval, null,
            List.of(),
            List.of(
                DraftOrdersReviewCollection.builder()
                    .value(
                        DraftOrdersReview.builder()
                            .hearingJudge("Peter Chapman")
                            .hearingType("Hearing 1")
                            .psaDocReviewCollection(List.of(
                                PsaDocReviewCollection.builder().value(PsaDocumentReview.builder().build()).build())
                            )
                            .draftOrderDocReviewCollection(List.of(
                                DraftOrderDocReviewCollection.builder().value(DraftOrderDocumentReview.builder().build()).build()
                            ))
                            .build()
                    )
                    .build()
            ),
            List.of(),
            null,
            null,
            null,
            null,
            0, 0
        );
    }

    private static DraftOrderDocReviewCollection buildDraftOrderReviewCollection(boolean withAttachment) {
        return DraftOrderDocReviewCollection.builder().value(DraftOrderDocumentReview.builder()
                .draftOrderDocument(TARGET_DOCUMENT)
                .submittedBy(SUBMITTED_BY)
                .submittedByEmail(SUBMITTED_BY_EMAIL)
                .submittedDate(SUBMITTED_DATE)
                .attachments(withAttachment ? ATTACHMENTS : null)
                .orderStatus(OrderStatus.REFUSED)
                .refusedDate(FIXED_DATE_TIME)
                .approvalJudge("Mary Chapman")
                .build()).build();
    }

    static Arguments refuseTargetedDraftOrder(JudgeApproval judgeApproval, int refusedOrderCount, boolean withAttachment) {
        AgreedDraftOrderCollection agreedDraftOrderCollectionToBeExamined =
            AgreedDraftOrderCollection.builder().value(AgreedDraftOrder.builder()
                    .draftOrder(TARGET_DOCUMENT)
                    .orderStatus(OrderStatus.REFUSED)
                    .build())
                .build();

        List<DraftOrderDocReviewCollection> draftOrderDocReviewCollections = new ArrayList<>();
        List<DraftOrderDocReviewCollection> targetDraftOrderDocReviewCollection = new ArrayList<>();
        for (int i = 0; i < refusedOrderCount; i++) {
            DraftOrderDocReviewCollection draftOrderDocReviewCollectionToBeExamined = buildDraftOrderReviewCollection(withAttachment);
            draftOrderDocReviewCollections.add(draftOrderDocReviewCollectionToBeExamined);
            targetDraftOrderDocReviewCollection.add(draftOrderDocReviewCollectionToBeExamined);
        }

        return Arguments.of(judgeApproval, null,
            List.of(agreedDraftOrderCollectionToBeExamined),
            List.of(
                DraftOrdersReviewCollection.builder()
                    .value(
                        DraftOrdersReview.builder()
                            .hearingJudge("Peter Chapman")
                            .hearingType("First Directions Appointment (FDA)")
                            .psaDocReviewCollection(List.of(
                                PsaDocReviewCollection.builder().value(PsaDocumentReview.builder()
                                        .psaDocument(CaseDocument.builder().build()).build())
                                    .build()
                            ))
                            .draftOrderDocReviewCollection(draftOrderDocReviewCollections)
                            .build()
                    )
                    .build()
            ),
            List.of(),
            agreedDraftOrderCollectionToBeExamined,
            targetDraftOrderDocReviewCollection,
            null,
            RefusedOrder.builder()
                .refusedDate(FIXED_DATE_TIME)
                .refusedDocument(TARGET_DOCUMENT)
                .refusalOrder(GENERATED_REFUSED_ORDER)
                .judgeFeedback(JUDGE_FEEDBACK)
                .refusalJudge(APPROVED_JUDGE_NAME)
                .submittedByEmail(SUBMITTED_BY_EMAIL)
                .submittedDate(SUBMITTED_DATE)
                .submittedBy(SUBMITTED_BY)
                .attachments(withAttachment ? ATTACHMENTS : null)
                .hearingDate(HEARING_DATE)
                .build(),
            refusedOrderCount, refusedOrderCount
        );
    }

    static Arguments refuseTargetedPsa(JudgeApproval judgeApproval, boolean withSubmittedByEmail) {
        AgreedDraftOrderCollection agreedDraftOrderCollectionToBeExamined =
            AgreedDraftOrderCollection.builder().value(AgreedDraftOrder.builder()
                    .pensionSharingAnnex(TARGET_DOCUMENT)
                    .orderStatus(OrderStatus.REFUSED)
                    .build())
                .build();
        PsaDocReviewCollection psaDocReviewCollection =
            PsaDocReviewCollection.builder().value(PsaDocumentReview.builder()
                    .submittedDate(SUBMITTED_DATE)
                    .submittedBy(SUBMITTED_BY)
                    .submittedByEmail(withSubmittedByEmail ? SUBMITTED_BY_EMAIL : null)
                    .psaDocument(TARGET_DOCUMENT)
                    .orderStatus(OrderStatus.REFUSED)
                    .refusedDate(FIXED_DATE_TIME)
                    .approvalJudge("Mary Chapman")
                    .build())
                .build();

        return Arguments.of(judgeApproval, null,
            List.of(agreedDraftOrderCollectionToBeExamined),
            List.of(
                DraftOrdersReviewCollection.builder()
                    .value(
                        DraftOrdersReview.builder()
                            .hearingJudge("Peter Chapman")
                            .hearingType("First Directions Appointment (FDA)")
                            .psaDocReviewCollection(List.of(
                                psaDocReviewCollection
                            ))
                            .build()
                    )
                    .build()
            ),
            List.of(),
            agreedDraftOrderCollectionToBeExamined,
            null,
            psaDocReviewCollection,
            RefusedOrder.builder()
                .refusedDate(FIXED_DATE_TIME).refusedDocument(TARGET_DOCUMENT).refusalOrder(GENERATED_REFUSED_ORDER).refusalJudge(APPROVED_JUDGE_NAME)
                .judgeFeedback(JUDGE_FEEDBACK)
                .submittedByEmail(withSubmittedByEmail ? SUBMITTED_BY_EMAIL : null).submittedDate(SUBMITTED_DATE).submittedBy(SUBMITTED_BY)
                .hearingDate(HEARING_DATE)
                .build(),
            1, 1
        );
    }

    static Arguments refuseTargetedPsaWithExistingRefusedOrders(JudgeApproval judgeApproval) {
        AgreedDraftOrderCollection agreedDraftOrderCollectionToBeExamined =
            AgreedDraftOrderCollection.builder().value(AgreedDraftOrder.builder()
                    .pensionSharingAnnex(TARGET_DOCUMENT)
                    .orderStatus(OrderStatus.REFUSED)
                    .build())
                .build();
        PsaDocReviewCollection psaDocReviewCollection =
            PsaDocReviewCollection.builder().value(PsaDocumentReview.builder()
                    .psaDocument(TARGET_DOCUMENT)
                    .submittedDate(SUBMITTED_DATE)
                    .submittedBy(SUBMITTED_BY)
                    .submittedByEmail(SUBMITTED_BY_EMAIL)
                    .orderStatus(OrderStatus.REFUSED)
                    .refusedDate(FIXED_DATE_TIME)
                    .approvalJudge("Mary Chapman")
                    .build())
                .build();

        return Arguments.of(judgeApproval, RefusalOrderInstruction.builder().judgeType(JudgeType.DEPUTY_DISTRICT_JUDGE).build(),
            List.of(agreedDraftOrderCollectionToBeExamined),
            List.of(
                DraftOrdersReviewCollection.builder()
                    .value(
                        DraftOrdersReview.builder()
                            .hearingJudge("Peter Chapman")
                            .hearingType("First Directions Appointment (FDA)")
                            .psaDocReviewCollection(List.of(
                                psaDocReviewCollection
                            ))
                            .build()
                    )
                    .build()
            ),
            List.of(
                RefusedOrderCollection.builder().value(RefusedOrder.builder()
                    .refusalJudge("Another Judge")
                    .judgeFeedback("Here is the feedback")
                    .refusalOrder(CaseDocument.builder().documentUrl("existingRefusedOrder.pdf").build())
                    .refusedDocument(CaseDocument.builder().documentUrl("anotherDraftOrderRefused.pdf").build())
                    .build()).build(),
                RefusedOrderCollection.builder().value(RefusedOrder.builder()
                    .refusalJudge("Another Judge")
                    .judgeFeedback("Here is the another feedback")
                    .refusedDate(FIXED_DATE_TIME.minusDays(10))
                    .refusalOrder(CaseDocument.builder().documentUrl("existingRefusedOrder2.pdf").build())
                    .refusedDocument(CaseDocument.builder().documentUrl("anotherDraftOrderRefused2.pdf").build())
                    .build()).build()
            ),
            agreedDraftOrderCollectionToBeExamined,
            null,
            psaDocReviewCollection,
            RefusedOrder.builder()
                .refusedDate(FIXED_DATE_TIME).refusedDocument(TARGET_DOCUMENT).refusalOrder(GENERATED_REFUSED_ORDER).refusalJudge(APPROVED_JUDGE_NAME)
                .judgeFeedback(JUDGE_FEEDBACK)
                .submittedByEmail(SUBMITTED_BY_EMAIL).submittedDate(SUBMITTED_DATE).submittedBy(SUBMITTED_BY)
                .hearingDate(HEARING_DATE)
                .judgeType(JudgeType.DEPUTY_DISTRICT_JUDGE)
                .build(),
            3, 1
        );
    }

    private boolean isAgreedDraftOrderAbsent(List<AgreedDraftOrderCollection> agreedDraftOrderCollections, AgreedDraftOrderCollection targetObject) {
        return agreedDraftOrderCollections.stream().noneMatch(order -> order.equals(targetObject));
    }

    private boolean isPsaDocumentReviewAbsent(List<DraftOrdersReviewCollection> draftOrdersReviewCollections, PsaDocReviewCollection targetObject) {
        return draftOrdersReviewCollections.stream()
            .map(DraftOrdersReviewCollection::getValue) // Extract DraftOrdersReview
            .filter(draftOrdersReview -> draftOrdersReview.getPsaDocReviewCollection() != null) // Filter non-null collections
            .flatMap(draftOrdersReview -> draftOrdersReview.getPsaDocReviewCollection().stream()) // Flatten to PsaDocReviewCollection
            .noneMatch(order -> order.equals(targetObject));
    }

    private boolean isDraftOrderDocumentReviewAbsent(List<DraftOrdersReviewCollection> draftOrdersReviewCollections,
                                                    DraftOrderDocReviewCollection targetObject) {
        return draftOrdersReviewCollections.stream()
            .map(DraftOrdersReviewCollection::getValue) // Extract DraftOrdersReview
            .filter(draftOrdersReview -> draftOrdersReview.getDraftOrderDocReviewCollection() != null) // Filter non-null collections
            .flatMap(draftOrdersReview -> draftOrdersReview.getDraftOrderDocReviewCollection().stream()) // Flatten to DraftOrderDocReviewCollection
            .noneMatch(order -> order.equals(targetObject));
    }
}
