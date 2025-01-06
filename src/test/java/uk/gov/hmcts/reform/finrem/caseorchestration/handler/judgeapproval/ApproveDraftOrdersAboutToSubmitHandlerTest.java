package uk.gov.hmcts.reform.finrem.caseorchestration.handler.judgeapproval;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.HearingInstruction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeApproval;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.judgeapproval.ApproveOrderService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class ApproveDraftOrdersAboutToSubmitHandlerTest {

    @InjectMocks
    private ApproveDraftOrdersAboutToSubmitHandler handler;

    @Mock
    private ApproveOrderService approveOrderService;

    @Test
    void canHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.APPROVE_ORDERS);
    }

    @Test
    void shouldClearObjectsWhichAreForCapturingInputPurpose() {
        FinremCaseData caseData = FinremCaseData.builder()
            .draftOrdersWrapper(DraftOrdersWrapper.builder()
                .hearingInstruction(HearingInstruction.builder().build())
                .judgeApproval1(JudgeApproval.builder().build())
                .judgeApproval2(JudgeApproval.builder().build())
                .judgeApproval3(JudgeApproval.builder().build())
                .judgeApproval4(JudgeApproval.builder().build())
                .judgeApproval5(JudgeApproval.builder().build())
                .build())
            .build();

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(
            FinremCallbackRequestFactory.from(1727874196328932L, caseData), AUTH_TOKEN);

        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getDraftOrdersWrapper()).isNotNull();
        assertThat(response.getData().getDraftOrdersWrapper().getHearingInstruction()).isNull();
        assertThat(response.getData().getDraftOrdersWrapper().getJudgeApproval1()).isNull();
        assertThat(response.getData().getDraftOrdersWrapper().getJudgeApproval2()).isNull();
        assertThat(response.getData().getDraftOrdersWrapper().getJudgeApproval3()).isNull();
        assertThat(response.getData().getDraftOrdersWrapper().getJudgeApproval4()).isNull();
        assertThat(response.getData().getDraftOrdersWrapper().getJudgeApproval5()).isNull();
        assertThat(response.getData().getDraftOrdersWrapper().getShowWarningMessageToJudge()).isNull();
    }

    @Test
    void shouldInvokeApprovalServicePopulateJudgeDecisions() {
        DraftOrdersWrapper draftOrdersWrapper = null;
        FinremCaseData caseData = FinremCaseData.builder()
            .draftOrdersWrapper(draftOrdersWrapper = DraftOrdersWrapper.builder()
                .hearingInstruction(HearingInstruction.builder().build())
                .judgeApproval1(JudgeApproval.builder().build())
                .judgeApproval2(JudgeApproval.builder().build())
                .judgeApproval3(JudgeApproval.builder().build())
                .judgeApproval4(JudgeApproval.builder().build())
                .judgeApproval5(JudgeApproval.builder().build())
                .build())
            .build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .data(caseData)
            .build();

        handler.handle(FinremCallbackRequestFactory.from(caseDetails), AUTH_TOKEN);

        verify(approveOrderService).populateJudgeDecisions(caseDetails, draftOrdersWrapper, AUTH_TOKEN);
    }
}
