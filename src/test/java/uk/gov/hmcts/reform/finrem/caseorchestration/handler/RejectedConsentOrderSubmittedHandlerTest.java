package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder.ConsentOrderNotApprovedCorresponder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_REFERENCE;

@RunWith(MockitoJUnitRunner.class)
public class RejectedConsentOrderSubmittedHandlerTest {

    public static final String AUTH_TOKEN = "tokien:)";

    @InjectMocks
    private RejectedConsentOrderSubmittedHandler handler;

    @Mock
    private ConsentOrderNotApprovedCorresponder consentOrderNotApprovedCorresponder;

    @Test
    public void givenACcdCallbackConsentCase_WhenSubmitEventRejectOrder_thenHandlerCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONSENTED, EventType.REJECT_ORDER),
            is(true));
    }

    @Test
    public void givenACcdCallbackConsentedCase_WhenAnAboutToSubmitCallback_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.REJECT_ORDER),
            is(false));
    }

    @Test
    public void givenACcdCallbackConsentedCase_WhenContestedCaseType_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.REJECT_ORDER),
            is(false));
    }

    @Test
    public void givenACcdCallbackConsentedCase_WhenEventIsClose_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONSENTED, EventType.CLOSE),
            is(false));
    }

    @Test
    public void givenConsentOrderCase_sendConsentOrderNotApprovedCorrespondence() {
        CallbackRequest callbackRequest = getConsentedCallbackRequestForConsentOrder();
        handler.handle(callbackRequest, AUTH_TOKEN);
        verify(consentOrderNotApprovedCorresponder).sendCorrespondence(any());
    }


    protected CallbackRequest getConsentedCallbackRequestForConsentOrder() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(SOLICITOR_EMAIL, TEST_SOLICITOR_EMAIL);
        caseData.put(CONSENTED_SOLICITOR_NAME, TEST_SOLICITOR_NAME);
        caseData.put(SOLICITOR_REFERENCE, TEST_SOLICITOR_REFERENCE);
        caseData.put(RESP_SOLICITOR_EMAIL, TEST_RESP_SOLICITOR_EMAIL);
        caseData.put(RESP_SOLICITOR_NAME, TEST_RESP_SOLICITOR_NAME);
        caseData.put(APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONSENTED, YES_VALUE);
        caseData.put(RESP_SOLICITOR_REFERENCE, TEST_RESP_SOLICITOR_REFERENCE);
        caseData.put(CONSENTED_RESPONDENT_REPRESENTED, YES_VALUE);
        caseData.put(RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT, YES_VALUE);

        caseData.put(DIVORCE_CASE_NUMBER, TEST_DIVORCE_CASE_NUMBER);
        List<String> natureOfApplication = List.of("Lump Sum Order",
            "Periodical Payment Order",
            "Pension Sharing Order",
            "Pension Attachment Order",
            "Pension Compensation Sharing Order",
            "Pension Compensation Attachment Order",
            "A settlement or a transfer of property",
            "Property Adjustment Order");
        caseData.put("natureOfApplication2", natureOfApplication);
        CaseDetails caseDetails = CaseDetails.builder()
            .caseTypeId(uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED.getCcdType())
            .id(12345L)
            .build();
        caseDetails.setData(caseData);
        return CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
    }

    protected CallbackRequest getConsentedCallbackRequestForVariationOrder() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(SOLICITOR_EMAIL, TEST_SOLICITOR_EMAIL);
        caseData.put(CONSENTED_SOLICITOR_NAME, TEST_SOLICITOR_NAME);
        caseData.put(SOLICITOR_REFERENCE, TEST_SOLICITOR_REFERENCE);
        caseData.put(RESP_SOLICITOR_EMAIL, TEST_RESP_SOLICITOR_EMAIL);
        caseData.put(RESP_SOLICITOR_NAME, TEST_RESP_SOLICITOR_NAME);
        caseData.put(RESP_SOLICITOR_REFERENCE, TEST_RESP_SOLICITOR_REFERENCE);
        caseData.put(DIVORCE_CASE_NUMBER, TEST_DIVORCE_CASE_NUMBER);
        List<String> natureOfApplication = List.of("Lump Sum Order",
            "Periodical Payment Order",
            "Pension Sharing Order",
            "Pension Attachment Order",
            "Pension Compensation Sharing Order",
            "Pension Compensation Attachment Order",
            "A settlement or a transfer of property",
            "Variation Order",
            "Property Adjustment Order");
        caseData.put("natureOfApplication2", natureOfApplication);
        CaseDetails caseDetails = CaseDetails.builder()
            .caseTypeId(uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED.getCcdType())
            .id(12345L)
            .build();
        caseDetails.setData(caseData);
        CallbackRequest genericCallbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        return genericCallbackRequest;
    }
}