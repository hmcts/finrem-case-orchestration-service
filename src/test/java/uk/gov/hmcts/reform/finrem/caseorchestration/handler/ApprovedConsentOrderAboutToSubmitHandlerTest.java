package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.OngoingStubbing;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderApprovedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.io.InputStream;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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

@RunWith(MockitoJUnitRunner.class)
public class ApprovedConsentOrderAboutToSubmitHandlerTest {

    private ApprovedConsentOrderAboutToSubmitHandler handler;
    @Mock
    private ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;
    @Mock
    private GenericDocumentService genericDocumentService;
    @Mock
    private CaseDataService caseDataService;
    @Mock
    private ConsentOrderPrintService consentOrderPrintService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private DocumentHelper documentHelper;
    private final ObjectMapper objectMapper = new ObjectMapper();


    private static final String AUTH_TOKEN = "4d73f8d4-2a8d-48e2-af91-11cbaa642345";
    private static final String APPROVE_ORDER_VALID_JSON = "/fixtures/pba-validate.json";
    private static final String APPROVE_ORDER_NO_PENSION_VALID_JSON = "/fixtures/bulkprint/bulk-print-no-pension-collection.json";
    private static final String NO_PENSION_VALID_JSON = "/fixtures/bulkprint/bulk-print-no-pension-collection.json";


    @Before
    public void setup() {
        handler = new ApprovedConsentOrderAboutToSubmitHandler(consentOrderApprovedDocumentService,
            genericDocumentService,
            consentOrderPrintService,
            notificationService,
            caseDataService,
            documentHelper,
            objectMapper);
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
        CallbackRequest callbackRequest =
            doValidCaseDataSetUp(APPROVE_ORDER_VALID_JSON);

        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> response = handler.handle(callbackRequest, AUTH_TOKEN);
        assertNotNull(response.getData().get("otherCollection"));

        verify(consentOrderApprovedDocumentService).generateApprovedConsentOrderLetter(any(), any());
        verify(genericDocumentService).annexStampDocument(any(), any());
        verify(documentHelper, times(2)).getPensionDocumentsData(any());
        verify(consentOrderPrintService).sendConsentOrderToBulkPrint(any(), any());
        verify(notificationService).sendConsentOrderAvailableCtscEmail(any());
        verify(caseDataService).isApplicantSolicitorAgreeToReceiveEmails(any());
        verify(notificationService).isRespondentSolicitorEmailCommunicationEnabled(any());
    }

    @Test(expected = FeignException.InternalServerError.class)
    public void given_case_when_failed_to_generate_doc_then_should_throw_error() {
        CallbackRequest callbackRequest =
            doValidCaseDataSetUp(APPROVE_ORDER_VALID_JSON);
        whenServiceGeneratesDocument().thenThrow(feignError());

        handler.handle(callbackRequest, AUTH_TOKEN);
    }

    @Test
    public void given_case_when_NotPaperApplication_then_shouldNotTriggerConsentOrderApprovedNotificationLetter() {
        whenServiceGeneratesDocument().thenReturn(caseDocument());
        whenAnnexStampingDocument().thenReturn(caseDocument());
        when(documentHelper.getPensionDocumentsData(any())).thenReturn(singletonList(caseDocument()));

        CallbackRequest callbackRequest =
            doValidCaseDataSetUp(APPROVE_ORDER_VALID_JSON);
        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(consentOrderApprovedDocumentService, never()).generateApprovedConsentOrderCoverLetter(any(), any());
    }

    @Test
    public void givenCase_whenNoPendsion_thenShouldUpdateStateToConsentOrderMadeAndBulkPrint() {
        CallbackRequest callbackRequest =
            doValidCaseDataSetUp(NO_PENSION_VALID_JSON);
        whenServiceGeneratesDocument().thenReturn(caseDocument());
        whenAnnexStampingDocument().thenReturn(caseDocument());
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(any())).thenReturn(true);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(true);

        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> response = handler.handle(callbackRequest, AUTH_TOKEN);

        assertEquals(response.getData().get(STATE), CONSENT_ORDER_MADE.toString());

        verify(consentOrderPrintService).sendConsentOrderToBulkPrint(any(), any());
        verify(notificationService).sendConsentOrderAvailableCtscEmail(any());
        verify(notificationService).sendConsentOrderAvailableEmailToApplicantSolicitor(any());
        verify(notificationService).sendConsentOrderAvailableEmailToRespondentSolicitor(any());
    }

    @Test
    public void shouldUpdateStateToConsentOrderMadeAndBulkPrint_noEmails() {
        CallbackRequest callbackRequest =
            doValidCaseDataSetUp(APPROVE_ORDER_NO_PENSION_VALID_JSON);
        whenServiceGeneratesDocument().thenReturn(caseDocument());
        whenAnnexStampingDocument().thenReturn(caseDocument());
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(any())).thenReturn(false);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(false);

        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> response = handler.handle(callbackRequest, AUTH_TOKEN);

        assertEquals(response.getData().get(STATE), CONSENT_ORDER_MADE.toString());

        verify(consentOrderPrintService).sendConsentOrderToBulkPrint(any(), any());
        verify(notificationService).sendConsentOrderAvailableCtscEmail(any());
        verify(notificationService, never()).sendConsentOrderAvailableEmailToApplicantSolicitor(any());
        verify(notificationService, never()).sendConsentOrderAvailableEmailToRespondentSolicitor(any());
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

    private OngoingStubbing<CaseDocument> whenAnnexStampingDocument() {
        return when(genericDocumentService.annexStampDocument(isA(CaseDocument.class), eq(AUTH_TOKEN)));
    }
}