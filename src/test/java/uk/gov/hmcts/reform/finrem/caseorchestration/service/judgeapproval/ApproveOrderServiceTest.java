package uk.gov.hmcts.reform.finrem.caseorchestration.service.judgeapproval;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeApproval;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;

import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeDecision.READY_TO_BE_SEALED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeDecision.REVIEW_LATER;

@ExtendWith(MockitoExtension.class)
class ApproveOrderServiceTest {

    private static final CaseDocument TARGET_DOC = mock(CaseDocument.class);

    @InjectMocks
    private ApproveOrderService underTest;

    @Mock
    private JudgeApprovalResolver judgeApprovalResolver;

    @Mock
    private JudgeApprovalInfoCapturer judgeApprovalInfoCapturer;

    @ParameterizedTest
    @MethodSource("providePopulateJudgeDecisionsData")
    void testPopulateJudgeDecisions(DraftOrdersWrapper draftOrdersWrapper, int expectedPopulateJudgeDecisionInvoked) {

        underTest.populateJudgeDecisions(FinremCaseDetails.builder().build(), draftOrdersWrapper, AUTH_TOKEN);

        verify(judgeApprovalResolver, times(expectedPopulateJudgeDecisionInvoked))
            .populateJudgeDecision(any(FinremCaseDetails.class), eq(draftOrdersWrapper), any(CaseDocument.class), any(JudgeApproval.class),
                eq(AUTH_TOKEN));

        verify(judgeApprovalInfoCapturer).buildConfirmationBody(any(FinremCaseDetails.class), eq(draftOrdersWrapper));

    }

    static Stream<Arguments> providePopulateJudgeDecisionsData() {

        return Stream.of(
            // All judge approvals are valid
            Arguments.of(
                DraftOrdersWrapper.builder()
                    .judgeApproval1(JudgeApproval.builder().document(TARGET_DOC).judgeDecision(READY_TO_BE_SEALED).build())
                    .judgeApproval2(JudgeApproval.builder().document(TARGET_DOC).judgeDecision(READY_TO_BE_SEALED).build())
                    .judgeApproval3(JudgeApproval.builder().document(TARGET_DOC).judgeDecision(READY_TO_BE_SEALED).build())
                    .judgeApproval4(JudgeApproval.builder().document(TARGET_DOC).judgeDecision(READY_TO_BE_SEALED).build())
                    .judgeApproval5(JudgeApproval.builder().document(TARGET_DOC).judgeDecision(READY_TO_BE_SEALED).build())
                    .build(),
                5
            ),
            // Some approvals are valid
            Arguments.of(
                DraftOrdersWrapper.builder()
                    .judgeApproval1(JudgeApproval.builder().document(TARGET_DOC).judgeDecision(READY_TO_BE_SEALED).build())
                    .judgeApproval2(JudgeApproval.builder().document(TARGET_DOC).judgeDecision(REVIEW_LATER).build())
                    .judgeApproval4(JudgeApproval.builder().document(TARGET_DOC).judgeDecision(READY_TO_BE_SEALED).build())
                    .build(),
                3
            ),
            // Approvals exist but have no valid decision
            Arguments.of(
                DraftOrdersWrapper.builder()
                    .judgeApproval1(JudgeApproval.builder().document(TARGET_DOC).judgeDecision(REVIEW_LATER).build())
                    .judgeApproval5(JudgeApproval.builder().document(TARGET_DOC).judgeDecision(REVIEW_LATER).build())
                    .build(),
                2
            ),
            // No judge approvals exist
            Arguments.of(
                DraftOrdersWrapper.builder().build(),
                0
            )
        );
    }
}
