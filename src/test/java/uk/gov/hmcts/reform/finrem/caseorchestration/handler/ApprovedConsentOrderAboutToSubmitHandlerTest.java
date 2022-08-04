package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import feign.FeignException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.OngoingStubbing;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderApprovedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.ccd.callback.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseType;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.EventType;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.feignError;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.newDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ConsentedStatus.CONSENT_ORDER_MADE;

@RunWith(MockitoJUnitRunner.class)
public class ApprovedConsentOrderAboutToSubmitHandlerTest extends BaseHandlerTest {

    private ApprovedConsentOrderAboutToSubmitHandler handler;
    @Mock
    private ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;
    @Mock
    private GenericDocumentService genericDocumentService;
    @Mock
    private ConsentOrderPrintService consentOrderPrintService;
    @Mock
    private NotificationService notificationService;

    private static final String AUTH_TOKEN = "4d73f8d4-2a8d-48e2-af91-11cbaa642345";
    private static final String APPROVE_ORDER_VALID_JSON = "/fixtures/pba-validate.json";
    private static final String APPROVE_ORDER_NO_PENSION_VALID_JSON = "/fixtures/bulkprint/bulk-print-no-pension-collection.json";
    private static final String NO_PENSION_VALID_JSON = "/fixtures/bulkprint/bulk-print-no-pension-collection.json";

    @Before
    public void setup() {
        handler = new ApprovedConsentOrderAboutToSubmitHandler(consentOrderApprovedDocumentService,
            genericDocumentService,
            consentOrderPrintService,
            notificationService);
    }

    @Test
    public void given_case_whenEvent_type_is_approveOrder_thenCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.APPROVE_APPLICATION),
            is(true));
    }

    @Test
    public void given_case_when_wrong_callback_then_case_can_not_handle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.APPROVE_APPLICATION),
            is(false));
    }

    @Test
    public void given_case_when_wrong_casetype_then_case_can_not_handle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.APPROVE_APPLICATION),
            is(false));
    }

    @Test
    public void given_case_when_wrong_eventtype_then_case_can_not_handle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.CLOSE),
            is(false));
    }


    @Test
    public void given_case_when_consent_order_requested_then_create_consent_order() {
        CallbackRequest callbackRequest = doValidCaseDataSetUp(APPROVE_ORDER_VALID_JSON);
        callbackRequest.getCaseDetails().getCaseData().setPensionCollection(null);

        AboutToStartOrSubmitCallbackResponse response = handler.handle(callbackRequest, AUTH_TOKEN);
        assertNotNull(response.getData().getOtherDocumentsCollection());

        verify(consentOrderApprovedDocumentService).generateApprovedConsentOrderLetter(any(), any());
        verify(genericDocumentService).annexStampDocument(isA(Document.class), any());
        verify(consentOrderPrintService).sendConsentOrderToBulkPrint(any(), any());
        verify(notificationService).sendConsentOrderAvailableCtscEmail(any());
        verify(notificationService).isRespondentSolicitorEmailCommunicationEnabled(isA(FinremCaseData.class));
    }

    @Test(expected = FeignException.InternalServerError.class)
    public void given_case_when_failed_to_generate_doc_then_should_throw_error() {
        CallbackRequest callbackRequest = doValidCaseDataSetUp(APPROVE_ORDER_VALID_JSON);
        whenServiceGeneratesDocument().thenThrow(feignError());

        handler.handle(callbackRequest, AUTH_TOKEN);
    }

    @Test
    public void given_case_when_NotPaperApplication_then_shouldNotTriggerConsentOrderApprovedNotificationLetter()  {
        whenServiceGeneratesDocument().thenReturn(newDocument());
        whenAnnexStampingDocument().thenReturn(newDocument());

        CallbackRequest callbackRequest = doValidCaseDataSetUp(APPROVE_ORDER_VALID_JSON);
        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(consentOrderApprovedDocumentService, never()).generateApprovedConsentOrderCoverLetter(any(), any());
    }

    @Test
    public void givenCase_whenNoPension_thenShouldUpdateStateToConsentOrderMadeAndBulkPrint() {
        CallbackRequest callbackRequest = doValidCaseDataSetUp(NO_PENSION_VALID_JSON);
        whenServiceGeneratesDocument().thenReturn(newDocument());
        whenAnnexStampingDocument().thenReturn(newDocument());
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(isA(FinremCaseData.class))).thenReturn(true);
        when(notificationService.isApplicantSolicitorRegisteredAndEmailCommunicationEnabled(isA(FinremCaseDetails.class))).thenReturn(true);

        AboutToStartOrSubmitCallbackResponse response = handler.handle(callbackRequest, AUTH_TOKEN);

        assertEquals(response.getData().getState(), CONSENT_ORDER_MADE.toString());

        verify(consentOrderPrintService).sendConsentOrderToBulkPrint(any(), any());
        verify(notificationService).sendConsentOrderAvailableCtscEmail(any());
        verify(notificationService).sendConsentOrderAvailableEmailToApplicantSolicitor(any());
        verify(notificationService).sendConsentOrderAvailableEmailToRespondentSolicitor(any());
    }

    @Test
    public void shouldUpdateStateToConsentOrderMadeAndBulkPrint_noEmails() {
        CallbackRequest callbackRequest = doValidCaseDataSetUp(APPROVE_ORDER_NO_PENSION_VALID_JSON);
        whenServiceGeneratesDocument().thenReturn(newDocument());
        whenAnnexStampingDocument().thenReturn(newDocument());
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(isA(FinremCaseData.class))).thenReturn(false);
        when(notificationService.isApplicantSolicitorRegisteredAndEmailCommunicationEnabled(isA(FinremCaseDetails.class)))
            .thenReturn(false);

        AboutToStartOrSubmitCallbackResponse response = handler.handle(callbackRequest, AUTH_TOKEN);

        assertEquals(response.getData().getState(), CONSENT_ORDER_MADE.toString());

        verify(consentOrderPrintService).sendConsentOrderToBulkPrint(any(), any());
        verify(notificationService).sendConsentOrderAvailableCtscEmail(any());
        verify(notificationService, never()).sendConsentOrderAvailableEmailToApplicantSolicitor(any());
        verify(notificationService, never()).sendConsentOrderAvailableEmailToRespondentSolicitor(any());
    }

    private CallbackRequest doValidCaseDataSetUp(final String path)  {
        try {
            return getCallbackRequestFromResource(path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private OngoingStubbing<Document> whenServiceGeneratesDocument() {
        return when(consentOrderApprovedDocumentService.generateApprovedConsentOrderLetter(isA(FinremCaseDetails.class), eq(AUTH_TOKEN)));
    }

    private OngoingStubbing<Document> whenAnnexStampingDocument() {
        return when(genericDocumentService.annexStampDocument(isA(Document.class), eq(AUTH_TOKEN)));
    }
}