package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.lenient;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.newDocument;

@RunWith(MockitoJUnitRunner.class)
public class AmendApplicationAboutToSubmitHandlerTest extends BaseHandlerTest {

    private static final String AUTH_TOKEN = "4d73f8d4-2a8d-48e2-af91-11cbaa642345";
    private static final String PERIODIC_PAYMENT_CHILD_JSON = "/fixtures/updatecase/amend-periodic-payment-order.json";
    private static final String PERIODIC_PAYMENT_JSON = "/fixtures/updatecase/amend-periodic-payment-order-without"
        + "-agreement-with-valid-enums.json";
    private static final String PROPERTY_ADJ_JSON = "/fixtures/updatecase/amend-property-adjustment-details.json";
    private static final String PROPERTY_DETAILS_JSON = "/fixtures/updatecase/remove-property-adjustment-details.json";
    private static final String DECREE_NISI_JSON = "/fixtures/updatecase/amend-divorce-details-decree-nisi.json";
    private static final String DECREE_ABS_JSON = "/fixtures/updatecase/amend-divorce-details-decree-absolute.json";
    private static final String D81_JOINT_JSON = "/fixtures/updatecase/amend-divorce-details-d81-joint.json";
    private static final String D81_INDIVIUAL_JSON = "/fixtures/updatecase/amend-divorce-details-d81-individual.json";
    private static final String PAYMENT_UNCHECKED_JSON = "/fixtures/updatecase/amend-remove-periodic-payment-order.json";
    private static final String RES_SOL_JSON = "/fixtures/updatecase/remove-respondent-solicitor-details.json";
    private static final String APP_SOL_JSON = "/fixtures/updatecase/remove-applicant-solicitor-details.json";


    private AmendApplicationAboutToSubmitHandler handler;
    @Mock
    private ConsentOrderService consentOrderService;

