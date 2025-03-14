package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.assertj.core.api.Assertions;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.MID_EVENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.CLOSE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.NEW_PAPER_CASE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@ExtendWith(MockitoExtension.class)
class PaperCaseCreateContestedMidHandlerTest {
    public static final String AUTH_TOKEN = "tokien:)";
    @InjectMocks
    private PaperCaseCreateContestedMidHandler handler;

    @Mock
    private InternationalPostalService postalService;

    private static final String APPLICANT_POSTCODE_ERROR = "Postcode field is required for applicant address.";
    private static final String RESPONDENT_POSTCODE_ERROR = "Postcode field is required for respondent address.";
    private static final String APPLICANT_SOLICITOR_POSTCODE_ERROR = "Postcode field is required for applicant solicitor address.";
    private static final String RESPONDENT_SOLICITOR_POSTCODE_ERROR = "Postcode field is required for respondent solicitor address.";

    @ParameterizedTest
    @MethodSource
    void testCanHandle(CallbackType callbackType, CaseType caseType, EventType eventType, boolean expected) {
        assertThat(handler.canHandle(callbackType, caseType, eventType)).isEqualTo(expected);
    }

    private static Stream<Arguments> testCanHandle() {
        return Stream.of(
            Arguments.of(MID_EVENT, CONTESTED, CLOSE, false),
            Arguments.of(MID_EVENT, CONTESTED, NEW_PAPER_CASE, true),
            Arguments.of(MID_EVENT, CONSENTED, NEW_PAPER_CASE, false),
            Arguments.of(SUBMITTED, CONTESTED, CLOSE, false)
        );
    }

    @Test
    void testHandle() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        handler.handle(callbackRequest, AUTH_TOKEN);
        verify(postalService).validate(callbackRequest.getCaseDetails().getData());
    }

    @Test
    void givenContestedCase_WhenNotEmptyPostCode_thenHandlerWillShowNoErrorMessage() {

        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();

        data.getContactDetailsWrapper().setApplicantSolicitorAddress(new Address(
            "AddressLine1",
            "AddressLine2",
            "AddressLine3",
            "County",
            "Country",
            "Town",
            "SW1A 1AA"
        ));

        data.getContactDetailsWrapper().setApplicantAddress(new Address(
            "AddressLine1",
            "AddressLine2",
            "AddressLine3",
            "County",
            "Country",
            "Town",
            "SW1A 1AA"
        ));

        data.getContactDetailsWrapper().setRespondentSolicitorAddress(new Address(
            "AddressLine1",
            "AddressLine2",
            "AddressLine3",
            "County",
            "Country",
            "Town",
            "SW1A 2AA"
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

        data.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.YES);
        data.getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.YES);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(finremCallbackRequest, AUTH_TOKEN);
        assertThat(handle.getErrors()).isEmpty();
    }

    @Test
    void givenContestedCase_WhenEmptyApplicantSolicitorPostCode_thenHandlerWillShowErrorMessage() {

        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();

        data.getContactDetailsWrapper().setApplicantSolicitorAddress(new Address());

        data.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.YES);
        data.getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.YES);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        Assertions.assertThat(handle.getErrors())
            .containsExactly(APPLICANT_SOLICITOR_POSTCODE_ERROR);
    }

    @Test
    void givenContestedCase_WhenEmptyApplicantPostCode_thenHandlerWillShowErrorMessage() {

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
            null));

        data.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.NO);
        data.getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.YES);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        Assertions.assertThat(handle.getErrors())
            .containsExactly(APPLICANT_POSTCODE_ERROR);
    }

    @Test
    void givenContestedCase_WhenEmptyRespondentSolicitorPostCode_thenHandlerWillShowErrorMessage() {

        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();

        data.getContactDetailsWrapper().setRespondentSolicitorAddress(new Address());

        data.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.YES);
        data.getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.YES);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        Assertions.assertThat(handle.getErrors())
            .containsExactly(RESPONDENT_SOLICITOR_POSTCODE_ERROR);
    }

    @Test
    void givenContestedCase_WhenEmptyRespondentPostCode_thenHandlerWillShowErrorMessage() {

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

        data.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.YES);
        data.getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.YES);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        Assertions.assertThat(handle.getErrors())
            .containsExactlyInAnyOrder(RESPONDENT_POSTCODE_ERROR);
    }

    private FinremCallbackRequest buildCallbackRequest() {
        return FinremCallbackRequest
            .builder()
            .eventType(EventType.NEW_PAPER_CASE)
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(FinremCaseData.builder().ccdCaseType(CONTESTED).build()).build())
            .build();
    }
}