package uk.gov.hmcts.reform.finrem.caseorchestration.service.judgeapproval;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.contestordernotapproved.ContestedDraftOrderNotApprovedDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Approvable;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.CaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.AnotherHearingRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.AnotherHearingRequestCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.HearingInstruction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeApproval;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeDecision.JUDGE_NEEDS_TO_MAKE_CHANGES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeDecision.LEGAL_REP_NEEDS_TO_MAKE_CHANGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeDecision.READY_TO_BE_SEALED;

@ExtendWith(MockitoExtension.class)
class JudgeApprovalResolverTest {

    private static final CaseDocument TARGET_DOCUMENT = CaseDocument.builder().documentUrl("targetUrl").build();
    private static final CaseDocument GENERATED_REFUSED_ORDER = CaseDocument.builder().documentUrl("generatedRefusedOrder.pdf").build();
    private static final String JUDGE_FEEDBACK = "Refusal reason";
    private static final String APPROVED_JUDGE_NAME = "Mary Chapman";
    private static final LocalDateTime FIXED_DATE_TIME = LocalDateTime.of(2024, 11, 4, 9, 0, 0);
    private static final LocalDate HEARING_DATE = LocalDate.of(2024, 1, 1);
    private static final String SUBMITTEDED_BY = "Claire";
    private static final String SUBMITTED_BY_EMAIL = "claire@solicitor.com";
    private static final LocalDateTime SUBMITTEDED_DATE = LocalDateTime.of(1989, 6, 4, 0, 0);
    private static final List<CaseDocumentCollection> ATTACHMENTS = List.of(
        CaseDocumentCollection.builder().value(CaseDocument.builder().documentUrl("randomDoc.pdf").build()).build()
    );

    @InjectMocks
    private JudgeApprovalResolver judgeApprovalResolver;

    @Mock
    private IdamService idamService;

    @Mock
    private HearingProcessor hearingProcessor;

    @Mock
    private GenericDocumentService genericDocumentService;

    @Mock
    private DocumentConfiguration documentConfiguration;

    @Mock
    private ContestedDraftOrderNotApprovedDetailsMapper contestedDraftOrderNotApprovedDetailsMapper;

    @ParameterizedTest
    @MethodSource("provideShouldInvokeProcessHearingInstructionData")
    void shouldInvokeProcessHearingInstruction(DraftOrdersWrapper draftOrdersWrapper, int expectHearingInvocationCount) {
        // Execute the method being tested
        judgeApprovalResolver.populateJudgeDecision(FinremCaseDetails.builder().build(),
            draftOrdersWrapper,
            CaseDocument.builder().build(),
            JudgeApproval.builder().judgeDecision(READY_TO_BE_SEALED).build(),
            AUTH_TOKEN
        );

        // Verify the expected number of invocations to processHearingInstruction
        verify(hearingProcessor, times(expectHearingInvocationCount))
            .processHearingInstruction(eq(draftOrdersWrapper), any(AnotherHearingRequest.class));
    }

    static Stream<Arguments> provideShouldInvokeProcessHearingInstructionData() {
        return Stream.of(
            // No hearing requests
            Arguments.of(
                DraftOrdersWrapper.builder()
                    .hearingInstruction(HearingInstruction.builder().build())
                    .build(),
                0 // No hearing request means no invocation
            ),
            // Single hearing request
            Arguments.of(
                DraftOrdersWrapper.builder()
                    .hearingInstruction(HearingInstruction.builder()
                        .anotherHearingRequestCollection(List.of(
                            AnotherHearingRequestCollection.builder().value(AnotherHearingRequest.builder().build()).build()
                        ))
                        .build())
                    .build(),
                1 // Expect a single invocation for one hearing request
            ),
            // Multiple hearing requests
            Arguments.of(
                DraftOrdersWrapper.builder()
                    .hearingInstruction(HearingInstruction.builder()
                        .anotherHearingRequestCollection(List.of(
                            AnotherHearingRequestCollection.builder().value(AnotherHearingRequest.builder().build()).build(),
                            AnotherHearingRequestCollection.builder().value(AnotherHearingRequest.builder().build()).build()
                        ))
                        .build())
                    .build(),
                2 // Expect two invocations for two hearing requests
            )
        );
    }

