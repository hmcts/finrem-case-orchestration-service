package uk.gov.hmcts.reform.finrem.caseorchestration.handler.judgeapproval;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.JudgeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.ExtraReportFieldsInput;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.HearingInstruction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeApproval;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ContestedOrderApprovedLetterService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.judgeapproval.ApproveOrderService;

import java.util.stream.Stream;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class ApproveDraftOrdersAboutToSubmitHandlerTest {

    @InjectMocks
    private ApproveDraftOrdersAboutToSubmitHandler handler;

    @Mock
    private ApproveOrderService approveOrderService;

    @Mock
    private ContestedOrderApprovedLetterService contestedOrderApprovedLetterService;

    @Mock
    private IdamService idamService;

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

        when(approveOrderService.populateJudgeDecisions(any(FinremCaseDetails.class), any(DraftOrdersWrapper.class), eq(AUTH_TOKEN)))
            .thenReturn(Pair.of(FALSE, FALSE));
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
                DraftOrdersWrapper::getShowWarningMessageToJudge)
            .containsOnlyNulls();
    }

    static Stream<Arguments> provideInvokeApprovalServicePopulateJudgeDecisionsAndGenerateCoverLetterData() {
        return Stream.of(
            Arguments.of(Pair.of(TRUE, TRUE), true),
            Arguments.of(Pair.of(TRUE, FALSE), true),
            Arguments.of(Pair.of(FALSE, FALSE), false),
            Arguments.of(Pair.of(FALSE, TRUE), false)
        );
    }

    @ParameterizedTest
    @MethodSource("provideInvokeApprovalServicePopulateJudgeDecisionsAndGenerateCoverLetterData")
    void shouldInvokeApprovalServicePopulateJudgeDecisionsAndGenerateCoverLetter(Pair<Boolean, Boolean> statuses, boolean shouldGenerateCoverLetter) {
        DraftOrdersWrapper draftOrdersWrapper = null;
        FinremCaseData caseData = FinremCaseData.builder()
            .draftOrdersWrapper(draftOrdersWrapper = DraftOrdersWrapper.builder()
                .hearingInstruction(HearingInstruction.builder().build())
                .judgeApproval1(JudgeApproval.builder().build())
                .judgeApproval2(JudgeApproval.builder().build())
                .judgeApproval3(JudgeApproval.builder().build())
                .judgeApproval4(JudgeApproval.builder().build())
                .judgeApproval5(JudgeApproval.builder().build())
                .extraReportFieldsInput(ExtraReportFieldsInput.builder().judgeType(JudgeType.DISTRICT_JUDGE).build())
                .build())
            .build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .data(caseData)
            .build();

        when(approveOrderService.populateJudgeDecisions(caseDetails, draftOrdersWrapper, AUTH_TOKEN)).thenReturn(statuses);
        lenient().when(idamService.getIdamFullName(AUTH_TOKEN)).thenReturn("Peter CHAPMAN");

        handler.handle(FinremCallbackRequestFactory.from(caseDetails), AUTH_TOKEN);

        verify(approveOrderService).populateJudgeDecisions(caseDetails, draftOrdersWrapper, AUTH_TOKEN);
        verify(contestedOrderApprovedLetterService, times(shouldGenerateCoverLetter ? 1 : 0))
            .generateAndStoreContestedOrderApprovedLetter(caseDetails, "District Judge Peter CHAPMAN", AUTH_TOKEN);
    }
}
