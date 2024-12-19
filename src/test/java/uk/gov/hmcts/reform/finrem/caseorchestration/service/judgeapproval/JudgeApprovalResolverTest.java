package uk.gov.hmcts.reform.finrem.caseorchestration.service.judgeapproval;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.Approvable;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.AnotherHearingRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.AnotherHearingRequestCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.HearingInstruction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeApproval;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeDecision;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocumentReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.PsaDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.PsaDocumentReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo.NO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeDecision.JUDGE_NEEDS_TO_MAKE_CHANGES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeDecision.READY_TO_BE_SEALED;

@ExtendWith(MockitoExtension.class)
class JudgeApprovalResolverTest {

    private static final String APPROVED_JUDGE_NAME = "Mary Chapman";
    private static final LocalDateTime FIXED_DATE_TIME = LocalDateTime.of(2024, 11, 4, 9, 0, 0);
    private static final CaseDocument TARGET_DOCUMENT = CaseDocument.builder().documentUrl("targetUrl").build();

    @InjectMocks
    private JudgeApprovalResolver judgeApprovalResolver;

    @Mock
    private IdamService idamService;

    @Mock
    private HearingProcessor hearingProcessor;

    @Mock
    private RefusedOrderProcessor refusedOrderProcessor;

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
                                                     CaseDocument expectedAmendedDocument,
                                                     YesOrNo expectedFinalOrder) {

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
                            assertEquals(expectedFinalOrder, approvable.getFinalOrder());
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

    static Arguments checkJudgeNeedsToMakeChanges(DraftOrderDocumentReview draftReview,
                                                  Boolean isFinalOrder) {
        CaseDocument draftOrderDocument = CaseDocument.builder().documentUrl("NEW_DOC1.doc").build();
        CaseDocument amendedDocument = CaseDocument.builder().documentUrl("AMENDED_DOC.doc").build();

        JudgeApproval.JudgeApprovalBuilder judgeApprovalBuilder = JudgeApproval.builder()
            .judgeDecision(JUDGE_NEEDS_TO_MAKE_CHANGES)
            .amendedDocument(amendedDocument);

        JudgeApproval judgeApproval = null;
        if (isFinalOrder == null) {
            judgeApproval = judgeApprovalBuilder.build();
        } else {
            judgeApproval = judgeApprovalBuilder
                .isFinalOrder(DynamicMultiSelectList.builder().value(List.of(
                    DynamicMultiSelectListElement.builder()
                        .code(YesOrNo.getYesOrNo(isFinalOrder))
                        .build())
                ).build())
                .build();
        }
        return Arguments.of(
            DraftOrdersWrapper.builder()
                .draftOrdersReviewCollection(List.of(
                    DraftOrdersReviewCollection.builder()
                        .value(DraftOrdersReview.builder()
                            .draftOrderDocReviewCollection(List.of(DraftOrderDocReviewCollection.builder().value(draftReview).build()))
                            .build())
                        .build()))
                .build(),
            List.of(draftReview),
            judgeApproval,
            true, // should be approved
            draftOrderDocument,
            OrderStatus.APPROVED_BY_JUDGE,
            amendedDocument,
            YesOrNo.forValue(isFinalOrder)
        );
    }

    static Stream<Arguments> provideProcessApprovableCollectionDataWithHandleApprovable() {
        // Mock approvable objects
        CaseDocument draftOrderDocument = CaseDocument.builder().documentUrl("NEW_DOC1.doc").build();
        CaseDocument psaDocument = CaseDocument.builder().documentUrl("NEW_DOC2.doc").build();

        DraftOrderDocumentReview draftReview = DraftOrderDocumentReview.builder()
            .draftOrderDocument(draftOrderDocument)
            .build();
        PsaDocumentReview psaReview = PsaDocumentReview.builder()
            .psaDocument(psaDocument)
            .build();

        List<AgreedDraftOrder> agreedDrafts = List.of(AgreedDraftOrder.builder().build());

        JudgeApproval.JudgeApprovalBuilder approvedJudgeApprovalBuilder = JudgeApproval.builder()
            .judgeDecision(READY_TO_BE_SEALED);

        JudgeApproval notApprovedJudgeApproval = JudgeApproval.builder().judgeDecision(null).build();

        return Stream.of(
            //
            checkJudgeNeedsToMakeChanges(draftReview, TRUE),
            checkJudgeNeedsToMakeChanges(draftReview, FALSE),
            checkJudgeNeedsToMakeChanges(draftReview, null),
            //
            Arguments.of(
                DraftOrdersWrapper.builder()
                    .draftOrdersReviewCollection(List.of(
                        DraftOrdersReviewCollection.builder().value(DraftOrdersReview.builder()
                            .psaDocReviewCollection(List.of(PsaDocReviewCollection.builder()
                                .value(psaReview).build()))
                            .build()).build()))
                    .build(),
                List.of(psaReview),
                approvedJudgeApprovalBuilder.build(),
                true, // should be approved
                psaDocument,
                OrderStatus.APPROVED_BY_JUDGE,
                null,
                NO
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
                null,
                null
            ),
            Arguments.of(DraftOrdersWrapper.builder().build(), null, approvedJudgeApprovalBuilder.build(), false, null, null, null, null)
        );
    }

    @Test
    void shouldProcessRefusedApprovablesAndUpdateTheirState() {
        AgreedDraftOrder sample1 = null;
        DraftOrderDocumentReview sample2 = null;
        DraftOrdersWrapper.DraftOrdersWrapperBuilder dowBuilder = DraftOrdersWrapper.builder();
        dowBuilder.agreedDraftOrderCollection(List.of(
            AgreedDraftOrderCollection.builder().value(sample1 = AgreedDraftOrder.builder().draftOrder(TARGET_DOCUMENT).build()).build()
        ));
        dowBuilder.draftOrdersReviewCollection(List.of(
            DraftOrdersReviewCollection.builder()
                .value(DraftOrdersReview.builder()
                    .draftOrderDocReviewCollection(List.of(
                        DraftOrderDocReviewCollection.builder()
                            .value(sample2 = DraftOrderDocumentReview.builder().draftOrderDocument(TARGET_DOCUMENT).build())
                            .build()
                    ))
                    .build())
                .build()
        ));

        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder().build();
        DraftOrdersWrapper draftOrdersWrapper = null;
        JudgeApproval ja = null;

        // Mocking IDAM service for getting judge's full name
        lenient().when(idamService.getIdamFullName(AUTH_TOKEN)).thenReturn(APPROVED_JUDGE_NAME);

        try (MockedStatic<LocalDateTime> mockedStatic = Mockito.mockStatic(LocalDateTime.class, Mockito.CALLS_REAL_METHODS)) {
            mockedStatic.when(LocalDateTime::now).thenReturn(FIXED_DATE_TIME);
            judgeApprovalResolver.populateJudgeDecision(finremCaseDetails,
                draftOrdersWrapper = dowBuilder.build(), TARGET_DOCUMENT, ja = JudgeApproval.builder()
                    .judgeDecision(JudgeDecision.LEGAL_REP_NEEDS_TO_MAKE_CHANGE)
                    .document(TARGET_DOCUMENT)
                    .changesRequestedByJudge("FEEDBACK")
                    .build(), AUTH_TOKEN);

            assertEquals(OrderStatus.REFUSED, sample1.getOrderStatus());
            assertEquals(OrderStatus.REFUSED, sample2.getOrderStatus());
            assertEquals(FIXED_DATE_TIME, sample2.getRefusedDate());
            assertNull(sample1.getApprovalDate());
            assertNull(sample2.getApprovalDate());
            assertNull(sample1.getApprovalJudge()); // AgreedDraftOrder doesn't store approvalJudge
            assertEquals(APPROVED_JUDGE_NAME, sample2.getApprovalJudge());
            verify(refusedOrderProcessor).processRefusedOrders(finremCaseDetails, draftOrdersWrapper, ja, AUTH_TOKEN);
            verify(hearingProcessor, never()).processHearingInstruction(eq(draftOrdersWrapper), any(AnotherHearingRequest.class));
        }
    }
}
