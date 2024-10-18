package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCaseDetailsBuilderFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;

import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.MID_EVENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.CLOSE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.UPDATE_CONTACT_DETAILS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@ExtendWith(MockitoExtension.class)
class UpdateContactDetailsContestedMidHandlerTest {

    @InjectMocks
    private UpdateContactDetailsContestedMidHandler handler;

    @Mock
    private InternationalPostalService postalService;

    @ParameterizedTest
    @MethodSource
    void testCanHandle(CallbackType callbackType, CaseType caseType, EventType eventType, boolean expected) {
        assertThat(handler.canHandle(callbackType, caseType, eventType)).isEqualTo(expected);
    }

    private static Stream<Arguments> testCanHandle() {
        return Stream.of(
            Arguments.of(MID_EVENT, CONTESTED, CLOSE, false),
            Arguments.of(MID_EVENT, CONTESTED, UPDATE_CONTACT_DETAILS, true),
            Arguments.of(MID_EVENT, CONSENTED, UPDATE_CONTACT_DETAILS, false),
            Arguments.of(SUBMITTED, CONTESTED, CLOSE, false)
        );
    }

    @Test
    void testHandle() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        handler.handle(callbackRequest, AUTH_TOKEN);
        verify(postalService).validate(callbackRequest.getCaseDetails().getData());
    }

    private FinremCallbackRequest buildCallbackRequest() {
        return buildCallbackRequest(false);
    }

    private FinremCallbackRequest buildCallbackRequest(boolean consented) {
        return FinremCallbackRequestFactory.from(EventType.UPDATE_CONTACT_DETAILS,
            FinremCaseDetailsBuilderFactory.from(123L,
                consented ? CONSENTED : CONTESTED));
    }

    @Test
    void givenContestedCase_WhenNotEmptyPostCode_thenHandlerWillShowNoErrorMessage() {

        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();

        Address address = new Address();
        address.setPostCode("AB1 1AB");
        data.getContactDetailsWrapper().setApplicantAddress(address);
        data.getContactDetailsWrapper().setRespondentAddress(address);
        data.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.NO);
        data.getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.NO);

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

        assertEquals(1, handle.getErrors().size());
        assertEquals("Postcode field is required for applicant address.", handle.getErrors().get(0));
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

        assertEquals(1, handle.getErrors().size());
        assertEquals("Postcode field is required for applicant address.", handle.getErrors().get(0));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void givenConsentedCase_WhenEmptyRespondentPostCode_thenHandlerWillShowMessage(boolean consented) {
        
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest(consented);
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();

        data.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.NO);
        data.getContactDetailsWrapper().setApplicantAddress(Address.builder().postCode("AB1 1AB").build());

        if (consented) {
            data.getContactDetailsWrapper().setConsentedRespondentRepresented(YesOrNo.NO);
        } else {
            data.getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.NO);
        }
        data.getContactDetailsWrapper().setRespondentAddress(Address.builder().build());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertEquals(1, handle.getErrors().size());
        assertEquals("Postcode field is required for respondent address.", handle.getErrors().get(0));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void givenConsentedCase_WhenEmptyRespondentPostCodeAndRespondentRepresentedIsTrue_thenHandlerWillNotShowMessage(boolean consented) {

        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest(consented);
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();

        data.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.NO);
        data.getContactDetailsWrapper().setApplicantAddress(Address.builder().postCode("AB1 1AB").build());

        if (consented) {
            data.getContactDetailsWrapper().setConsentedRespondentRepresented(YesOrNo.YES);
        } else {
            data.getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.YES);
        }
        data.getContactDetailsWrapper().setRespondentAddress(Address.builder().build());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertEquals(0, handle.getErrors().size());
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
        data.getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.NO);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertEquals(1, handle.getErrors().size());
        assertEquals("Postcode field is required for respondent address.", handle.getErrors().get(0));
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
        data.getContactDetailsWrapper().setApplicantSolicitorAddress(addressBlank);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertEquals(1, handle.getErrors().size());
        assertEquals("Postcode field is required for applicant solicitor address.", handle.getErrors().get(0));
    }

    @Test
    void givenConsentedCase_WhenEmptyRespondentSolicitorPostCode_thenHandlerWillShowMessage() {

        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();

        Address address = new Address();
        address.setPostCode("AB1 1AB");
        Address addressBlank = new Address();
        addressBlank.setPostCode("");
        data.getContactDetailsWrapper().setApplicantAddress(address);
        data.getContactDetailsWrapper().setRespondentAddress(address);
        data.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.YES);
        data.getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.YES);
        data.getContactDetailsWrapper().setRespondentSolicitorAddress(addressBlank);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertEquals(1, handle.getErrors().size());
        assertEquals("Postcode field is required for respondent solicitor address.", handle.getErrors().get(0));
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
        data.getContactDetailsWrapper().setApplicantSolicitorAddress(addressBlank);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertEquals(1, handle.getErrors().size());
        assertEquals("Postcode field is required for applicant solicitor address.", handle.getErrors().get(0));
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
        data.getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.YES);
        data.getContactDetailsWrapper().setRespondentSolicitorAddress(addressBlank);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertEquals(1, handle.getErrors().size());
        assertEquals("Postcode field is required for respondent solicitor address.", handle.getErrors().get(0));
    }
}
