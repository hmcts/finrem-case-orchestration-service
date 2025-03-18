package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCaseDetailsBuilderFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.express.ExpressCaseService;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.MANAGE_EXPRESS_CASE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class ManageExpressCaseAboutToSubmitHandlerTest {

    @Mock
    private ExpressCaseService expressCaseService;

    @InjectMocks
    private ManageExpressCaseAboutToSubmitHandler underTest;

    @Test
    void testHandlerCanHandle() {
        assertCanHandle(underTest, ABOUT_TO_SUBMIT, CONTESTED, MANAGE_EXPRESS_CASE);
    }

    @Test
    void shouldPopulateDefaultOrganisationPolicyData() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(
            MANAGE_EXPRESS_CASE, FinremCaseDetailsBuilderFactory.from(123L, CONTESTED));
        underTest.handle(callbackRequest, AUTH_TOKEN);
        verify(expressCaseService).setExpressCaseEnrollmentStatusToWithdrawn(callbackRequest.getCaseDetails().getData());
    }
}
