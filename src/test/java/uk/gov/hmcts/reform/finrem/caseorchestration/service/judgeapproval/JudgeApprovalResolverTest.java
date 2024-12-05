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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.PsaDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.PsaDocumentReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeDecision.READY_TO_BE_SEALED;

@ExtendWith(MockitoExtension.class)
@ExtendWith(MockitoExtension.class)
class JudgeApprovalResolverTest {

    private static final String AUTH_TOKEN = "auth-token";

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
    @MethodSource("provideProcessApprovableCollectionData")
    void shouldInvokeProcessApprovableCollection(DraftOrdersWrapper draftOrdersWrapper,
                                                 List<? extends Approvable> approvables) {
        CaseDocument targetDoc = CaseDocument.builder().build();
        JudgeApproval judgeApproval = mock(JudgeApproval.class);
        judgeApprovalResolver.populateJudgeDecision(draftOrdersWrapper, targetDoc, judgeApproval, "auth");

        if (approvables != null) {
            verify(judgeApprovalResolver, times(1))
                .processApprovableCollection(approvables, targetDoc, judgeApproval, "auth");
        } else {
            verify(judgeApprovalResolver, never()).processApprovableCollection(any(), any(), any(), any());
        }
    }

    static Stream<Arguments> provideProcessApprovableCollectionData() {
        List<DraftOrderDocumentReview> draftReviews = List.of(DraftOrderDocumentReview.builder().build());
        List<PsaDocumentReview> psaReviews = List.of(PsaDocumentReview.builder().build());
        List<AgreedDraftOrder> agreedDrafts = List.of(AgreedDraftOrder.builder().build());

        return Stream.of(
            Arguments.of(
                DraftOrdersWrapper.builder()
                    .draftOrdersReviewCollection(List.of(
                        DraftOrdersReviewCollection.builder().value(DraftOrdersReview.builder()
                            .draftOrderDocReviewCollection(List.of(DraftOrderDocReviewCollection.builder()
                                .value(draftReviews.get(0)).build()))
                            .build()).build()))
                    .build(),
                draftReviews
            ),
            Arguments.of(
                DraftOrdersWrapper.builder()
                    .draftOrdersReviewCollection(List.of(
                        DraftOrdersReviewCollection.builder().value(DraftOrdersReview.builder()
                            .psaDocReviewCollection(List.of(PsaDocReviewCollection.builder()
                                .value(psaReviews.get(0)).build()))
                            .build()).build()))
                    .build(),
                psaReviews
            ),
            Arguments.of(
                DraftOrdersWrapper.builder()
                    .agreedDraftOrderCollection(List.of(
                        AgreedDraftOrderCollection.builder().value(agreedDrafts.get(0)).build()))
                    .build(),
                agreedDrafts
            ),
            Arguments.of(DraftOrdersWrapper.builder().build(), null)
        );
    }
}
