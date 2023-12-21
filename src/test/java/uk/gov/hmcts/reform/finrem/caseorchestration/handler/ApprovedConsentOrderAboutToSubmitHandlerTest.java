package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderApprovedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import java.io.InputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ApprovedConsentOrderAboutToSubmitHandlerTest {

    @InjectMocks
    private ApprovedConsentOrderAboutToSubmitHandler handler;
    @Mock
    private ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;
    @Mock
    private GenericDocumentService genericDocumentService;
    @Mock
    private ConsentOrderPrintService consentOrderPrintService;
    @Mock
    private DocumentHelper documentHelper;
    private final ObjectMapper objectMapper = new ObjectMapper();


    private static final String AUTH_TOKEN = "4d73f8d4-2a8d-48e2-af91-11cbaa642345";
    private static final String APPROVE_ORDER_VALID_JSON = "/fixtures/pba-validate.json";
    private static final String APPROVE_ORDER_NO_PENSION_VALID_JSON = "/fixtures/bulkprint/bulk-print-no-pension-collection.json";
    private static final String NO_PENSION_VALID_JSON = "/fixtures/bulkprint/bulk-print-no-pension-collection.json";


    @Test
    void given_case_whenEvent_type_is_approveOrder_thenCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.APPROVE_ORDER),
            is(true));
    }

    @Test
    void given_case_when_wrong_callback_then_case_can_not_handle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.APPROVE_ORDER),
            is(false));
    }

    @Test
    void given_case_when_wrong_casetype_then_case_can_not_handle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.APPROVE_ORDER),
            is(false));
    }

    @Test
    void given_case_when_wrong_eventtype_then_case_can_not_handle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.CLOSE),
            is(false));
    }


    @Test
    void given_case_when_consent_order_requested_then_create_consent_order() {

        FinremCallbackRequest callbackRequest = doValidCaseDataSetUp();

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(callbackRequest, AUTH_TOKEN);
        assertNotNull(handle.getData().getOtherDocumentsCollection());

        verify(consentOrderApprovedDocumentService).generateApprovedConsentOrderLetter(any(FinremCaseDetails.class),eq(AUTH_TOKEN));
        verify(genericDocumentService).annexStampDocument(any(), any(), any(), any());
        verify(documentHelper, times(1)).getPensionDocumentsData(any(FinremCaseData.class));
        verify(consentOrderPrintService).sendConsentOrderToBulkPrint(any(FinremCaseDetails.class), any());
    }


    private FinremCallbackRequest doValidCaseDataSetUp() {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(APPROVE_ORDER_VALID_JSON)) {
            return objectMapper.readValue(resourceAsStream, FinremCallbackRequest.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
