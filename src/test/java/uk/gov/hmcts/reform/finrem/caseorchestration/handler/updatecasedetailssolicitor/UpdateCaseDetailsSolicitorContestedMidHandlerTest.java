package uk.gov.hmcts.reform.finrem.caseorchestration.handler.updatecasedetailssolicitor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ContactDetailsValidator;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.MID_EVENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.UPDATE_CASE_DETAILS_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class UpdateCaseDetailsSolicitorContestedMidHandlerTest {

    @InjectMocks
    private UpdateCaseDetailsSolicitorContestedMidHandler underTest;

    @Mock
    private UpdateRepresentationService updateRepresentationService;

    @Test
    void testCanHandle() {
        assertCanHandle(underTest,
            Arguments.of(MID_EVENT, CONTESTED, UPDATE_CASE_DETAILS_SOLICITOR),
            Arguments.of(MID_EVENT, CONSENTED, UPDATE_CASE_DETAILS_SOLICITOR)
        );
    }


    @ParameterizedTest
    @EnumSource(value = CaseRole.class, mode = EnumSource.Mode.EXCLUDE,  names = {"APP_SOLICITOR", "RESP_SOLICITOR"})
    /*
     All CaseRoles other that applicant and respondent solicitor should fail.
     */
    void when_CaseRole_party_wrong_then_exceptionThrown(CaseRole caseRole) {

        FinremCaseData caseData = FinremCaseData.builder()
            .currentUserCaseRole(caseRole)
            .build();

        // PT todo - refactor into private func if poss
        FinremCallbackRequest callbackRequest =
            FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), CONTESTED, UPDATE_CASE_DETAILS_SOLICITOR, caseData);

        assertThat(assertThrows(IllegalArgumentException.class, () -> underTest.handle(callbackRequest, AUTH_TOKEN)).getMessage())
            .isEqualTo(String.format("Update Contact Details provided invalid CaseRole.  Case reference:%s", CASE_ID));
    }

    @Test
    void when_CaseRole_party_null_then_exceptionThrown() {
        FinremCaseData caseData = FinremCaseData.builder()
            .build();

        FinremCallbackRequest callbackRequest =
            FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), CONTESTED, UPDATE_CASE_DETAILS_SOLICITOR, caseData);

        CaseDocument litigantSolicitorAddedCaseDocument = CaseDocument.builder().documentFilename("docFileNameAdded").build();

        assertThat(assertThrows(IllegalArgumentException.class, () -> underTest.handle(callbackRequest, AUTH_TOKEN)).getMessage())
            .isEqualTo(String.format("Update Contact Details: CaseRole is null. Case reference:%s", CASE_ID));
    }

    // test app sol call the right downstream methods with right params
    @Test
    void when_applicantSolicitor_then_correct_validation_called() {

        String validEmail = "validEmail@test.com";

        ContactDetailsWrapper wrapper = ContactDetailsWrapper.builder()
            .applicantSolicitorEmail(validEmail)
            .build();

        FinremCaseData caseData = FinremCaseData.builder()
            .currentUserCaseRole(CaseRole.APP_SOLICITOR)
            .contactDetailsWrapper(wrapper)
            .build();

        FinremCallbackRequest callbackRequest =
            FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), CONTESTED, UPDATE_CASE_DETAILS_SOLICITOR, caseData);

        List<String> noErrors = new ArrayList<>();
        List<String> errors = new ArrayList<>(List.of("an error"));

        when(updateRepresentationService.validateEmailActiveForOrganisation(validEmail, CASE_ID, AUTH_TOKEN)).thenReturn(errors);

         underTest.handle(callbackRequest, AUTH_TOKEN);

        try (MockedStatic<ContactDetailsValidator> mocked = Mockito.mockStatic(ContactDetailsValidator.class)) {
            // run code that should call the static method
            underTest.handle(callbackRequest, AUTH_TOKEN);
            mocked.verify(() ->
                ContactDetailsValidator.checkForEmptyApplicantSolicitorPostcode(caseData, wrapper, noErrors)
            );

            // PT Todo, use call with real methods to allow the mockeedstatic and the below assertion to both work
            // verify(updateRepresentationService).validateEmailActiveForOrganisation(validEmail, CASE_ID, AUTH_TOKEN);
        }
    }

    // test some errors go in response

    // test resp sol call the right downstream methods with right params


    static Stream<Arguments> errorScenarios() {
        return Stream.of(
            Arguments.of(
                List.of("address error 1"),
                List.of("email error 1"),
                List.of("address error 1", "email error 1")
            ),
            Arguments.of(
                List.of(),
                List.of(),
                List.of()
            ),
            Arguments.of(
                List.of("address only"),
                List.of(),
                List.of("address only")
            )
        );
    }

    // PT todo - fix
    // May be easiest to write one working test, the make parameterised.
    // New approach needed.  New methods don't return errors, so stubs on 87 and 89 fail
    // Really, you want to test that the RIGHT validation functions are called, not the errors given.
    // And also test the extra logic in the core handle method.
    // New tests need to return the errors needed.
    // And maybe one test to confirm that a response can include an error - like AW has done here.
    //    @ParameterizedTest
    //    @MethodSource("errorScenarios")
    //    void testHandle(List<String> addressErrors,
    //                    List<String> emailErrors,
    //                    List<String> expectedErrors) {
    //
    //        FinremCaseData caseData = FinremCaseData.builder()
    //            .currentUserCaseRole(CaseRole.APP_SOLICITOR)
    //            .build();
    //
    //        ContactDetailsWrapper wrapper =  caseData.getContactDetailsWrapper();
    //
    //        FinremCallbackRequest callbackRequest =
    //            FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), CONTESTED, UPDATE_CASE_DETAILS_SOLICITOR, caseData);
    //
    //        List<String> errors = new ArrayList<>();
    //
    //        try (MockedStatic<ContactDetailsValidator> contactValidatorMock = mockStatic(ContactDetailsValidator.class)) {
    //            contactValidatorMock.when(() -> ContactDetailsValidator.checkForEmptyApplicantSolicitorPostcode(caseData, wrapper, errors))
    //                .thenReturn(errors.addAll(addressErrors));
    //            contactValidatorMock.when(() -> ContactDetailsValidator.checkForApplicantSolicitorEmailAddress(caseData, wrapper, errors))
    //                .thenReturn(errors.addAll(emailErrors));
    //
    //            GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
    //                underTest.handle(callbackRequest, AUTH_TOKEN);
    //
    //            assertThat(response.getErrors()).containsExactlyElementsOf(expectedErrors);
    //        }
    //    }
}
