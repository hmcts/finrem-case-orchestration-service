package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;

import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.MID_EVENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.CLOSE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.UPDATE_CONTACT_DETAILS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@ExtendWith(MockitoExtension.class)
class UpdateContactDetailsContestedMidHandlerTest {
    public static final String AUTH_TOKEN = "tokien:)";
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
        return FinremCallbackRequest
            .builder()
            .eventType(EventType.UPDATE_CONTACT_DETAILS)
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(FinremCaseData.builder().ccdCaseType(CONTESTED).contactDetailsWrapper(new ContactDetailsWrapper()).build()).build())
            .build();
    }

    @Test
    public void givenContestedCase_WhenNotEmptyPostCode_thenHandlerWillShowNoErrorMessage() {

        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();

        Address address = new Address();
        address.setPostCode("AB1 1AB");
        data.getContactDetailsWrapper().setApplicantAddress(address);
        data.getContactDetailsWrapper().setRespondentAddress(address);
        data.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.NO);
        data.getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.NO);

        handler.canHandle(MID_EVENT, CONTESTED, UPDATE_CONTACT_DETAILS);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(finremCallbackRequest, AUTH_TOKEN);
        assertEquals(0, handle.getErrors().size());
    }


    @Test
    public void givenConsentedCase_WhenEmptyApplicantPostCode_thenHandlerWillShowMessage() {

        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();

        Address address = new Address();
        address.setPostCode("");
        data.getContactDetailsWrapper().setApplicantAddress(address);
        data.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.NO);

        handler.canHandle(MID_EVENT, CONTESTED, UPDATE_CONTACT_DETAILS);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertEquals(1, handle.getErrors().size());
        assertTrue(handle.getErrors().contains("Postcode field is required for applicant address."));

    }

    @Test
    public void givenConsentedCase_WhenEmptyRespondentPostCode_thenHandlerWillShowMessage() {

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
        data.getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.NO);

        handler.canHandle(MID_EVENT, CONTESTED, UPDATE_CONTACT_DETAILS);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertEquals(1, handle.getErrors().size());
        assertTrue(handle.getErrors().contains("Postcode field is required for respondent address."));

    }

    @Test
    public void givenConsentedCase_WhenEmptyApplicantSolicitorPostCode_thenHandlerWillShowMessage() {

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

        handler.canHandle(MID_EVENT, CONTESTED, UPDATE_CONTACT_DETAILS);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertEquals(1, handle.getErrors().size());
        assertTrue(handle.getErrors().contains("Postcode field is required for applicant solicitor address."));

    }

    @Test
    public void givenConsentedCase_WhenEmptyRespondentSolicitorPostCode_thenHandlerWillShowMessage() {

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

        handler.canHandle(MID_EVENT, CONTESTED, UPDATE_CONTACT_DETAILS);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertEquals(1, handle.getErrors().size());
        assertTrue(handle.getErrors().contains("Postcode field is required for respondent solicitor address."));

    }
}