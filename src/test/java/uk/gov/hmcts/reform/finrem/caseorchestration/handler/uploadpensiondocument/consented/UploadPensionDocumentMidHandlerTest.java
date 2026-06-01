package uk.gov.hmcts.reform.finrem.caseorchestration.handler.uploadpensiondocument.consented;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class UploadPensionDocumentMidHandlerTest {

    @InjectMocks
    private UploadPensionDocumentMidHandler underTest;

    @Mock
    private ConsentedApplicationHelper consentedApplicationHelper;

    @Test
    void testCanHandle() {
        assertCanHandle(underTest, CallbackType.MID_EVENT, CaseType.CONSENTED, EventType.UPLOAD_PENSION_DOCUMENTS);
    }

    @Test
    void shouldSetConsentVariationOrderLabelField() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from();
        underTest.handle(callbackRequest, AUTH_TOKEN);

        verify(consentedApplicationHelper).setConsentVariationOrderLabelField(callbackRequest.getFinremCaseData());
    }
}
