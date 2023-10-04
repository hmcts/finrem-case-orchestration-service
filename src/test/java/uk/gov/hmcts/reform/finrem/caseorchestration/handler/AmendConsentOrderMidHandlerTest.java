package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AmendedConsentOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AmendedConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintDocumentService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;

@ExtendWith(MockitoExtension.class)
class AmendConsentOrderMidHandlerTest extends BaseHandlerTestSetup {

    private AmendConsentOrderMidHandler handler;
    @Mock
    private BulkPrintDocumentService service;
    private static final String FILE_URL = "http://dm:80/documents/kbjh87y8y9JHVKKKJVJ";
    private static final String FILE_BINARY_URL = "http://dm:80/documents/kbjh87y8y9JHVKKKJVJ/binary";
    private static final String FILE_NAME = "abc.pdf";
    public static final String AUTH_TOKEN = "tokien:)";


    @BeforeEach
    void setup() {
        FinremCaseDetailsMapper finremCaseDetailsMapper = new FinremCaseDetailsMapper(new ObjectMapper().registerModule(new JavaTimeModule()));
        handler = new AmendConsentOrderMidHandler(finremCaseDetailsMapper, service);
    }

    @Test
    void canHandle() {
        assertTrue(handler.canHandle(CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.AMEND_CONSENT_ORDER));
    }

    @Test
    void canNotHandle() {
        assertFalse(handler.canHandle(CallbackType.MID_EVENT, CaseType.CONSENTED, EventType.AMEND_CONSENT_ORDER));
    }

    @Test
    void canNotHandleWrongEventType() {
        assertFalse(handler.canHandle(CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.CLOSE));
    }

    @Test
    void canNotHandleWrongCallbackType() {
        assertFalse(handler.canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.AMEND_CONSENT_ORDER));
    }


    @Test
    void givenContestedCase_whenAmendedConsentOrderUploadedNonEncryptedFileShouldNotGetError() {
        FinremCallbackRequest finremCallbackRequest = buildConsentCallbackRequest(EventType.AMEND_CONSENT_ORDER);
        FinremCaseData caseData = finremCallbackRequest.getCaseDetails().getData();

        CaseDocument caseDocument = caseDocument(FILE_URL, FILE_NAME, FILE_BINARY_URL);

        List<AmendedConsentOrderCollection> amendedCollection = new  ArrayList<>();

        AmendedConsentOrderCollection order = AmendedConsentOrderCollection.builder()
            .value(AmendedConsentOrder.builder().amendedConsentOrder(caseDocument).build()).build();

        amendedCollection.add(order);
        caseData.setAmendedConsentOrderCollection(amendedCollection);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertTrue(response.getErrors().isEmpty());
        verify(service).validateEncryptionOnUploadedDocument(any(), any(), any(), any());
    }
}