    @ParameterizedTest
    @MethodSource("provideProcessApprovableCollectionDataWithHandleApprovable")
    void shouldProcessApprovablesAndUpdateTheirState(DraftOrdersWrapper draftOrdersWrapper,
                                                     List<? extends Approvable> approvables,
                                                     JudgeApproval judgeApproval,
                                                     boolean shouldBeApproved,
                                                     CaseDocument targetDoc,
                                                     OrderStatus expectedOrderStatus,
                                                     CaseDocument expectedAmendedDocument) {

        // Mocking IDAM service for getting judge's full name
        lenient().when(idamService.getIdamFullName(AUTH_TOKEN)).thenReturn(APPROVED_JUDGE_NAME);

        try (MockedStatic<LocalDateTime> mockedStatic = Mockito.mockStatic(LocalDateTime.class, Mockito.CALLS_REAL_METHODS)) {
            mockedStatic.when(LocalDateTime::now).thenReturn(FIXED_DATE_TIME);
            judgeApprovalResolver.populateJudgeDecision(FinremCaseDetails.builder().build(),
                draftOrdersWrapper, targetDoc, judgeApproval, AUTH_TOKEN);

            if (approvables != null) {
                for (Approvable approvable : approvables) {
                    if (approvable.match(targetDoc)) {
                        if (shouldBeApproved) {
                            assertEquals(expectedOrderStatus, approvable.getOrderStatus());
                            assertEquals(FIXED_DATE_TIME, approvable.getApprovalDate());
                            assertEquals(APPROVED_JUDGE_NAME, approvable.getApprovalJudge());
                            if (expectedAmendedDocument != null) {
                                assertEquals(expectedAmendedDocument, approvable.getReplacedDocument());
                            }
                        } else {
                            assertNull(approvable.getOrderStatus());
                            assertNull(approvable.getApprovalDate());
                            assertNull(approvable.getApprovalJudge());
                        }
                    }
                }
            }
        }
    }

    static Stream<Arguments> provideProcessApprovableCollectionDataWithHandleApprovable() {

        // Mock approvable objects
        CaseDocument draftOrderDocument = CaseDocument.builder().documentUrl("NEW_DOC1.doc").build();
        CaseDocument psaDocument = CaseDocument.builder().documentUrl("NEW_DOC2.doc").build();
        CaseDocument amendedDocument = CaseDocument.builder().documentUrl("AMENDED_DOC.doc").build();

        DraftOrderDocumentReview draftReview = DraftOrderDocumentReview.builder()
            .draftOrderDocument(draftOrderDocument)
            .build();
        PsaDocumentReview psaReview = PsaDocumentReview.builder()
            .psaDocument(psaDocument)
            .build();

        List<AgreedDraftOrder> agreedDrafts = List.of(AgreedDraftOrder.builder().build());

        JudgeApproval approvedJudgeApproval = JudgeApproval.builder()
            .judgeDecision(READY_TO_BE_SEALED)
            .build();

        JudgeApproval approvedJudgeApprovalWithChanges = JudgeApproval.builder()
            .judgeDecision(JUDGE_NEEDS_TO_MAKE_CHANGES)
            .amendedDocument(amendedDocument)
            .build();

        JudgeApproval notApprovedJudgeApproval = JudgeApproval.builder().judgeDecision(null).build();

        return Stream.of(
            Arguments.of(
                DraftOrdersWrapper.builder()
                    .draftOrdersReviewCollection(List.of(
                        DraftOrdersReviewCollection.builder().value(DraftOrdersReview.builder()
                            .draftOrderDocReviewCollection(List.of(DraftOrderDocReviewCollection.builder()
                                .value(draftReview).build()))
                            .build()).build()))
                    .build(),
                List.of(draftReview),
                approvedJudgeApprovalWithChanges,
                true, // should be approved
                draftOrderDocument,
                OrderStatus.APPROVED_BY_JUDGE,
                amendedDocument
            ),
            Arguments.of(
                DraftOrdersWrapper.builder()
                    .draftOrdersReviewCollection(List.of(
                        DraftOrdersReviewCollection.builder().value(DraftOrdersReview.builder()
                            .psaDocReviewCollection(List.of(PsaDocReviewCollection.builder()
                                .value(psaReview).build()))
                            .build()).build()))
                    .build(),
                List.of(psaReview),
                approvedJudgeApproval,
                true, // should be approved
                psaDocument,
                OrderStatus.APPROVED_BY_JUDGE,
                null
            ),
            Arguments.of(
                DraftOrdersWrapper.builder()
                    .agreedDraftOrderCollection(List.of(
                        AgreedDraftOrderCollection.builder().value(agreedDrafts.get(0)).build()))
                    .build(),
                agreedDrafts,
                notApprovedJudgeApproval,
                false, // should not be approved
                CaseDocument.builder().build(),
                null,
                null
            ),
            Arguments.of(DraftOrdersWrapper.builder().build(), null, approvedJudgeApproval, false, null, null, null)
        );
    }