    @Before
    public void setUp() {
        FinremCaseDetailsMapper finremCaseDetailsMapper = new FinremCaseDetailsMapper(new ObjectMapper().registerModule(new JavaTimeModule()));
        handler = new AmendApplicationAboutToSubmitHandler(finremCaseDetailsMapper,
            consentOrderService);
        lenient().when(consentOrderService.getLatestConsentOrderData(isA(CallbackRequest.class)))
            .thenReturn(newDocument(DOC_URL, BINARY_URL, FILE_NAME));
    }


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

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        final FinremCaseData responseData = response.getData();
        assertNull(responseData.getDivorceUploadEvidence2());
        assertNull(responseData.getDivorceDecreeAbsoluteDate());
    }

    @Test
    public void givenCase_whenSolicitorChooseToDecreeNisi_thenShouldDeleteDecreeAbsolute() {
        CallbackRequest callbackRequest = doValidCaseDataSetUp(DECREE_ABS_JSON);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        final FinremCaseData responseData = response.getData();
        assertNull(responseData.getDivorceUploadEvidence1());
        assertNull(responseData.getDivorceDecreeNisiDate());
    }

    @Test
    public void givenCase_whenCaseUpdated_thenShouldDeleteD81IndividualData() {
        CallbackRequest callbackRequest = doValidCaseDataSetUp(D81_JOINT_JSON);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        final FinremCaseData responseData = response.getData();
        assertNull(responseData.getD81Applicant());
        assertNull(responseData.getD81Respondent());
        assertNotNull(responseData.getD81Joint());
    }

    @Test
    public void givenCase_whenCaseUpdated_thenShouldDeleteD81JointData() {
        CallbackRequest callbackRequest = doValidCaseDataSetUp(D81_INDIVIUAL_JSON);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        final FinremCaseData responseData = response.getData();
        assertNull(responseData.getD81Joint());
        assertNotNull(responseData.getD81Applicant());
        assertNotNull(responseData.getD81Respondent());
    }

    @Test
    public void givenCase_whenCaseUpdated_thenShouldDeletePropertyDetails() {
        CallbackRequest callbackRequest = doValidCaseDataSetUp(PROPERTY_DETAILS_JSON);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        final FinremCaseData responseData = response.getData();
        assertNull(responseData.getNatureApplicationWrapper().getNatureOfApplication3a());
        assertNull(responseData.getNatureApplicationWrapper().getNatureOfApplication3b());
    }

    @Test
    public void givenCase_whenCaseUpdated_thenShouldNotRemovePropertyDetails() {
        CallbackRequest callbackRequest = doValidCaseDataSetUp(PROPERTY_ADJ_JSON);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        final FinremCaseData responseData = response.getData();
        assertNotNull(responseData.getNatureApplicationWrapper().getNatureOfApplication3a());
        assertNotNull(responseData.getNatureApplicationWrapper().getNatureOfApplication3b());
    }

    @Test
    public void givenCase_whenCaseUpdated_thenShouldDeletePeriodicPaymentDetailsWithOutWrittenAgreement() {
        CallbackRequest callbackRequest =
            doValidCaseDataSetUp(PERIODIC_PAYMENT_JSON);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        final FinremCaseData responseData = response.getData();
        assertNotNull(responseData.getNatureApplicationWrapper().getNatureOfApplication6());
        assertNotNull(responseData.getNatureApplicationWrapper().getNatureOfApplication7());
    }

    @Test
    public void givenCase_whenCaseUpdated_thenShouldDeletePeriodicPaymentDetailsWithWrittenAgreementForChildren() {
        CallbackRequest callbackRequest = doValidCaseDataSetUp(PERIODIC_PAYMENT_CHILD_JSON);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        final FinremCaseData responseData = response.getData();
        assertNull(responseData.getNatureApplicationWrapper().getNatureOfApplication6());
        assertNull(responseData.getNatureApplicationWrapper().getNatureOfApplication7());
    }

    @Test
    public void givenCase_whenCaseUpdated_thenShouldDeletePeriodicPaymentDetailsIfUnchecked() {
        CallbackRequest callbackRequest = doValidCaseDataSetUp(PAYMENT_UNCHECKED_JSON);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        final FinremCaseData responseData = response.getData();
        assertNull(responseData.getNatureApplicationWrapper().getNatureOfApplication5());
        assertNull(responseData.getNatureApplicationWrapper().getNatureOfApplication6());
        assertNull(responseData.getNatureApplicationWrapper().getNatureOfApplication7());
        assertNull(responseData.getNatureApplicationWrapper().getOrderForChildrenQuestion1());
    }

    @Test
    public void givenCase_whenIfRespondentNotRepresentedBySolicitor_thenShouldDeleteRespondentSolicitorDetails() {
        CallbackRequest callbackRequest = doValidCaseDataSetUp(RES_SOL_JSON);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        final FinremCaseData responseData = response.getData();
        assertNull(responseData.getContactDetailsWrapper().getRespondentSolicitorFirm());
        assertNull(responseData.getContactDetailsWrapper().getRespondentSolicitorName());
        assertNull(responseData.getContactDetailsWrapper().getRespondentSolicitorReference());
        assertNull(responseData.getContactDetailsWrapper().getRespondentSolicitorAddress());
        assertNull(responseData.getContactDetailsWrapper().getRespondentSolicitorDxNumber());
        assertNull(responseData.getContactDetailsWrapper().getRespondentSolicitorEmail());
        assertNull(responseData.getContactDetailsWrapper().getRespondentSolicitorPhone());
    }


    @Test
    public void givenCase_whenApplicantNotRepresentedBySolicitor_thenShouldDeleteApplicantSolicitorDetails() {
        CallbackRequest callbackRequest = doValidCaseDataSetUp(APP_SOL_JSON);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        final FinremCaseData responseData = response.getData();
        assertNull(responseData.getContactDetailsWrapper().getSolicitorFirm());
        assertNull(responseData.getContactDetailsWrapper().getSolicitorName());
        assertNull(responseData.getContactDetailsWrapper().getSolicitorReference());
        assertNull(responseData.getContactDetailsWrapper().getSolicitorAddress());
        assertNull(responseData.getContactDetailsWrapper().getSolicitorDxNumber());
        assertNull(responseData.getContactDetailsWrapper().getSolicitorEmail());
        assertNull(responseData.getContactDetailsWrapper().getSolicitorPhone());
    }

    private CallbackRequest doValidCaseDataSetUp(final String path) {
        try {
            return getCallbackRequestFromResource(path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}