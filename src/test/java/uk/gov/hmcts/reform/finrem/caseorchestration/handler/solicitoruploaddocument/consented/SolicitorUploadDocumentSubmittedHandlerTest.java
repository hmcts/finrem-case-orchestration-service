package uk.gov.hmcts.reform.finrem.caseorchestration.handler.solicitoruploaddocument.consented;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremSubmittedCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class SolicitorUploadDocumentSubmittedHandlerTest {

    @InjectMocks
    private SolicitorUploadDocumentSubmittedHandler underTest;

    @Test
    void testCanHandle() {
        assertCanHandle(underTest, CallbackType.SUBMITTED, CaseType.CONSENTED, EventType.SOLICITOR_UPLOAD_DOCUMENT);
    }

    @Test
    void testHandlerExtendsFinremSubmittedCallbackHandler() {
        assertThat(underTest).isInstanceOf(FinremSubmittedCallbackHandler.class);
    }
}
