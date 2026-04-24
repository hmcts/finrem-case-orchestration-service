package uk.gov.hmcts.reform.finrem.caseorchestration.handler.amendapplicationdetails;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.AMEND_APP_DETAILS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.AMEND_CONTESTED_APP_DETAILS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.AMEND_CONTESTED_PAPER_APP_DETAILS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class AmendApplicationDetailsSubmittedHandlerTest {
    @InjectMocks
    private AmendApplicationDetailsSubmittedHandler underTest;

    @Test
    void testHandlerCanHandle() {
        assertCanHandle(underTest,
            Arguments.of(SUBMITTED, CONTESTED, AMEND_CONTESTED_PAPER_APP_DETAILS),
            Arguments.of(SUBMITTED, CONTESTED, AMEND_CONTESTED_APP_DETAILS),
            Arguments.of(SUBMITTED, CONSENTED, AMEND_APP_DETAILS)
        );
    }
}
