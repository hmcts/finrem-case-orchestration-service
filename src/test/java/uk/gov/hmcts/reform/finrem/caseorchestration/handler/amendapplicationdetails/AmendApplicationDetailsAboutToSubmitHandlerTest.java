package uk.gov.hmcts.reform.finrem.caseorchestration.handler.amendapplicationdetails;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseFlagsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnlineFormDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.express.ExpressCaseService;

import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.AMEND_CONTESTED_APP_DETAILS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.AMEND_CONTESTED_PAPER_APP_DETAILS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class AmendApplicationDetailsAboutToSubmitHandlerTest {

    @InjectMocks
    private AmendApplicationDetailsAboutToSubmitHandler handler;
    
    @Mock
    private OnlineFormDocumentService onlineFormDocumentService;

    @Mock
    private CaseFlagsService caseFlagsService;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private ExpressCaseService expressCaseService;

    @Test
    void testCanHandle() {
        assertCanHandle(handler,
                Arguments.of(ABOUT_TO_SUBMIT, CONTESTED, AMEND_CONTESTED_PAPER_APP_DETAILS),
                Arguments.of(ABOUT_TO_SUBMIT, CONTESTED, AMEND_CONTESTED_APP_DETAILS)
        );
    }
}