    @ParameterizedTest
    @MethodSource("provideProcessRefusedDocumentsAndUpdateTheirState")
    void shouldProcessRefusedDocumentsAndUpdateTheirState(JudgeApproval judgeApproval,
                                                          List<AgreedDraftOrderCollection> agreedDraftOrderCollections,
                                                          List<DraftOrdersReviewCollection> draftOrdersReviewCollection,
                                                          List<RefusedOrderCollection> existingRefusedOrders,
                                                          AgreedDraftOrderCollection targetAgreedDraftOrderCollection,
                                                          DraftOrderDocReviewCollection targetDraftOrderDocReviewCollection,
                                                          PsaDocReviewCollection targetPsaDocReviewCollection,
                                                          RefusedOrder expectedRefusedOrder) {
        DraftOrdersWrapper draftOrdersWrapper = DraftOrdersWrapper.builder()
            .agreedDraftOrderCollection(agreedDraftOrderCollections)
            .draftOrdersReviewCollection(draftOrdersReviewCollection)
            .refusedOrdersCollection(existingRefusedOrders)
            .build();

        // Mocking IDAM service for getting judge's full name
        lenient().when(idamService.getIdamFullName(AUTH_TOKEN)).thenReturn(APPROVED_JUDGE_NAME);
        lenient().when(documentConfiguration.getContestedDraftOrderNotApprovedFileName()).thenReturn("RefusalOrder.doc");
        lenient().when(documentConfiguration.getContestedDraftOrderNotApprovedTemplate(any(FinremCaseDetails.class))).thenReturn("TemplateName");
        lenient().when(genericDocumentService.generateDocumentFromPlaceholdersMap(anyString(), anyMap(), anyString(), anyString(), anyString()))
            .thenReturn(GENERATED_REFUSED_ORDER);

        try (MockedStatic<LocalDateTime> mockedStatic = Mockito.mockStatic(LocalDateTime.class, Mockito.CALLS_REAL_METHODS)) {
            mockedStatic.when(LocalDateTime::now).thenReturn(FIXED_DATE_TIME);
            judgeApprovalResolver.populateJudgeDecision(FinremCaseDetails.builder()
                    .id(Long.valueOf(CASE_ID))
                    .data(FinremCaseData.builder()
                        .draftOrdersWrapper(draftOrdersWrapper)
                        .build())
                    .build(),
                draftOrdersWrapper, judgeApproval.getDocument(), judgeApproval, AUTH_TOKEN);

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

            assertThat(draftOrdersWrapper.getRefusedOrdersCollection()).hasSize(existingRefusedOrders.size() + result);

            if (result != 0) {
                assertThat(draftOrdersWrapper.getRefusalOrderIdsToBeSent()).hasSize(1);
                // verify agreedDraftOrderToBeExamined is removed
                assertTrue(isAgreedDraftOrderAbsent(draftOrdersWrapper.getAgreedDraftOrderCollection(), targetAgreedDraftOrderCollection));

                List<DraftOrdersReviewCollection> newDraftOrdersReviewCollections = draftOrdersWrapper.getDraftOrdersReviewCollection();
                if (targetDraftOrderDocReviewCollection != null) {
                    assertTrue(isDraftOrderDocumentReviewAbsent(newDraftOrdersReviewCollections, targetDraftOrderDocReviewCollection));
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
            refuseTargetedDraftOrder(judgeApproval, true),
            refuseTargetedDraftOrder(judgeApproval, false),
            refuseTargetedPsa(judgeApproval, true),
            refuseTargetedPsa(judgeApproval, false),
            refuseTargetedPsaWithExistingRefusedOrders(judgeApproval)
        );
    }

    static Arguments targetDocNotFound(JudgeApproval judgeApproval) {
        return Arguments.of(judgeApproval,
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
            null
        );
    }

    static Arguments refuseTargetedDraftOrder(JudgeApproval judgeApproval, boolean withAttachment) {
        AgreedDraftOrderCollection agreedDraftOrderCollectionToBeExamined =
            AgreedDraftOrderCollection.builder().value(AgreedDraftOrder.builder().draftOrder(TARGET_DOCUMENT).build()).build();

        DraftOrderDocReviewCollection draftOrderDocReviewCollectionToBeExamined =
            DraftOrderDocReviewCollection.builder().value(DraftOrderDocumentReview.builder()
                .draftOrderDocument(TARGET_DOCUMENT)
                .submittedBy(SUBMITTEDED_BY)
                .submittedByEmail(SUBMITTED_BY_EMAIL)
                .submittedDate(SUBMITTEDED_DATE)
                .attachments(withAttachment ? ATTACHMENTS : null)
                .build()).build();

        return Arguments.of(judgeApproval,
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
                            .draftOrderDocReviewCollection(List.of(
                                draftOrderDocReviewCollectionToBeExamined
                            ))
                            .build()
                    )
                    .build()
            ),
            List.of(),
            agreedDraftOrderCollectionToBeExamined,
            draftOrderDocReviewCollectionToBeExamined,
            null,
            RefusedOrder.builder()
                .refusedDate(FIXED_DATE_TIME)
                .refusedDocument(TARGET_DOCUMENT)
                .refusalOrder(GENERATED_REFUSED_ORDER)
                .judgeFeedback(JUDGE_FEEDBACK)
                .refusalJudge(APPROVED_JUDGE_NAME)
                .submittedByEmail(SUBMITTED_BY_EMAIL)
                .submittedDate(SUBMITTEDED_DATE)
                .submittedBy(SUBMITTEDED_BY)
                .attachments(withAttachment ? ATTACHMENTS : null)
                .hearingDate(HEARING_DATE)
                .build()
        );
    }

