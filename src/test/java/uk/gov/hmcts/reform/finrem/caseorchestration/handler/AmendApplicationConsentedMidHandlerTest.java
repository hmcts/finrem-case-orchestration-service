package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.AMEND_APP_DETAILS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class AmendApplicationConsentedMidHandlerTest {

    @InjectMocks
    private AmendApplicationConsentedMidHandler handler;
    @Mock
    private ConsentOrderService consentOrderService;
    @Mock
    private InternationalPostalService postalService;
    @Mock
    private ObjectMapper objectMapper;

    @Test
    void testCanHandle() {
        assertCanHandle(handler, CallbackType.MID_EVENT, CONSENTED, EventType.AMEND_APP_DETAILS);
    }

    @Test
    void testHandle() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        handler.handle(callbackRequest, AUTH_TOKEN);
        verify(postalService).validate(callbackRequest.getCaseDetails().getData());
    }

    @Test
    void handle() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        handler.handle(callbackRequest, AUTH_TOKEN);
        verify(consentOrderService).performCheck(objectMapper.convertValue(callbackRequest, CallbackRequest.class), AUTH_TOKEN);
        verify(postalService).validate(callbackRequest.getCaseDetails().getData());
    }

    @Test
    void givenConsentedCase_WhenNotEmptyPostCode_thenHandlerWillShowNoErrorMessage() {

        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();

        data.getContactDetailsWrapper().setApplicantAddress(new Address(
            "AddressLine1",
            "AddressLine2",
            "AddressLine3",
            "County",
            "Country",
            "Town",
            "SW1A 1AA"
        ));

        data.getContactDetailsWrapper().setRespondentAddress(new Address(
            "AddressLine1",
            "AddressLine2",
            "AddressLine3",
            "County",
            "Country",
            "Town",
            "SW1A 2AA"
        ));

        data.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.NO);
        data.getContactDetailsWrapper().setConsentedRespondentRepresented(YesOrNo.NO);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(finremCallbackRequest, AUTH_TOKEN);
        assertEquals(0, handle.getErrors().size());
    }

    @Test
    void givenBlankApplicantOrRespondentAddress_thenHandlerWillShowNoErrorMessage() {

        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();

        data.getContactDetailsWrapper().setApplicantResideOutsideUK(YesOrNo.YES);
        data.getContactDetailsWrapper().setApplicantAddress(new Address());
        data.getContactDetailsWrapper().setRespondentResideOutsideUK(YesOrNo.YES);
        data.getContactDetailsWrapper().setRespondentAddress(new Address());

        data.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.NO);
        data.getContactDetailsWrapper().setConsentedRespondentRepresented(YesOrNo.NO);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(finremCallbackRequest, AUTH_TOKEN);
        assertThat(handle.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @NullSource
    @EnumSource(value = YesOrNo.class, names = {"NO"})
    void givenBlankApplicantOrRespondentAddressAndTheyAreLivingOutsideUK_thenHandlerWillShowErrorMessages
        (YesOrNo resideOutsideUK) {

        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();

        data.getContactDetailsWrapper().setApplicantResideOutsideUK(resideOutsideUK);
        data.getContactDetailsWrapper().setApplicantAddress(new Address());
        data.getContactDetailsWrapper().setRespondentResideOutsideUK(resideOutsideUK);
        data.getContactDetailsWrapper().setRespondentAddress(new Address());

        data.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.NO);
        data.getContactDetailsWrapper().setConsentedRespondentRepresented(YesOrNo.NO);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(finremCallbackRequest, AUTH_TOKEN);
        assertThat(handle.getErrors()).containsExactlyInAnyOrder(
            "Postcode field is required for applicant address.",
            "Postcode field is required for respondent address."
        );
    }

    @Test
    void givenConsentedCase_WhenEmptyApplicantPostCode_thenHandlerWillShowMessage() {

        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();

        data.getContactDetailsWrapper().setApplicantAddress(new Address(
                "AddressLine1",
                "AddressLine2",
                "AddressLine3",
                "County",
                "Country",
                "Town",
                null
        ));

        data.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.NO);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertThat(handle.getErrors()).containsExactly("Postcode field is required for applicant address.");
    }

    @Test
    void givenConsentedCase_WhenNullApplicantPostCode_thenHandlerWillShowMessage() {

        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();

        data.getContactDetailsWrapper().setApplicantAddress(new Address(
            "AddressLine1",
            "AddressLine2",
            "AddressLine3",
            "County",
            "Country",
            "Town",
            null
        ));

        data.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.NO);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertThat(handle.getErrors()).containsExactly("Postcode field is required for applicant address.");
    }

    @Test
    void givenConsentedCase_WhenEmptyRespondentPostCode_thenHandlerWillShowMessage() {

        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();

        data.getContactDetailsWrapper().setRespondentAddress(new Address(
            "AddressLine1",
            "AddressLine2",
            "AddressLine3",
            "County",
            "Country",
            "Town",
            null
        ));

        data.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.NO);
        data.getContactDetailsWrapper().setConsentedRespondentRepresented(YesOrNo.NO);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertThat(handle.getErrors()).containsExactly("Postcode field is required for respondent address.");
    }

    @Test
    void givenConsentedCase_WhenNullRespondentPostCode_thenHandlerWillShowMessage() {

        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();

        data.getContactDetailsWrapper().setRespondentAddress(new Address(
            "AddressLine1",
            "AddressLine2",
            "AddressLine3",
            "County",
            "Country",
            "Town",
            null
        ));

        data.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.NO);
        data.getContactDetailsWrapper().setConsentedRespondentRepresented(YesOrNo.NO);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertThat(handle.getErrors()).containsExactly("Postcode field is required for respondent address.");
    }

    @Test
    void givenConsentedCase_WhenEmptyApplicantSolicitorPostCode_thenHandlerWillShowMessage() {

        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();

        data.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.YES);
        data.getContactDetailsWrapper().setSolicitorAddress(new Address(
            "AddressLine1",
            "AddressLine2",
            "AddressLine3",
            "County",
            "Country",
            "Town",
            null
        ));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertThat(handle.getErrors()).containsExactly("Postcode field is required for applicant solicitor address.");
    }

    @Test
    void givenConsentedCase_WhenNullApplicantSolicitorPostCode_thenHandlerWillShowMessage() {

        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();

        data.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.YES);
        data.getContactDetailsWrapper().setSolicitorAddress(new Address(
            "AddressLine1",
            "AddressLine2",
            "AddressLine3",
            "County",
            "Country",
            "Town",
            null
        ));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertThat(handle.getErrors()).containsExactly("Postcode field is required for applicant solicitor address.");
    }

    @Test
    void givenConsentedCase_WhenNullRespondentSolicitorPostCode_thenHandlerWillShowMessage() {

        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();

        data.getContactDetailsWrapper().setConsentedRespondentRepresented(YesOrNo.YES);
        data.getContactDetailsWrapper().setRespondentSolicitorAddress(new Address(
            "AddressLine1",
            "AddressLine2",
            "AddressLine3",
            "County",
            "Country",
            "Town",
            null
        ));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertThat(handle.getErrors()).containsExactly("Postcode field is required for respondent solicitor address.");
    }

    private FinremCallbackRequest buildCallbackRequest() {
        return FinremCallbackRequest
            .builder()
            .eventType(AMEND_APP_DETAILS)
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONSENTED)
                .data(FinremCaseData.builder().ccdCaseType(CONSENTED).build()).build())
            .caseDetailsBefore(FinremCaseDetails.builder().id(123L).caseType(CONSENTED)
                .data(FinremCaseData.builder().ccdCaseType(CONSENTED).build()).build())
            .build();
    }
}
