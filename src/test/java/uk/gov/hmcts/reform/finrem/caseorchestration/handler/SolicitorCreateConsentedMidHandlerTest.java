package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CONSENT_ORDER_CAMELCASE_LABEL_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CONSENT_ORDER_LOWERCASE_LABEL_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CONSENT_OTHER_DOC_LABEL_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CV_LOWERCASE_LABEL_FIELD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CV_ORDER_CAMELCASE_LABEL_FIELD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CV_OTHER_DOC_LABEL_FIELD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CV_OTHER_DOC_LABEL_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.VARIATION_ORDER_CAMELCASE_LABEL_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.VARIATION_ORDER_LOWERCASE_LABEL_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;

@RunWith(MockitoJUnitRunner.class)
public class SolicitorCreateConsentedMidHandlerTest {

    private SolicitorCreateConsentedMidHandler solicitorCreateConsentedMidHandler;
    public static final String AUTH_TOKEN = "4d73f8d4-2a8d-48e2-af91-11cbaa642345";

    @Mock
    DocumentConfiguration documentConfiguration;
    @Mock
    private BulkPrintDocumentService service;

    @Mock
    private ConsentOrderService consentOrderService;

    @Before
    public void setup() {
        solicitorCreateConsentedMidHandler = new SolicitorCreateConsentedMidHandler(
            new ConsentedApplicationHelper(documentConfiguration), service, consentOrderService);
    }

    @Test
    public void given_case_whenEvent_type_is_amendApp_thenCanHandle() {
        assertTrue(solicitorCreateConsentedMidHandler.canHandle(CallbackType.MID_EVENT, CaseType.CONSENTED, EventType.AMEND_APP_DETAILS));
    }

