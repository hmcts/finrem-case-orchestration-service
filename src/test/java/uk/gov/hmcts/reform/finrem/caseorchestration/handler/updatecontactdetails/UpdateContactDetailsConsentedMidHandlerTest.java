package uk.gov.hmcts.reform.finrem.caseorchestration.handler.updatecontactdetails;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class UpdateContactDetailsConsentedMidHandlerTest {
    @InjectMocks
    private UpdateContactDetailsConsentedMidHandler handler;

    @Mock
    private InternationalPostalService postalService;

    @Test
    void testCanHandle() {
        assertCanHandle(handler, CallbackType.MID_EVENT, CONSENTED, EventType.UPDATE_CONTACT_DETAILS);
    }

    @Test
    void testHandle() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        handler.handle(callbackRequest, AUTH_TOKEN);
        verify(postalService).validate(callbackRequest.getCaseDetails().getData());
    }

    @Test
    void givenConsentedCase_WhenNotEmptyPostCode_thenHandlerWillShowNoErrorMessage() {

        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();

        Address address = new Address();
        address.setPostCode("AB1 1AB");
        data.getContactDetailsWrapper().setApplicantAddress(address);
        data.getContactDetailsWrapper().setRespondentAddress(address);
        data.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.NO);
        data.getContactDetailsWrapper().setConsentedRespondentRepresented(YesOrNo.NO);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(finremCallbackRequest, AUTH_TOKEN);
        assertEquals(0, handle.getErrors().size());
    }

    @Test
    void givenConsentedCase_WhenEmptyApplicantPostCode_thenHandlerWillShowMessage() {

        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();

        Address address = new Address();
        address.setPostCode("");
        data.getContactDetailsWrapper().setApplicantAddress(address);
        data.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.NO);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertThat(handle.getErrors()).containsExactly("Postcode field is required for applicant address.");
    }

    @Test
    void givenConsentedCase_WhenNullApplicantPostCode_thenHandlerWillShowMessage() {

        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();

        Address address = new Address();
        address.setPostCode(null);
        data.getContactDetailsWrapper().setApplicantAddress(address);
        data.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.NO);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertThat(handle.getErrors()).containsExactly("Postcode field is required for applicant address.");
    }

    @Test
    void givenConsentedCase_WhenEmptyRespondentPostCode_thenHandlerWillShowMessage() {

        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();

        Address address = new Address();
        address.setPostCode("AB1 1AB");
        Address addressBlank = new Address();
        addressBlank.setPostCode("");
        data.getContactDetailsWrapper().setApplicantAddress(address);
        data.getContactDetailsWrapper().setRespondentAddress(addressBlank);
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

        Address address = new Address();
        address.setPostCode("AB1 1AB");
        Address addressBlank = new Address();
        addressBlank.setPostCode(null);
        data.getContactDetailsWrapper().setApplicantAddress(address);
        data.getContactDetailsWrapper().setRespondentAddress(addressBlank);
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

        Address address = new Address();
        address.setPostCode("AB1 1AB");
        Address addressBlank = new Address();
        addressBlank.setPostCode("");
        data.getContactDetailsWrapper().setApplicantAddress(address);
        data.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.YES);
        data.getContactDetailsWrapper().setSolicitorAddress(addressBlank);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertThat(handle.getErrors()).containsExactly("Postcode field is required for applicant solicitor address.");
    }

    @Test
    void givenConsentedCase_WhenNullApplicantSolicitorPostCode_thenHandlerWillShowMessage() {

        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();

        Address address = new Address();
        address.setPostCode("AB1 1AB");
        Address addressBlank = new Address();
        addressBlank.setPostCode(null);
        data.getContactDetailsWrapper().setApplicantAddress(address);
        data.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.YES);
        data.getContactDetailsWrapper().setSolicitorAddress(addressBlank);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertThat(handle.getErrors()).containsExactly("Postcode field is required for applicant solicitor address.");
    }

    @Test
    void givenConsentedCase_WhenNullRespondentSolicitorPostCode_thenHandlerWillShowMessage() {

        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();

        Address address = new Address();
        address.setPostCode("AB1 1AB");
        Address addressBlank = new Address();
        addressBlank.setPostCode(null);
        data.getContactDetailsWrapper().setApplicantAddress(address);
        data.getContactDetailsWrapper().setRespondentAddress(address);
        data.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.YES);
        data.getContactDetailsWrapper().setConsentedRespondentRepresented(YesOrNo.YES);
        data.getContactDetailsWrapper().setRespondentSolicitorAddress(addressBlank);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertThat(handle.getErrors()).containsExactly("Postcode field is required for respondent solicitor address.");
    }

    private FinremCallbackRequest buildCallbackRequest() {
        return FinremCallbackRequest
            .builder()
            .eventType(EventType.UPDATE_CONTACT_DETAILS)
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONSENTED)
                .data(FinremCaseData.builder().ccdCaseType(CONSENTED).build()).build())
            .caseDetailsBefore(FinremCaseDetails.builder().id(123L).caseType(CONSENTED)
                .data(FinremCaseData.builder().ccdCaseType(CONSENTED).build()).build())
            .build();
    }
}
