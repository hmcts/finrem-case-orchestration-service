package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.lenient;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.newDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class AmendApplicationAboutToSubmitHandlerTest extends BaseHandlerTestSetup {

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

    private AmendApplicationAboutToSubmitHandler underTest;

    @Mock
    private ConsentOrderService consentOrderService;

    @BeforeEach
    public void setUp() {
        FinremCaseDetailsMapper finremCaseDetailsMapper = new FinremCaseDetailsMapper(new ObjectMapper().registerModule(new JavaTimeModule()));
        underTest = new AmendApplicationAboutToSubmitHandler(finremCaseDetailsMapper,
            consentOrderService);
        lenient().when(consentOrderService.getLatestConsentOrderData(isA(CallbackRequest.class)))
            .thenReturn(newDocument(DOC_URL, BINARY_URL, FILE_NAME));
    }

    @Test
    void givenCase_whenEventIsAmendApplication_thenCanHandle() {
        assertCanHandle(underTest, CallbackType.ABOUT_TO_SUBMIT, CONSENTED, EventType.AMEND_APP_DETAILS);
    }

    @Test
    void givenCase_whenSolicitorChooseToDecreeAbsolute_thenShouldDeleteDecreeNisi() {
        CallbackRequest callbackRequest = doValidCaseDataSetUp(DECREE_NISI_JSON);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = underTest.handle(callbackRequest, AUTH_TOKEN);

        final FinremCaseData responseData = response.getData();
        assertNull(responseData.getDivorceUploadEvidence2());
        assertNull(responseData.getDivorceDecreeAbsoluteDate());
    }

    @Test
    void givenCase_whenSolicitorChooseToDecreeNisi_thenShouldDeleteDecreeAbsolute() {
        CallbackRequest callbackRequest = doValidCaseDataSetUp(DECREE_ABS_JSON);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = underTest.handle(callbackRequest, AUTH_TOKEN);

        final FinremCaseData responseData = response.getData();
        assertNull(responseData.getDivorceUploadEvidence1());
        assertNull(responseData.getDivorceDecreeNisiDate());
    }

    @Test
    void givenCase_whenCaseUpdated_thenShouldDeleteD81IndividualData() {
        CallbackRequest callbackRequest = doValidCaseDataSetUp(D81_JOINT_JSON);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = underTest.handle(callbackRequest, AUTH_TOKEN);

        final FinremCaseData responseData = response.getData();
        assertNull(responseData.getD81Applicant());
        assertNull(responseData.getD81Respondent());
        assertNotNull(responseData.getD81Joint());
    }

    @Test
    void givenCase_whenCaseUpdated_thenShouldDeleteD81JointData() {
        CallbackRequest callbackRequest = doValidCaseDataSetUp(D81_INDIVIUAL_JSON);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = underTest.handle(callbackRequest, AUTH_TOKEN);

        final FinremCaseData responseData = response.getData();
        assertNull(responseData.getD81Joint());
        assertNotNull(responseData.getD81Applicant());
        assertNotNull(responseData.getD81Respondent());
    }

    @Test
    void givenCase_whenCaseUpdated_thenShouldDeletePropertyDetails() {
        CallbackRequest callbackRequest = doValidCaseDataSetUp(PROPERTY_DETAILS_JSON);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = underTest.handle(callbackRequest, AUTH_TOKEN);

        final FinremCaseData responseData = response.getData();
        assertNull(responseData.getNatureApplicationWrapper().getNatureOfApplication3a());
        assertNull(responseData.getNatureApplicationWrapper().getNatureOfApplication3b());
    }

    @Test
    void givenCase_whenCaseUpdated_thenShouldNotRemovePropertyDetails() {
        CallbackRequest callbackRequest = doValidCaseDataSetUp(PROPERTY_ADJ_JSON);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = underTest.handle(callbackRequest, AUTH_TOKEN);

        final FinremCaseData responseData = response.getData();
        assertNotNull(responseData.getNatureApplicationWrapper().getNatureOfApplication3a());
        assertNotNull(responseData.getNatureApplicationWrapper().getNatureOfApplication3b());
    }

    @Test
    void givenCase_whenCaseUpdated_thenShouldDeletePeriodicPaymentDetailsWithOutWrittenAgreement() {
        CallbackRequest callbackRequest =
            doValidCaseDataSetUp(PERIODIC_PAYMENT_JSON);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = underTest.handle(callbackRequest, AUTH_TOKEN);

        final FinremCaseData responseData = response.getData();
        assertNotNull(responseData.getNatureApplicationWrapper().getNatureOfApplication6());
        assertNotNull(responseData.getNatureApplicationWrapper().getNatureOfApplication7());
    }

    @Test
    void givenCase_whenCaseUpdated_thenShouldDeletePeriodicPaymentDetailsWithWrittenAgreementForChildren() {
        CallbackRequest callbackRequest = doValidCaseDataSetUp(PERIODIC_PAYMENT_CHILD_JSON);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = underTest.handle(callbackRequest, AUTH_TOKEN);

        final FinremCaseData responseData = response.getData();
        assertNull(responseData.getNatureApplicationWrapper().getNatureOfApplication6());
        assertNull(responseData.getNatureApplicationWrapper().getNatureOfApplication7());
    }

    @Test
    void givenCase_whenCaseUpdated_thenShouldDeletePeriodicPaymentDetailsIfUnchecked() {
        CallbackRequest callbackRequest = doValidCaseDataSetUp(PAYMENT_UNCHECKED_JSON);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = underTest.handle(callbackRequest, AUTH_TOKEN);

        final FinremCaseData responseData = response.getData();
        assertNull(responseData.getNatureApplicationWrapper().getNatureOfApplication5());
        assertNull(responseData.getNatureApplicationWrapper().getNatureOfApplication6());
        assertNull(responseData.getNatureApplicationWrapper().getNatureOfApplication7());
        assertNull(responseData.getNatureApplicationWrapper().getOrderForChildrenQuestion1());
    }

    @Test
    void givenCase_whenIfRespondentNotRepresentedBySolicitor_thenShouldDeleteRespondentSolicitorDetails() {
        CallbackRequest callbackRequest = doValidCaseDataSetUp(RES_SOL_JSON);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = underTest.handle(callbackRequest, AUTH_TOKEN);

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
    void givenCase_whenApplicantNotRepresentedBySolicitor_thenShouldDeleteApplicantSolicitorDetails() {
        CallbackRequest callbackRequest = doValidCaseDataSetUp(APP_SOL_JSON);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = underTest.handle(callbackRequest, AUTH_TOKEN);

        final FinremCaseData responseData = response.getData();
        assertNull(responseData.getContactDetailsWrapper().getSolicitorFirm());
        assertNull(responseData.getContactDetailsWrapper().getSolicitorName());
        assertNull(responseData.getContactDetailsWrapper().getSolicitorReference());
        assertNull(responseData.getContactDetailsWrapper().getSolicitorAddress());
        assertNull(responseData.getContactDetailsWrapper().getSolicitorDxNumber());
        assertNull(responseData.getContactDetailsWrapper().getSolicitorEmail());
        assertNull(responseData.getContactDetailsWrapper().getSolicitorPhone());
    }

    @Test
    void givenNullPostCode_whenApplicantAndRespondentNotRepresentedBySolicitor_thenHandlerThrowError() {
        // Arrange
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        Address address = new Address();
        address.setCounty("West Yorkshire");
        finremCaseData.getContactDetailsWrapper().setApplicantAddress(address);
        finremCaseData.getContactDetailsWrapper().setRespondentAddress(address);
        finremCaseData.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.NO);
        finremCaseData.getContactDetailsWrapper().setConsentedRespondentRepresented(YesOrNo.NO);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = underTest.handle(finremCallbackRequest, AUTH_TOKEN);

        assertEquals(2, response.getErrors().size());
        assertEquals("Postcode field is required for applicant address.", response.getErrors().get(0));
        assertEquals("Postcode field is required for respondent address.", response.getErrors().get(1));
    }

    @Test
    void givenValidPostCode_whenApplicantAndRespondentNotRepresentedBySolicitor_thenHandlerThrowNoErrors() {
        // Arrange
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        Address address = new Address();
        address.setCounty("West Yorkshire");
        address.setPostCode("BD1 1BE");
        finremCaseData.getContactDetailsWrapper().setApplicantAddress(address);
        finremCaseData.getContactDetailsWrapper().setRespondentAddress(address);
        finremCaseData.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.NO);
        finremCaseData.getContactDetailsWrapper().setConsentedRespondentRepresented(YesOrNo.NO);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = underTest.handle(finremCallbackRequest, AUTH_TOKEN);

        assertEquals(0, response.getErrors().size());
    }

    @Test
    void givenNullPostCode_whenApplicantAndRespondentRepresentedBySolicitor_thenHandlerThrowError() {
        // Arrange
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        Address address = new Address();
        address.setCounty("West Yorkshire");
        finremCaseData.getContactDetailsWrapper().setApplicantSolicitorAddress(address);
        finremCaseData.getContactDetailsWrapper().setRespondentSolicitorAddress(address);
        finremCaseData.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.YES);
        finremCaseData.getContactDetailsWrapper().setConsentedRespondentRepresented(YesOrNo.YES);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = underTest.handle(finremCallbackRequest, AUTH_TOKEN);

        assertEquals(2, response.getErrors().size());
        assertEquals("Postcode field is required for applicant address.", response.getErrors().get(0));
        assertEquals("Postcode field is required for respondent address.", response.getErrors().get(1));
    }

    @Test
    void givenValidPostCode_whenApplicantAndRespondentRepresentedBySolicitor_thenHandlerThrowNoErrors() {
        // Arrange
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        Address address = new Address();
        address.setCounty("West Yorkshire");
        address.setPostCode("BD1 1BE");
        finremCaseData.getContactDetailsWrapper().setApplicantSolicitorAddress(address);
        finremCaseData.getContactDetailsWrapper().setRespondentSolicitorAddress(address);
        finremCaseData.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.YES);
        finremCaseData.getContactDetailsWrapper().setConsentedRespondentRepresented(YesOrNo.YES);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = underTest.handle(finremCallbackRequest, AUTH_TOKEN);

        assertEquals(0, response.getErrors().size());
    }

    @Test
    void givenEmptyPostCode_whenApplicantAndRespondentRepresentedBySolicitor_thenHandlerThrowError() {
        // Arrange
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        Address address = new Address();
        address.setCounty("West Yorkshire");
        address.setPostCode(" ");
        finremCaseData.getContactDetailsWrapper().setApplicantSolicitorAddress(address);
        finremCaseData.getContactDetailsWrapper().setRespondentSolicitorAddress(address);
        finremCaseData.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.YES);
        finremCaseData.getContactDetailsWrapper().setConsentedRespondentRepresented(YesOrNo.YES);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = underTest.handle(finremCallbackRequest, AUTH_TOKEN);

        assertEquals(2, response.getErrors().size());
        assertEquals("Postcode field is required for applicant address.", response.getErrors().get(0));
        assertEquals("Postcode field is required for respondent address.", response.getErrors().get(1));
    }

    private CallbackRequest doValidCaseDataSetUp(final String path) {
        try {
            return getCallbackRequestFromResource(path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private FinremCallbackRequest buildCallbackRequest() {
        return buildConsentCallbackRequest(EventType.AMEND_APP_DETAILS);
    }
}
