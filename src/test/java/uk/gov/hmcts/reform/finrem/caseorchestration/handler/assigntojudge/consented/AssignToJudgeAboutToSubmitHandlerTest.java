package uk.gov.hmcts.reform.finrem.caseorchestration.handler.assigntojudge.consented;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudge.consented.AssignToJudgeCorresponder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID_IN_LONG;
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
class AssignToJudgeAboutToSubmitHandlerTest {

    @Mock
    private AssignToJudgeCorresponder assignToJudgeCorresponder;

    @InjectMocks
    private AssignToJudgeAboutToSubmitHandler underTest;

    @Test
    void testCanHandle() {
        assertCanHandle(underTest,
            Arguments.of(CallbackType.ABOUT_TO_SUBMIT, CONSENTED, REFER_TO_JUDGE),
            Arguments.of(CallbackType.ABOUT_TO_SUBMIT, CONSENTED, REFER_TO_JUDGE_FROM_ORDER_MADE),
            Arguments.of(CallbackType.ABOUT_TO_SUBMIT, CONSENTED, REFER_TO_JUDGE_FROM_CONSENT_ORDER_APPROVED),
            Arguments.of(CallbackType.ABOUT_TO_SUBMIT, CONSENTED, REFER_TO_JUDGE_FROM_CONSENT_ORDER_MADE),
            Arguments.of(CallbackType.ABOUT_TO_SUBMIT, CONSENTED, REFER_TO_JUDGE_FROM_AWAITING_RESPONSE),
            Arguments.of(CallbackType.ABOUT_TO_SUBMIT, CONSENTED, REFER_TO_JUDGE_FROM_RESPOND_TO_ORDER),
            Arguments.of(CallbackType.ABOUT_TO_SUBMIT, CONSENTED, REFER_TO_JUDGE_FROM_CLOSE)
        );
    }

    @Test
    void shouldCreateAuditsForCorrespondence_whenHandled() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, REFER_TO_JUDGE);

        var response = underTest.handle(callbackRequest, AUTH_TOKEN);

        assertAll(
            () -> assertThat(response.getData()).isEqualTo(callbackRequest.getCaseDetails().getData()),
            () -> verify(assignToJudgeCorresponder).createAuditsForCorrespondence(REFER_TO_JUDGE, callbackRequest.getCaseDetails(),
                AUTH_TOKEN)
        );
    }
}
