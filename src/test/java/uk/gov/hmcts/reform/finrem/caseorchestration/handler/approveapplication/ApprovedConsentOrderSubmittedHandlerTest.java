package uk.gov.hmcts.reform.finrem.caseorchestration.handler.approveapplication;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder.FinremConsentOrderAvailableCorresponder;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class ApprovedConsentOrderSubmittedHandlerTest {

    @InjectMocks
    private ApprovedConsentOrderSubmittedHandler handler;

    @Mock
    private FinremConsentOrderAvailableCorresponder consentOrderAvailableCorresponder;

    @Mock
    private DocumentHelper documentHelper;

    @Test
    void testCanHandle() {
        assertCanHandle(handler, CallbackType.SUBMITTED, CaseType.CONSENTED, EventType.APPROVE_ORDER);
    }

    @Test
    void givenEmptyPensionDocuments_whenHandled_thenSendCorrespondence() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from();

        when(documentHelper.getPensionDocumentsData(callbackRequest.getFinremCaseData())).thenReturn(Collections.emptyList());

        handler.handle(callbackRequest, AUTH_TOKEN);

        assertAll(
            () -> verify(documentHelper).getPensionDocumentsData(callbackRequest.getFinremCaseData()),
            () -> verify(consentOrderAvailableCorresponder).sendCorrespondence(callbackRequest.getCaseDetails())
        );
    }

    @Test
    void givenNonEmptyPensionDocuments_whenHandled_thenSendCorrespondence() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from();

        when(documentHelper.getPensionDocumentsData(callbackRequest.getFinremCaseData()))
            .thenReturn(List.of(mock(CaseDocument.class)));

        handler.handle(callbackRequest, AUTH_TOKEN);

        assertAll(
            () -> verify(documentHelper).getPensionDocumentsData(callbackRequest.getFinremCaseData()),
            () -> verify(consentOrderAvailableCorresponder, never()).sendCorrespondence(callbackRequest.getCaseDetails())
        );
    }
}
