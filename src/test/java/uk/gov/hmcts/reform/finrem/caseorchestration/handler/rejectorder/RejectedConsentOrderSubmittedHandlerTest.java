package uk.gov.hmcts.reform.finrem.caseorchestration.handler.rejectorder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder.ConsentOrderNotApprovedCorresponder;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@RunWith(MockitoJUnitRunner.class)
public class RejectedConsentOrderSubmittedHandlerTest {

    @InjectMocks
    private RejectedConsentOrderSubmittedHandler handler;

    @Mock
    private ConsentOrderNotApprovedCorresponder consentOrderNotApprovedCorresponder;

    @Test
    public void testCanHandle() {
        assertCanHandle(handler ,CallbackType.SUBMITTED, CaseType.CONSENTED, EventType.REJECT_ORDER);
    }

    @Test
    public void givenCase_whenHandled_thenSendCorrespondence() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from();

        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(consentOrderNotApprovedCorresponder).sendCorrespondence(callbackRequest.getCaseDetails());
    }
}
