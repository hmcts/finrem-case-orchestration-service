package uk.gov.hmcts.reform.finrem.caseorchestration.service.judgeapproval;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Approvable;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeDecision.READY_TO_BE_SEALED;

@ExtendWith(MockitoExtension.class)
class JudgeApprovalResolverTest {


    @Spy
    @InjectMocks
    private JudgeApprovalResolver judgeApprovalResolver;

    @Mock
    private IdamService idamService;

    @Mock
    private HearingProcessor hearingProcessor;

    @ParameterizedTest
    @MethodSource("provideShouldInvokeProcessHearingInstructionData")
    void shouldInvokeProcessHearingInstruction(DraftOrdersWrapper draftOrdersWrapper, int expectHearingInvocationCount) {
        // Execute the method being tested
        judgeApprovalResolver.populateJudgeDecision(
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
    void shouldInvokeHandleApprovable(DraftOrdersWrapper draftOrdersWrapper,
                                      List<? extends Approvable> approvables,
                                      JudgeApproval judgeApproval,
                                      boolean shouldCallHandleApprovable,
                                      CaseDocument targetDoc
                                      ) {

        judgeApprovalResolver.populateJudgeDecision(draftOrdersWrapper, targetDoc, judgeApproval, "auth");

        if (approvables != null) {
            verify(judgeApprovalResolver, times(1))
                .processApprovableCollection(approvables, targetDoc, judgeApproval, "auth");

            approvables.forEach(approvable -> {
                if (shouldCallHandleApprovable) {
                    verify(judgeApprovalResolver, times(1))
                        .handleApprovable(approvable, judgeApproval, "auth");
                    assertEquals(OrderStatus.APPROVED_BY_JUDGE, approvable.getOrderStatus());
                } else {
                    verify(judgeApprovalResolver, never())
                        .handleApprovable(any(), any(), any());
                }
            });
        } else {
            verify(judgeApprovalResolver, never())
                .processApprovableCollection(any(), any(), any(), any());
        }
    }

    static Stream<Arguments> provideProcessApprovableCollectionDataWithHandleApprovable() {
        //Mock approvable objects to ensure they match the target document
        CaseDocument draftOrderDocument = CaseDocument.builder().documentUrl("NEW_DOC1.doc").build();
        CaseDocument psaDocument = CaseDocument.builder().documentUrl("NEW_DOC2.doc").build();

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

        JudgeApproval notApprovedJudgeApproval = mock(JudgeApproval.class);
        when(notApprovedJudgeApproval.getJudgeDecision()).thenReturn(null);

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
                approvedJudgeApproval,
                true, // should call handleApprovable
                draftOrderDocument
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
                true, // should call handleApprovable
                psaDocument
            ),
            Arguments.of(
                DraftOrdersWrapper.builder()
                    .agreedDraftOrderCollection(List.of(
                        AgreedDraftOrderCollection.builder().value(agreedDrafts.get(0)).build()))
                    .build(),
                agreedDrafts,
                notApprovedJudgeApproval,
                false, // should not call handleApprovable
                CaseDocument.builder().build()
            ),
            Arguments.of(DraftOrdersWrapper.builder().build(), null, approvedJudgeApproval, false, CaseDocument.builder().build())
        );
    }
}
