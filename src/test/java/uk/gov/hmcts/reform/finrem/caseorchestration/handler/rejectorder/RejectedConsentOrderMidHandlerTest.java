package uk.gov.hmcts.reform.finrem.caseorchestration.handler.rejectorder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.RefusalOrderDocumentService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class RejectedConsentOrderMidHandlerTest {

    @InjectMocks
    private RejectedConsentOrderMidHandler handler;
    @Mock
    private RefusalOrderDocumentService refusalOrderDocumentService;

    @Test
    void testCanHandle() {
        assertCanHandle(handler, CallbackType.MID_EVENT, CaseType.CONSENTED, EventType.REJECT_ORDER);
    }

    @Test
    void givenCase_whenHandled_thenGenerateConsentOrderNotApprovedPreview() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from();
        FinremCaseData finremCaseData = mock(FinremCaseData.class);

        when(refusalOrderDocumentService.previewConsentOrderNotApproved(AUTH_TOKEN, callbackRequest.getCaseDetails()))
            .thenReturn(finremCaseData);

        var response = handler.handle(callbackRequest, AUTH_TOKEN);
        assertAll(
            () -> assertThat(response.getData()).isEqualTo(finremCaseData),
            () -> verify(refusalOrderDocumentService).previewConsentOrderNotApproved(AUTH_TOKEN,
                callbackRequest.getCaseDetails()),
            () -> verifyNoMoreInteractions(refusalOrderDocumentService)
        );
    }
}
