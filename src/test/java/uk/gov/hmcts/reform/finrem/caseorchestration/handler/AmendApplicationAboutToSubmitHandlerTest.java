package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.OngoingStubbing;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderService;

import java.io.InputStream;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;

@RunWith(MockitoJUnitRunner.class)
public class AmendApplicationAboutToSubmitHandlerTest {

    private static final String AUTH_TOKEN = "4d73f8d4-2a8d-48e2-af91-11cbaa642345";
    private static final String PERIODIC_PAYMENT_CHILD_JSON = "/fixtures/updatecase/amend-periodic-payment-order.json";
    private static final String PERIODIC_PAYMENT_JSON = "/fixtures/updatecase/amend-periodic-payment-order-without"
            + "-agreement.json";
    private static final String PROPERTY_ADJ_JSON = "/fixtures/updatecase/amend-property-adjustment-details.json";
    private static final String PROPERTY_DETAILS_JSON = "/fixtures/updatecase/remove-property-adjustment-details.json";
    private static final String DECREE_NISI_JSON = "/fixtures/updatecase/amend-divorce-details-decree-nisi.json";
    private static final String DECREE_ABS_JSON = "/fixtures/updatecase/amend-divorce-details-decree-absolute.json";
    private static final String D81_JOINT_JSON = "/fixtures/updatecase/amend-divorce-details-d81-joint.json";
    private static final String D81_INDIVIUAL_JSON = "/fixtures/updatecase/amend-divorce-details-d81-individual.json";
    private static final String PAYMENT_UNCHECKED_JSON = "/fixtures/updatecase/amend-remove-periodic-payment-order.json";
    private static final String RES_SOL_JSON = "/fixtures/updatecase/remove-respondent-solicitor-details.json";


