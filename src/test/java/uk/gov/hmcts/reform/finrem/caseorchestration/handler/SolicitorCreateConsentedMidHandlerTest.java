package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedApplicationHelper;
import uk.gov.hmcts.reform.finrem.ccd.callback.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseType;
import uk.gov.hmcts.reform.finrem.ccd.domain.EventType;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.NatureApplication;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CONSENT_ORDER_CAMELCASE_LABEL_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CONSENT_ORDER_LOWERCASE_LABEL_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CONSENT_OTHER_DOC_LABEL_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CV_OTHER_DOC_LABEL_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.VARIATION_ORDER_CAMELCASE_LABEL_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.VARIATION_ORDER_LOWERCASE_LABEL_VALUE;

@RunWith(MockitoJUnitRunner.class)
public class SolicitorCreateConsentedMidHandlerTest {

    @Autowired
    private SolicitorCreateConsentedMidHandler solicitorCreateConsentedMidHandler;
    public static final String AUTH_TOKEN = "4d73f8d4-2a8d-48e2-af91-11cbaa642345";

    @Before
    public void setup() {
        solicitorCreateConsentedMidHandler = new SolicitorCreateConsentedMidHandler(
            new ConsentedApplicationHelper(new DocumentConfiguration()));
    }

    @Test
    public void given_case_whenEvent_type_is_amendApp_thenCanHandle() {
        assertThat(solicitorCreateConsentedMidHandler
                .canHandle(CallbackType.MID_EVENT, CaseType.CONSENTED, EventType.AMEND_APPLICATION_DETAILS),
            is(true));
    }

    @Test
    public void given_case_when_wrong_callback_then_case_can_not_handle() {
        assertThat(solicitorCreateConsentedMidHandler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.SOLICITOR_CREATE),
            is(false));
    }

    @Test
    public void given_case_when_wrong_casetype_then_case_can_not_handle() {
        assertThat(solicitorCreateConsentedMidHandler
                .canHandle(CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.SOLICITOR_CREATE),
            is(false));
    }

    @Test
    public void given_case_when_wrong_eventtype_then_case_can_not_handle() {
        assertThat(solicitorCreateConsentedMidHandler
                .canHandle(CallbackType.MID_EVENT, CaseType.CONSENTED, EventType.CLOSE),
            is(false));
    }

    @Test
    public void given_case_when_natureOfApplicationIsVariation_thenReturnVariationOrderLabels() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        List<NatureApplication> orderList  = List.of(NatureApplication.VARIATION_ORDER, NatureApplication.PROPERTY_ADJUSTMENT_ORDER);
        callbackRequest.getCaseDetails().getCaseData().getNatureApplicationWrapper().setNatureOfApplication2(orderList);

        AboutToStartOrSubmitCallbackResponse response =
            solicitorCreateConsentedMidHandler.handle(callbackRequest, AUTH_TOKEN);

        final String camelCaseLabel = response.getData().getConsentOrderWrapper().getConsentVariationOrderLabelC();
        assertEquals(VARIATION_ORDER_CAMELCASE_LABEL_VALUE, camelCaseLabel);
        final String lowerCaseLabel = response.getData().getConsentOrderWrapper().getConsentVariationOrderLabelL();
        assertEquals(VARIATION_ORDER_LOWERCASE_LABEL_VALUE, lowerCaseLabel);
        final String docLabel = response.getData().getConsentOrderWrapper().getOtherDocLabel();
        assertEquals(CV_OTHER_DOC_LABEL_VALUE, docLabel);
    }

    @Test
    public void given_case_when_natureOfApplicationDoNotContainsVariation_thenReturnConsentOrderLabels() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        List<NatureApplication> orderList  = List.of(NatureApplication.PROPERTY_ADJUSTMENT_ORDER);
        callbackRequest.getCaseDetails().getCaseData().getNatureApplicationWrapper().setNatureOfApplication2(orderList);

        AboutToStartOrSubmitCallbackResponse response =
            solicitorCreateConsentedMidHandler.handle(callbackRequest, AUTH_TOKEN);

        final String camelCaseLabel = response.getData().getConsentOrderWrapper().getConsentVariationOrderLabelC();
        assertEquals(CONSENT_ORDER_CAMELCASE_LABEL_VALUE, camelCaseLabel);
        final String lowerCaseLabel = response.getData().getConsentOrderWrapper().getConsentVariationOrderLabelL();
        assertEquals(CONSENT_ORDER_LOWERCASE_LABEL_VALUE, lowerCaseLabel);
        final String docLabel = response.getData().getConsentOrderWrapper().getOtherDocLabel();
        assertEquals(CONSENT_OTHER_DOC_LABEL_VALUE, docLabel);
    }

    private CallbackRequest buildCallbackRequest() {
        FinremCaseData caseData = new FinremCaseData();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(123L).caseData(caseData).build();
        return CallbackRequest.builder().eventType(EventType.SOLICITOR_CREATE).caseDetails(caseDetails).build();
    }
}