package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.express.ExpressCaseService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.MID_EVENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.NEW_PAPER_CASE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class PaperCaseCreateContestedMidHandlerTest {

    @InjectMocks
    private PaperCaseCreateContestedMidHandler handler;

    @Mock
    ExpressCaseService expressCaseService;
    @Mock
    InternationalPostalService postalService;

    private static final String APPLICANT_POSTCODE_ERROR = "Postcode field is required for applicant address.";
    private static final String RESPONDENT_POSTCODE_ERROR = "Postcode field is required for respondent address.";
    private static final String APPLICANT_SOLICITOR_POSTCODE_ERROR = "Postcode field is required for applicant solicitor address.";
    private static final String RESPONDENT_SOLICITOR_POSTCODE_ERROR = "Postcode field is required for respondent solicitor address.";

    @Test
    void testCanHandle() {
        assertCanHandle(handler, MID_EVENT, CONTESTED, NEW_PAPER_CASE);
    }

    @Test
    void testHandle() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        handler.handle(callbackRequest, AUTH_TOKEN);
        verify(postalService).validate(callbackRequest.getCaseDetails().getData());
    }

    @ParameterizedTest
    @NullSource
    @EnumSource(value = YesOrNo.class, names = {"NO"})
    void givenContestedCase_WhenNotEmptyPostCode_thenHandlerWillShowNoErrorMessage(YesOrNo resideOutsideUK) {

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

        if (resideOutsideUK != null) {
            data.getContactDetailsWrapper().setApplicantResideOutsideUK(resideOutsideUK);
        }
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

        if (resideOutsideUK != null) {
            data.getContactDetailsWrapper().setRespondentResideOutsideUK(resideOutsideUK);
        }
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

        assertThat(handle.getErrors()).containsExactly(APPLICANT_SOLICITOR_POSTCODE_ERROR);
    }

    @ParameterizedTest
    @NullSource
    @EnumSource(value = YesOrNo.class, names = {"NO"})
    void givenContestedCase_WhenEmptyApplicantPostCode_thenHandlerWillShowErrorMessage(YesOrNo resideOutsideUK) {

        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();

        if (resideOutsideUK != null) {
            data.getContactDetailsWrapper().setApplicantResideOutsideUK(resideOutsideUK);
        }
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

        assertThat(handle.getErrors()).containsExactly(APPLICANT_POSTCODE_ERROR);
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

        assertThat(handle.getErrors()).containsExactly(RESPONDENT_SOLICITOR_POSTCODE_ERROR);
    }

    @ParameterizedTest
    @NullSource
    @EnumSource(value = YesOrNo.class, names = {"NO"})
    void givenContestedCase_WhenEmptyRespondentPostCode_thenHandlerWillShowErrorMessage(YesOrNo resideOutsideUK) {

        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();

        if (resideOutsideUK != null) {
            data.getContactDetailsWrapper().setRespondentResideOutsideUK(resideOutsideUK);
        }
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

        assertThat(handle.getErrors()).containsExactlyInAnyOrder(RESPONDENT_POSTCODE_ERROR);
    }

    @Test
    void testGivenExpressPilotEnabled_ThenExpressCaseServiceCalled() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder().caseDetails(
            FinremCaseDetails.builder().data(
                FinremCaseData.builder().ccdCaseType(CONTESTED).build()
            ).build()
        ).build();
        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();

        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(expressCaseService).setExpressCaseEnrollmentStatus(caseData);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void givenContestedCase_WhenEmptyPostCodeForInternationalApplicantAndRespondent_thenHandlerWillShowNoErrorMessage(String nullOrEmptyPostCode) {

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

        data.getContactDetailsWrapper().setApplicantResideOutsideUK(YesOrNo.YES);
        data.getContactDetailsWrapper().setApplicantAddress(new Address(
            "AddressLine1",
            "AddressLine2",
            "AddressLine3",
            "County",
            "Country",
            "Town",
            nullOrEmptyPostCode
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

        data.getContactDetailsWrapper().setRespondentResideOutsideUK(YesOrNo.YES);
        data.getContactDetailsWrapper().setRespondentAddress(new Address(
            "AddressLine1",
            "AddressLine2",
            "AddressLine3",
            "County",
            "Country",
            "Town",
            nullOrEmptyPostCode
        ));

        data.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.YES);
        data.getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.YES);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(finremCallbackRequest, AUTH_TOKEN);
        assertThat(handle.getErrors()).isEmpty();
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
