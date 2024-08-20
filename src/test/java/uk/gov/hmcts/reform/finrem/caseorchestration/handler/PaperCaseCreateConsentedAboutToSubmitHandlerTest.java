package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCaseDetailsBuilderFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationWorkflowService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.NEW_PAPER_CASE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.SOLICITOR_CREATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class PaperCaseCreateConsentedAboutToSubmitHandlerTest {

    @Mock
    private UpdateRepresentationWorkflowService updateRepresentationWorkflowService;

    @InjectMocks
    private PaperCaseCreateConsentedAboutToSubmitHandler underTest;

    @Test
    void testHandlerCanHandle() {
        assertCanHandle(underTest, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, NEW_PAPER_CASE);
    }

    @Test
    void shouldPopulateDefaultOrganisationPolicyData() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(
            SOLICITOR_CREATE, FinremCaseDetailsBuilderFactory.from(123L, CONSENTED));
        underTest.handle(callbackRequest, AUTH_TOKEN);
        verify(updateRepresentationWorkflowService).persistDefaultOrganisationPolicy(any(FinremCaseData.class));
    }
}
