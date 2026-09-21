package uk.gov.hmcts.reform.finrem.caseorchestration.handler.reassignjudge.consented;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.reassignjudge.ReassignJudgeAboutToSubmitHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudge.consented.AssignToJudgeCorresponder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID_IN_LONG;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.REASSIGN_JUDGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class ReassignJudgeAboutToSubmitHandlerTest {

    @Mock
    private AssignToJudgeCorresponder assignToJudgeCorresponder;

    @InjectMocks
    private ReassignJudgeAboutToSubmitHandler underTest;

    @Test
    void testCanHandle() {
        assertCanHandle(underTest, CallbackType.ABOUT_TO_SUBMIT, CONSENTED, REASSIGN_JUDGE);
    }

    @Test
    void shouldCreateAuditsForCorrespondence_whenHandled() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, REASSIGN_JUDGE);

        var response = underTest.handle(callbackRequest, AUTH_TOKEN);

        assertAll(
            () -> assertThat(response.getData()).isEqualTo(callbackRequest.getCaseDetails().getData()),
            () -> verify(assignToJudgeCorresponder).createAuditsForCorrespondence(REASSIGN_JUDGE, callbackRequest.getCaseDetails(),
                AUTH_TOKEN)
        );
    }
}