    @InjectMocks
    private AmendApplicationAboutToSubmitHandler handler;
    @Mock
    private ConsentOrderService consentOrderService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void givenCase_whenEventIsAmendApplication_thenCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.AMEND_APP_DETAILS),
            is(true));
    }

    @Test
    public void given_case_when_wrong_callback_then_case_can_not_handle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.AMEND_APP_DETAILS),
            is(false));
    }

    @Test
    public void given_case_when_wrong_casetype_then_case_can_not_handle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.AMEND_APP_DETAILS),
            is(false));
    }

    @Test
    public void given_case_when_wrong_eventtype_then_case_can_not_handle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.CLOSE),
            is(false));
    }


    @Test
    public void givenCase_whenSolicitorChooseToDecreeAbsolute_thenShouldDeleteDecreeNisi() {
        CallbackRequest callbackRequest = doValidCaseDataSetUp(DECREE_NISI_JSON);
        whenServiceGeneratesDocument().thenReturn(caseDocument(DOC_URL, FILE_NAME, BINARY_URL));

        AboutToStartOrSubmitCallbackResponse response = handler.handle(callbackRequest, AUTH_TOKEN);

        final Map<String, Object> responseData = response.getData();
        assertNull(responseData.get("divorceUploadEvidence2"));
        assertNull(responseData.get("divorceDecreeAbsoluteDate"));
    }

    @Test
    public void givenCase_whenSolicitorChooseToDecreeNisi_thenShouldDeleteDecreeAbsolute() {
        CallbackRequest callbackRequest = doValidCaseDataSetUp(DECREE_ABS_JSON);
        whenServiceGeneratesDocument().thenReturn(caseDocument(DOC_URL, FILE_NAME, BINARY_URL));

        AboutToStartOrSubmitCallbackResponse response = handler.handle(callbackRequest, AUTH_TOKEN);

        final Map<String, Object> responseData = response.getData();
        assertNull(responseData.get("divorceUploadEvidence1"));
        assertNull(responseData.get("divorceDecreeNisiDate"));
    }

    @Test
    public void givenCase_whenCaseUpdated_thenShouldDeleteD81IndividualData() {
        CallbackRequest callbackRequest = doValidCaseDataSetUp(D81_JOINT_JSON);
        whenServiceGeneratesDocument().thenReturn(caseDocument(DOC_URL, FILE_NAME, BINARY_URL));

        AboutToStartOrSubmitCallbackResponse response = handler.handle(callbackRequest, AUTH_TOKEN);

        final Map<String, Object> responseData = response.getData();
        assertNull(responseData.get("d81Applicant"));
        assertNull(responseData.get("d81Respondent"));
        assertNotNull(responseData.get("d81Joint"));
    }

    @Test
    public void givenCase_whenCaseUpdated_thenShouldDeleteD81JointData() {
        CallbackRequest callbackRequest = doValidCaseDataSetUp(D81_INDIVIUAL_JSON);
        whenServiceGeneratesDocument().thenReturn(caseDocument(DOC_URL, FILE_NAME, BINARY_URL));

        AboutToStartOrSubmitCallbackResponse response = handler.handle(callbackRequest, AUTH_TOKEN);

        final Map<String, Object> responseData = response.getData();
        assertNull(responseData.get("d81Joint"));
        assertNotNull(responseData.get("d81Applicant"));
        assertNotNull(responseData.get("d81Respondent"));
    }

    @Test
    public void givenCase_whenCaseUpdated_thenShouldDeletePropertyDetails() {
        CallbackRequest callbackRequest = doValidCaseDataSetUp(PROPERTY_DETAILS_JSON);
        whenServiceGeneratesDocument().thenReturn(caseDocument(DOC_URL, FILE_NAME, BINARY_URL));

        AboutToStartOrSubmitCallbackResponse response = handler.handle(callbackRequest, AUTH_TOKEN);

        final Map<String, Object> responseData = response.getData();
        assertNull(responseData.get("natureOfApplication3a"));
        assertNull(responseData.get("natureOfApplication3b"));
    }

    @Test
    public void givenCase_whenCaseUpdated_thenShouldNotRemovePropertyDetails() {
        CallbackRequest callbackRequest = doValidCaseDataSetUp(PROPERTY_ADJ_JSON);
        whenServiceGeneratesDocument().thenReturn(caseDocument(DOC_URL, FILE_NAME, BINARY_URL));

        AboutToStartOrSubmitCallbackResponse response = handler.handle(callbackRequest, AUTH_TOKEN);

        final Map<String, Object> responseData = response.getData();
        assertNotNull(responseData.get("natureOfApplication3a"));
        assertNotNull(responseData.get("natureOfApplication3b"));
    }

    @Test
    public void givenCase_whenCaseUpdated_thenShouldDeletePeriodicPaymentDetailsWithOutWrittenAgreement() {
        CallbackRequest callbackRequest = doValidCaseDataSetUp(PERIODIC_PAYMENT_JSON);
        whenServiceGeneratesDocument().thenReturn(caseDocument(DOC_URL, FILE_NAME, BINARY_URL));

        AboutToStartOrSubmitCallbackResponse response = handler.handle(callbackRequest, AUTH_TOKEN);

        final Map<String, Object> responseData = response.getData();
        assertNotNull(responseData.get("natureOfApplication6"));
        assertNotNull(responseData.get("natureOfApplication7"));
    }

    @Test
    public void givenCase_whenCaseUpdated_thenShouldDeletePeriodicPaymentDetailsWithWrittenAgreementForChildren() {
        CallbackRequest callbackRequest = doValidCaseDataSetUp(PERIODIC_PAYMENT_CHILD_JSON);
        whenServiceGeneratesDocument().thenReturn(caseDocument(DOC_URL, FILE_NAME, BINARY_URL));

        AboutToStartOrSubmitCallbackResponse response = handler.handle(callbackRequest, AUTH_TOKEN);

        final Map<String, Object> responseData = response.getData();
        assertNull(responseData.get("natureOfApplication6"));
        assertNull(responseData.get("natureOfApplication7"));
    }

    @Test
    public void givenCase_whenCaseUpdated_thenShouldDeletePeriodicPaymentDetailsIfUnchecked() {
        CallbackRequest callbackRequest = doValidCaseDataSetUp(PAYMENT_UNCHECKED_JSON);
        whenServiceGeneratesDocument().thenReturn(caseDocument(DOC_URL, FILE_NAME, BINARY_URL));

        AboutToStartOrSubmitCallbackResponse response = handler.handle(callbackRequest, AUTH_TOKEN);

        final Map<String, Object> responseData = response.getData();
        assertNull(responseData.get("natureOfApplication5"));
        assertNull(responseData.get("natureOfApplication6"));
        assertNull(responseData.get("natureOfApplication7"));
        assertNull(responseData.get("orderForChildrenQuestion1"));
    }

    @Test
    public void givenCase_whenIfRespondentNotRepresentedBySolicitor_thenShouldDeleteRespondentSolicitorDetails() {
        CallbackRequest callbackRequest = doValidCaseDataSetUp(RES_SOL_JSON);
        whenServiceGeneratesDocument().thenReturn(caseDocument(DOC_URL, FILE_NAME, BINARY_URL));

        AboutToStartOrSubmitCallbackResponse response = handler.handle(callbackRequest, AUTH_TOKEN);

        final Map<String, Object> responseData = response.getData();
        assertNull(responseData.get("rSolicitorFirm"));
        assertNull(responseData.get("rSolicitorName"));
        assertNull(responseData.get("rSolicitorReference"));
        assertNull(responseData.get("rSolicitorAddress"));
        assertNull(responseData.get("rSolicitorDXnumber"));
        assertNull(responseData.get("rSolicitorEmail"));
        assertNull(responseData.get("rSolicitorPhone"));
    }

    private CallbackRequest doValidCaseDataSetUp(final String path)  {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(path)) {
            return objectMapper.readValue(resourceAsStream, CallbackRequest.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private OngoingStubbing<CaseDocument> whenServiceGeneratesDocument() {
        return when(consentOrderService.getLatestConsentOrderData(isA(CallbackRequest.class)));
    }
}