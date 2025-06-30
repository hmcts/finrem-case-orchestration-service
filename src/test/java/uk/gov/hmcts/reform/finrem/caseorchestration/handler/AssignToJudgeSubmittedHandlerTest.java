package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudge.FinremAssignToJudgeCorresponder;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.REASSIGN_JUDGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.REFER_TO_JUDGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.REFER_TO_JUDGE_FROM_AWAITING_RESPONSE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.REFER_TO_JUDGE_FROM_CLOSE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.REFER_TO_JUDGE_FROM_CONSENT_ORDER_APPROVED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.REFER_TO_JUDGE_FROM_CONSENT_ORDER_MADE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.REFER_TO_JUDGE_FROM_ORDER_MADE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.REFER_TO_JUDGE_FROM_RESPOND_TO_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class AssignToJudgeSubmittedHandlerTest {

    @InjectMocks
    private AssignToJudgeSubmittedHandler handlerUnderTest;

    @Mock
    private FinremAssignToJudgeCorresponder assignToJudgeCorresponder;

    @Test
    void testCanHandle() {
        assertCanHandle(handlerUnderTest,
            Arguments.of(CallbackType.SUBMITTED, CONSENTED, REFER_TO_JUDGE),
            Arguments.of(CallbackType.SUBMITTED, CONSENTED, REFER_TO_JUDGE_FROM_ORDER_MADE),
            Arguments.of(CallbackType.SUBMITTED, CONSENTED, REFER_TO_JUDGE_FROM_CONSENT_ORDER_APPROVED),
            Arguments.of(CallbackType.SUBMITTED, CONSENTED, REFER_TO_JUDGE_FROM_CONSENT_ORDER_MADE),
            Arguments.of(CallbackType.SUBMITTED, CONSENTED, REFER_TO_JUDGE_FROM_AWAITING_RESPONSE),
            Arguments.of(CallbackType.SUBMITTED, CONSENTED, REFER_TO_JUDGE_FROM_RESPOND_TO_ORDER),
            Arguments.of(CallbackType.SUBMITTED, CONSENTED, REFER_TO_JUDGE_FROM_CLOSE),
            Arguments.of(CallbackType.SUBMITTED, CONSENTED, REASSIGN_JUDGE)
        );
    }

    @Test
    void testHandle() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        handlerUnderTest.handle(callbackRequest, AUTH_TOKEN);
        verify(assignToJudgeCorresponder).sendCorrespondence(callbackRequest.getCaseDetails(), AUTH_TOKEN);
    }

    private FinremCallbackRequest buildCallbackRequest() {
        return FinremCallbackRequestFactory.from(FinremCaseData.builder().ccdCaseType(CONSENTED).build());
    }
}