    static Arguments refuseTargetedPsa(JudgeApproval judgeApproval, boolean withSubmittedByEmail) {
        AgreedDraftOrderCollection agreedDraftOrderCollectionToBeExamined =
            AgreedDraftOrderCollection.builder().value(AgreedDraftOrder.builder().pensionSharingAnnex(TARGET_DOCUMENT).build()).build();
        PsaDocReviewCollection psaDocReviewCollection =
            PsaDocReviewCollection.builder().value(PsaDocumentReview.builder()
                    .submittedDate(SUBMITTEDED_DATE)
                    .submittedBy(SUBMITTEDED_BY)
                    .submittedByEmail(withSubmittedByEmail ? SUBMITTED_BY_EMAIL : null)
                    .psaDocument(TARGET_DOCUMENT)
                    .build())
                .build();

        return Arguments.of(judgeApproval,
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
                .submittedByEmail(withSubmittedByEmail ? SUBMITTED_BY_EMAIL : null).submittedDate(SUBMITTEDED_DATE).submittedBy(SUBMITTEDED_BY)
                .hearingDate(HEARING_DATE)
                .build()
        );
    }

    static Arguments refuseTargetedPsaWithExistingRefusedOrders(JudgeApproval judgeApproval) {
        AgreedDraftOrderCollection agreedDraftOrderCollectionToBeExamined =
            AgreedDraftOrderCollection.builder().value(AgreedDraftOrder.builder().pensionSharingAnnex(TARGET_DOCUMENT).build()).build();
        PsaDocReviewCollection psaDocReviewCollection =
            PsaDocReviewCollection.builder().value(PsaDocumentReview.builder()
                    .psaDocument(TARGET_DOCUMENT)
                    .submittedDate(SUBMITTEDED_DATE)
                    .submittedBy(SUBMITTEDED_BY)
                    .submittedByEmail(SUBMITTED_BY_EMAIL)
                    .build())
                .build();

        return Arguments.of(judgeApproval,
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
                .submittedByEmail(SUBMITTED_BY_EMAIL).submittedDate(SUBMITTEDED_DATE).submittedBy(SUBMITTEDED_BY)
                .hearingDate(HEARING_DATE)
                .build()
        );
    }

    public boolean isAgreedDraftOrderAbsent(List<AgreedDraftOrderCollection> agreedDraftOrderCollections, AgreedDraftOrderCollection targetObject) {
        return agreedDraftOrderCollections.stream().noneMatch(order -> order.equals(targetObject));
    }

    public boolean isPsaDocumentReviewAbsent(List<DraftOrdersReviewCollection> draftOrdersReviewCollections, PsaDocReviewCollection targetObject) {
        return draftOrdersReviewCollections.stream()
            .map(DraftOrdersReviewCollection::getValue) // Extract DraftOrdersReview
            .filter(draftOrdersReview -> draftOrdersReview.getPsaDocReviewCollection() != null) // Filter non-null collections
            .flatMap(draftOrdersReview -> draftOrdersReview.getPsaDocReviewCollection().stream()) // Flatten to PsaDocReviewCollection
            .noneMatch(order -> order.equals(targetObject));
    }

    public boolean isDraftOrderDocumentReviewAbsent(List<DraftOrdersReviewCollection> draftOrdersReviewCollections,
                                                    DraftOrderDocReviewCollection targetObject) {
        return draftOrdersReviewCollections.stream()
            .map(DraftOrdersReviewCollection::getValue) // Extract DraftOrdersReview
            .filter(draftOrdersReview -> draftOrdersReview.getDraftOrderDocReviewCollection() != null) // Filter non-null collections
            .flatMap(draftOrdersReview -> draftOrdersReview.getDraftOrderDocReviewCollection().stream()) // Flatten to DraftOrderDocReviewCollection
            .noneMatch(order -> order.equals(targetObject));
    }
}

