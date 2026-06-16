package uk.gov.hmcts.reform.finrem.caseorchestration.handler.managecasedocuments.contested;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremSubmittedCallbackHandler;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.NEW_MANAGE_CASE_DOCUMENTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class ManageCaseDocumentsSubmittedHandlerTest {

    @InjectMocks
    private ManageCaseDocumentsSubmittedHandler underTest;

    @Test
    void testCanHandle() {
        assertCanHandle(underTest, SUBMITTED, CONTESTED, NEW_MANAGE_CASE_DOCUMENTS);
    }

    @Test
    void testHandlerExtendsFinremSubmittedCallbackHandler() {
        assertThat(underTest).isInstanceOf(FinremSubmittedCallbackHandler.class);
    }
}
