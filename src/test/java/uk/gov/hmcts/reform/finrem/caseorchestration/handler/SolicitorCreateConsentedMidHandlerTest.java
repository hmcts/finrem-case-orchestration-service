package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseManagementLocationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
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

@RunWith(MockitoJUnitRunner.class)
public class SolicitorCreateConsentedMidHandlerTest {

    public static final String AUTH_TOKEN = "4d73f8d4-2a8d-48e2-af91-11cbaa642345";

    @Mock
    private CaseManagementLocationService caseManagementLocationService;
    @Spy
    private ConsentedApplicationHelper consentedApplicationHelper = new ConsentedApplicationHelper();
    @InjectMocks
    private SolicitorCreateConsentedMidHandler solicitorCreateConsentedMidHandler;

    @Captor
    private ArgumentCaptor<CallbackRequest> requestCaptor;

    @Test
    public void given_case_whenEvent_type_is_amendApp_thenCanHandle() {
        assertThat(solicitorCreateConsentedMidHandler
                .canHandle(CallbackType.MID_EVENT, CaseType.CONSENTED, EventType.AMEND_APP_DETAILS),
            is(true));
    }

    @Test
    public void given_case_when_wrong_callback_then_case_can_not_handle() {
        assertThat(solicitorCreateConsentedMidHandler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.SOLICITOR_CREATE),
            is(false));
    }

    @Test
    public void given_case_when_wrong_caseType_then_case_can_not_handle() {
        assertThat(solicitorCreateConsentedMidHandler
                .canHandle(CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.SOLICITOR_CREATE),
            is(false));
    }

    @Test
    public void given_case_when_wrong_eventType_then_case_can_not_handle() {
        assertThat(solicitorCreateConsentedMidHandler
                .canHandle(CallbackType.MID_EVENT, CaseType.CONSENTED, EventType.CLOSE),
            is(false));
    }

    @Test
    public void given_case_when_natureOfApplicationIsVariation_thenReturnVariationOrderLabels() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        List<String> orderList = List.of("Variation Order", "Property Adjustment Order");
        callbackRequest.getCaseDetails().getData().put("natureOfApplication2", orderList);

        when(caseManagementLocationService.setCaseManagementLocation(any(CallbackRequest.class)))
            .thenReturn(GenericAboutToStartOrSubmitCallbackResponse.<Map<String, Object>>builder().build());

        solicitorCreateConsentedMidHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(caseManagementLocationService).setCaseManagementLocation(requestCaptor.capture());

        Map<String, Object> responseData = requestCaptor.getValue().getCaseDetails().getData();

        final String camelCaseLabel = (String) responseData.get(CV_ORDER_CAMELCASE_LABEL_FIELD);
        assertEquals(VARIATION_ORDER_CAMELCASE_LABEL_VALUE, camelCaseLabel);
        final String lowerCaseLabel = (String) responseData.get(CV_LOWERCASE_LABEL_FIELD);
        assertEquals(VARIATION_ORDER_LOWERCASE_LABEL_VALUE, lowerCaseLabel);
        final String docLabel = (String) responseData.get(CV_OTHER_DOC_LABEL_FIELD);
        assertEquals(CV_OTHER_DOC_LABEL_VALUE, docLabel);
    }

    @Test
    public void given_case_when_natureOfApplicationDoNotContainsVariation_thenReturnConsentOrderLabels() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        List<String> orderList = List.of("Property Adjustment Order");
        callbackRequest.getCaseDetails().getData().put("natureOfApplication2", orderList);

        when(caseManagementLocationService.setCaseManagementLocation(any(CallbackRequest.class)))
            .thenReturn(GenericAboutToStartOrSubmitCallbackResponse.<Map<String, Object>>builder().build());

        solicitorCreateConsentedMidHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(caseManagementLocationService).setCaseManagementLocation(requestCaptor.capture());

        Map<String, Object> responseData = requestCaptor.getValue().getCaseDetails().getData();

        final String camelCaseLabel = (String) responseData.get(CV_ORDER_CAMELCASE_LABEL_FIELD);
        assertEquals(CONSENT_ORDER_CAMELCASE_LABEL_VALUE, camelCaseLabel);
        final String lowerCaseLabel = (String) responseData.get(CV_LOWERCASE_LABEL_FIELD);
        assertEquals(CONSENT_ORDER_LOWERCASE_LABEL_VALUE, lowerCaseLabel);
        final String docLabel = (String) responseData.get(CV_OTHER_DOC_LABEL_FIELD);
        assertEquals(CONSENT_OTHER_DOC_LABEL_VALUE, docLabel);
    }

    private CallbackRequest buildCallbackRequest() {
        Map<String, Object> caseData = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().id(123L).build();
        caseDetails.setData(caseData);
        return CallbackRequest.builder().eventId(EventType.SOLICITOR_CREATE.getCcdType())
            .caseDetails(caseDetails).build();
    }
}