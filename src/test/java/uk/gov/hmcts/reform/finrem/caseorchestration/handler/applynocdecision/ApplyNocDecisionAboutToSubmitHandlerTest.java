package uk.gov.hmcts.reform.finrem.caseorchestration.handler.applynocdecision;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenerateCoverSheetService;

import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class ApplyNocDecisionAboutToSubmitHandlerTest {

    @Mock
    private GenerateCoverSheetService generateCoverSheetService;

    @InjectMocks
    private ApplyNocDecisionAboutToSubmitHandler handler;

    @Test
    void testCanHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.APPLY_NOC_DECISION);
    }
}
