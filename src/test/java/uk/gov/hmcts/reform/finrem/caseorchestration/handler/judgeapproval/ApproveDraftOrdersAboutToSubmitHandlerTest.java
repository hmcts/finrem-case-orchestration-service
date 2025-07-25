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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.DraftOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.judgeapproval.ApproveOrderService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class ApproveDraftOrdersAboutToSubmitHandlerTest {

    @InjectMocks
    private ApproveDraftOrdersAboutToSubmitHandler handler;

    @Mock
    private ApproveOrderService approveOrderService;

    @Mock
    private DraftOrderService draftOrderService;

    @Test
    void canHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.APPROVE_ORDERS);
    }

    @Test
    void givenValidDate_whenHandle_shouldClearObjects() {
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
        assertThat(response.getData().getDraftOrdersWrapper())
            .extracting(
                DraftOrdersWrapper::getHearingInstruction,
                DraftOrdersWrapper::getJudgeApproval1,
                DraftOrdersWrapper::getJudgeApproval2,
                DraftOrdersWrapper::getJudgeApproval3,
                DraftOrdersWrapper::getJudgeApproval4,
                DraftOrdersWrapper::getJudgeApproval5,
                DraftOrdersWrapper::getShowWarningMessageToJudge,
                DraftOrdersWrapper::getExtraReportFieldsInput)
            .containsOnlyNulls();

        DraftOrdersWrapper draftOrdersWrapper = response.getData().getDraftOrdersWrapper();
        verify(approveOrderService).populateJudgeDecisions(any(FinremCaseDetails.class), eq(draftOrdersWrapper), eq(AUTH_TOKEN));
        verify(draftOrderService).clearEmptyOrdersInDraftOrdersReviewCollection(caseData);
    }
}
