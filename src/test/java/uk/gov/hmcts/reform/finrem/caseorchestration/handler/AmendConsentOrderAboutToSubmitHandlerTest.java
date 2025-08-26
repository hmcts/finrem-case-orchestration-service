package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseFlagsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderService;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class AmendConsentOrderAboutToSubmitHandlerTest {

    @InjectMocks
    private AmendConsentOrderAboutToSubmitHandler handler;

    @Mock
    private ConsentOrderService consentOrderService;

    @Mock
    private CaseFlagsService caseFlagsService;

    @Test
    void testCanHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.AMEND_CONSENT_ORDER);
    }

    @Test
    void givenCase_whenRequestToUpdateLatestConsentOrderAndUserDoNotHaveAdminRole_thenHandlerCanHandle() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        when(consentOrderService.getLatestConsentOrderData(any(FinremCallbackRequest.class))).thenReturn(caseDocument());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        assertNotNull(response.getData().getLatestConsentOrder());
        verify(consentOrderService).getLatestConsentOrderData(any(FinremCallbackRequest.class));
        verify(caseFlagsService).setCaseFlagInformation(callbackRequest.getCaseDetails());
    }

    private FinremCallbackRequest buildCallbackRequest() {
        FinremCallbackRequest mockedCallbackRequest = mock(FinremCallbackRequest.class);
        FinremCaseDetails mockedCaseDetails = mock(FinremCaseDetails.class);
        when(mockedCallbackRequest.getCaseDetails()).thenReturn(mockedCaseDetails);

        FinremCaseData mockedCaseData = spy(FinremCaseData.class);
        when(mockedCaseDetails.getData()).thenReturn(mockedCaseData);
        when(mockedCaseDetails.getId()).thenReturn(Long.valueOf(CASE_ID));
        return mockedCallbackRequest;
    }
}
