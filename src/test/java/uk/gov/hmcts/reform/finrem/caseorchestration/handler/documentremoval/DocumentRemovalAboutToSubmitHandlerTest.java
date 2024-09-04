package uk.gov.hmcts.reform.finrem.caseorchestration.handler.documentremoval;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentremoval.DocumentRemovalService;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class DocumentRemovalAboutToSubmitHandlerTest {

    @Mock
    private DocumentRemovalService documentRemovalService;

    @InjectMocks
    private DocumentRemovalAboutToSubmitHandler handler;

    private FinremCallbackRequest callbackRequest;
    private FinremCaseData caseData;

    @BeforeEach
    public void setup() {
        caseData = FinremCaseData.builder().build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().data(caseData).id(1L).build();
        callbackRequest = FinremCallbackRequest.builder().caseDetails(caseDetails).build();
    }

    @Test
    void shouldHandleAllCaseTypes() {
        assertCanHandle(handler,
            Arguments.of(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.REMOVE_CASE_DOCUMENT),
            Arguments.of(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.REMOVE_CASE_DOCUMENT)
        );
    }

    @Test
    void testHandle() {
        when(documentRemovalService.removeDocuments(caseData, 1L, "auth")).thenReturn(caseData);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, "auth");

        assertEquals(response.getData(), caseData);
    }
}
