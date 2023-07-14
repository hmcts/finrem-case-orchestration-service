package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderApprovedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import java.io.InputStream;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.feignError;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ConsentedStatus.CONSENT_ORDER_MADE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.STATE;

@ExtendWith(MockitoExtension.class)
class ApprovedConsentOrderAboutToSubmitHandlerTest {

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


    @BeforeEach
    void setup() {
        handler = new ApprovedConsentOrderAboutToSubmitHandler(consentOrderApprovedDocumentService,
            genericDocumentService,
            consentOrderPrintService,
            documentHelper,
            objectMapper);
    }

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
        CallbackRequest callbackRequest =
            doValidCaseDataSetUp(APPROVE_ORDER_VALID_JSON);

        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> response = handler.handle(callbackRequest, AUTH_TOKEN);
        assertNotNull(response.getData().get("otherCollection"));

        verify(consentOrderApprovedDocumentService).generateApprovedConsentOrderLetter(any(), any());
        verify(genericDocumentService).annexStampDocument(any(), any(), any(), any());
        verify(documentHelper, times(2)).getPensionDocumentsData(any());
        verify(consentOrderPrintService).sendConsentOrderToBulkPrint(any(), any());
    }

    @Test
    void given_case_when_failed_to_generate_doc_then_should_throw_error() {
        CallbackRequest callbackRequest =
            doValidCaseDataSetUp(APPROVE_ORDER_VALID_JSON);
        whenServiceGeneratesDocument().thenThrow(feignError());

        assertThrows(FeignException.InternalServerError.class, () -> {
            handler.handle(callbackRequest, AUTH_TOKEN);
        });
        ;
    }

    @Test
    void given_case_when_NotPaperApplication_then_shouldNotTriggerConsentOrderApprovedNotificationLetter() {
        whenServiceGeneratesDocument().thenReturn(caseDocument());
        when(documentHelper.getPensionDocumentsData(any())).thenReturn(singletonList(caseDocument()));

        CallbackRequest callbackRequest =
            doValidCaseDataSetUp(APPROVE_ORDER_VALID_JSON);
        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(consentOrderApprovedDocumentService, never()).generateApprovedConsentOrderCoverLetter(any(), any());
        verify(genericDocumentService).convertDocumentIfNotPdfAlready(any(), any(), any());
    }

    @Test
    void givenCase_whenNoPendsion_thenShouldUpdateStateToConsentOrderMadeAndBulkPrint() {
        CallbackRequest callbackRequest =
            doValidCaseDataSetUp(NO_PENSION_VALID_JSON);
        whenServiceGeneratesDocument().thenReturn(caseDocument());

        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> response = handler.handle(callbackRequest, AUTH_TOKEN);

        assertEquals(response.getData().get(STATE), CONSENT_ORDER_MADE.toString());

        verify(consentOrderPrintService).sendConsentOrderToBulkPrint(any(), any());
    }

    @Test
    void shouldUpdateStateToConsentOrderMadeAndBulkPrint_noEmails() {
        CallbackRequest callbackRequest =
            doValidCaseDataSetUp(APPROVE_ORDER_NO_PENSION_VALID_JSON);
        whenServiceGeneratesDocument().thenReturn(caseDocument());

        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> response = handler.handle(callbackRequest, AUTH_TOKEN);

        assertEquals(response.getData().get(STATE), CONSENT_ORDER_MADE.toString());

        verify(consentOrderPrintService).sendConsentOrderToBulkPrint(any(), any());
    }

    private CallbackRequest doValidCaseDataSetUp(final String path) {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(path)) {
            return objectMapper.readValue(resourceAsStream, CallbackRequest.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private OngoingStubbing<CaseDocument> whenServiceGeneratesDocument() {
        return when(consentOrderApprovedDocumentService.generateApprovedConsentOrderLetter(isA(CaseDetails.class), eq(AUTH_TOKEN)));
    }
}