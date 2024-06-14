package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderApprovedDocumentService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.CONSENT_APPLICATION_APPROVED_IN_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class ConsentApplicationApprovedInContestedAboutToSubmitHandlerTest extends BaseHandlerTestSetup {

    public static final String AUTH_TOKEN = "token:)";
    private ConsentApplicationApprovedInContestedAboutToSubmitHandler aboutToSubmitHandler;
    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;
    @Mock
    private ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;

    @BeforeEach
    public void init() {
        aboutToSubmitHandler = new ConsentApplicationApprovedInContestedAboutToSubmitHandler(finremCaseDetailsMapper,
                consentOrderApprovedDocumentService);
    }

    @Test
    void testHandlerCanHandle() {
        assertCanHandle(aboutToSubmitHandler, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, CONSENT_APPLICATION_APPROVED_IN_CONTESTED);
    }

    @Test
    void consentInContestedConsentOrderApprovedShouldProcessDocuments() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest("/fixtures/pba-validate.json");
        aboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(consentOrderApprovedDocumentService).stampAndPopulateContestedConsentApprovedOrderCollection(any(), eq(AUTH_TOKEN), anyString());
        verify(consentOrderApprovedDocumentService).generateAndPopulateConsentOrderLetter(any(), eq(AUTH_TOKEN));
    }

    @Test
    void consentInContestedConsentOrderApprovedShouldProcessPensionDocs() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest("/fixtures/contested/consent-in-contested-with-pension.json");
        aboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(consentOrderApprovedDocumentService).stampAndPopulateContestedConsentApprovedOrderCollection(any(), eq(AUTH_TOKEN), any());
    }
}