    @Test
    public void given_case_when_wrong_callback_then_case_can_not_handle() {
        assertFalse(solicitorCreateConsentedMidHandler.canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.SOLICITOR_CREATE));
    }

    @Test
    public void given_case_when_wrong_casetype_then_case_can_not_handle() {
        assertFalse(solicitorCreateConsentedMidHandler.canHandle(CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.SOLICITOR_CREATE));
    }

    @Test
    public void given_case_when_wrong_eventtype_then_case_can_not_handle() {
        assertFalse(solicitorCreateConsentedMidHandler.canHandle(CallbackType.MID_EVENT, CaseType.CONSENTED, EventType.CLOSE));
    }

    @Test
    public void given_case_checkIfUploadedConsentOrderIsNotEncrypted() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        Map<String, Object> data = callbackRequest.getCaseDetails().getData();
        Map<String, Object> dataBefore = callbackRequest.getCaseDetailsBefore().getData();
        List<String> orderList = List.of("Variation Order", "Property Adjustment Order");
        data.put("natureOfApplication2", orderList);
        data.put("consentOrder", caseDocument());
        when(consentOrderService.checkIfD81DocumentContainsEncryption(data, dataBefore)).thenReturn(List.of(caseDocument()));
        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> response =
            solicitorCreateConsentedMidHandler.handle(callbackRequest, AUTH_TOKEN);

        final String camelCaseLabel = (String) response.getData().get(CV_ORDER_CAMELCASE_LABEL_FIELD);
        assertEquals(VARIATION_ORDER_CAMELCASE_LABEL_VALUE, camelCaseLabel);
        final String lowerCaseLabel = (String) response.getData().get(CV_LOWERCASE_LABEL_FIELD);
        assertEquals(VARIATION_ORDER_LOWERCASE_LABEL_VALUE, lowerCaseLabel);
        final String docLabel = (String) response.getData().get(CV_OTHER_DOC_LABEL_FIELD);
        assertEquals(CV_OTHER_DOC_LABEL_VALUE, docLabel);
        assertFalse(response.hasErrors());
        verify(service).validateEncryptionOnUploadedDocument(any(), any(), any(), any());
        verify(consentOrderService).checkIfD81DocumentContainsEncryption(anyMap(), anyMap());
    }

    @Test
    public void given_case_checkIfUploadedConsentOrderIsNotEncryptedIfSameDocumentAlreadyUploaded() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        Map<String, Object> data = callbackRequest.getCaseDetails().getData();
        Map<String, Object> dataBefore = callbackRequest.getCaseDetailsBefore().getData();
        List<String> orderList = List.of("Variation Order", "Property Adjustment Order");
        data.put("natureOfApplication2", orderList);
        data.put("consentOrder", caseDocument());
        dataBefore.put("natureOfApplication2", orderList);
        dataBefore.put("consentOrder", caseDocument());
        when(consentOrderService.checkIfD81DocumentContainsEncryption(data, dataBefore)).thenReturn(new ArrayList<>());
        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> response =
            solicitorCreateConsentedMidHandler.handle(callbackRequest, AUTH_TOKEN);

        final String camelCaseLabel = (String) response.getData().get(CV_ORDER_CAMELCASE_LABEL_FIELD);
        assertEquals(VARIATION_ORDER_CAMELCASE_LABEL_VALUE, camelCaseLabel);
        final String lowerCaseLabel = (String) response.getData().get(CV_LOWERCASE_LABEL_FIELD);
        assertEquals(VARIATION_ORDER_LOWERCASE_LABEL_VALUE, lowerCaseLabel);
        final String docLabel = (String) response.getData().get(CV_OTHER_DOC_LABEL_FIELD);
        assertEquals(CV_OTHER_DOC_LABEL_VALUE, docLabel);
        assertFalse(response.hasErrors());
        verify(service, never()).validateEncryptionOnUploadedDocument(any(), any(), any(), any());
        verify(consentOrderService).checkIfD81DocumentContainsEncryption(anyMap(), anyMap());
    }

    @Test
    public void given_case_when_natureOfApplicationIsVariation_thenReturnVariationOrderLabels() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        List<String> orderList = List.of("Variation Order", "Property Adjustment Order");
        callbackRequest.getCaseDetails().getData().put("natureOfApplication2", orderList);

        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> response =
            solicitorCreateConsentedMidHandler.handle(callbackRequest, AUTH_TOKEN);

        final String camelCaseLabel = (String) response.getData().get(CV_ORDER_CAMELCASE_LABEL_FIELD);
        assertEquals(VARIATION_ORDER_CAMELCASE_LABEL_VALUE, camelCaseLabel);
        final String lowerCaseLabel = (String) response.getData().get(CV_LOWERCASE_LABEL_FIELD);
        assertEquals(VARIATION_ORDER_LOWERCASE_LABEL_VALUE, lowerCaseLabel);
        final String docLabel = (String) response.getData().get(CV_OTHER_DOC_LABEL_FIELD);
        assertEquals(CV_OTHER_DOC_LABEL_VALUE, docLabel);
        verify(service, never()).validateEncryptionOnUploadedDocument(any(), any(), any(), any());
    }

    @Test
    public void given_case_when_natureOfApplicationDoNotContainsVariation_thenReturnConsentOrderLabels() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        List<String> orderList = List.of("Property Adjustment Order");
        callbackRequest.getCaseDetails().getData().put("natureOfApplication2", orderList);

        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> response =
            solicitorCreateConsentedMidHandler.handle(callbackRequest, AUTH_TOKEN);

        final String camelCaseLabel = (String) response.getData().get(CV_ORDER_CAMELCASE_LABEL_FIELD);
        assertEquals(CONSENT_ORDER_CAMELCASE_LABEL_VALUE, camelCaseLabel);
        final String lowerCaseLabel = (String) response.getData().get(CV_LOWERCASE_LABEL_FIELD);
        assertEquals(CONSENT_ORDER_LOWERCASE_LABEL_VALUE, lowerCaseLabel);
        final String docLabel = (String) response.getData().get(CV_OTHER_DOC_LABEL_FIELD);
        assertEquals(CONSENT_OTHER_DOC_LABEL_VALUE, docLabel);
    }

    private CallbackRequest buildCallbackRequest() {
        Map<String, Object> caseData = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().id(123L).build();
        caseDetails.setData(caseData);
        return CallbackRequest.builder().eventId(EventType.SOLICITOR_CREATE.getCcdType())
            .caseDetails(caseDetails).caseDetailsBefore(caseDetails).build();
    }
}