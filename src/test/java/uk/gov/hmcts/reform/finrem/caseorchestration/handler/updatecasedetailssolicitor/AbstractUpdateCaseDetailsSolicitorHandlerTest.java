package uk.gov.hmcts.reform.finrem.caseorchestration.handler.updatecasedetailssolicitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ContactDetailsValidator;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.UPDATE_CASE_DETAILS_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@ExtendWith(MockitoExtension.class)
class AbstractUpdateCaseDetailsSolicitorHandlerTest extends BaseServiceTest {

    private AbstractUpdateCaseDetailsSolicitorHandler underTest;

    @Mock
    private UpdateRepresentationService updateRepresentationService;

    private static final class TestHandler extends AbstractUpdateCaseDetailsSolicitorHandler {
        private TestHandler(FinremCaseDetailsMapper mapper, UpdateRepresentationService service) {
            super(mapper, service);
        }

        @Override
        public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
            return true; // Actual concrete handlers tested properly for canHandle.
        }
    }

    @BeforeEach
    void setUp() {
        underTest = new TestHandler(finremCaseDetailsMapper, updateRepresentationService);
    }

    @ParameterizedTest
    @EnumSource(value = CaseRole.class, mode = EnumSource.Mode.EXCLUDE,  names = {"APP_SOLICITOR", "RESP_SOLICITOR"})
    /*
     All CaseRoles other that applicant and respondent solicitor should fail.
     */
    void when_CaseRole_party_wrong_then_exceptionThrown(CaseRole caseRole) {
        // Arrange
        FinremCaseData caseData = FinremCaseData.builder()
            .currentUserCaseRole(caseRole)
            .build();

        // PT todo - refactor into private func if poss - but do it after consented introduced, as @EnumSource could change.
        FinremCallbackRequest callbackRequest =
            FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), CONTESTED, UPDATE_CASE_DETAILS_SOLICITOR, caseData);

        // Act and assert
        assertThat(assertThrows(IllegalArgumentException.class, () -> underTest.handle(callbackRequest, AUTH_TOKEN)).getMessage())
            .isEqualTo(String.format("Update Contact Details provided invalid CaseRole.  Case reference:%s", CASE_ID));
    }

    @Test
    void when_CaseRole_party_null_then_exceptionThrown() {
        // Arrange
        FinremCaseData caseData = FinremCaseData.builder()
            .build();

        FinremCallbackRequest callbackRequest =
            FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), CONTESTED, UPDATE_CASE_DETAILS_SOLICITOR, caseData);

        // Act and assert
        assertThat(assertThrows(IllegalArgumentException.class, () -> underTest.handle(callbackRequest, AUTH_TOKEN)).getMessage())
            .isEqualTo(String.format("Update Contact Details: CaseRole is null. Case reference:%s", CASE_ID));
    }

    /*
     * Todo parameterise with respondent solicitor
     * Test that applicant solicitor validation calls the right downstream methods with right params.
     * Then confirm representation service errors are shown in the response.
     */
    @Test
    void when_applicantSolicitor_then_correct_validation_called() {
        // Arrange
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

        // Act
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = underTest.handle(callbackRequest, AUTH_TOKEN);

        // Assert validation methods called with the correct params.
        try (MockedStatic<ContactDetailsValidator> mocked = mockStatic(ContactDetailsValidator.class)) {
            // run code that should call the static method
            underTest.handle(callbackRequest, AUTH_TOKEN);
            mocked.verify(() ->
                ContactDetailsValidator.checkForEmptyApplicantSolicitorPostcode(caseData, wrapper, noErrors)
            );
            mocked.verify(() ->
                ContactDetailsValidator.checkForApplicantSolicitorEmailAddress(caseData, wrapper, noErrors)
            );

            verify(updateRepresentationService)
                .validateEmailActiveForOrganisation(validEmail, CASE_ID, AUTH_TOKEN);
        }

        // Assert mocked errors end up in the response
        assertThat(response.getErrors()).isEqualTo(errors);
    }

    static Stream<Arguments> errorScenarios(String solicitorRole) {

        Address invalidAddress = Address.builder()
            .addressLine1("invalidAddressLine1")
            .county("invalidCounty")
            .build();

        Address validAddress = Address.builder()
            .addressLine1("validAddressLine1")
            .county("validCounty")
            .postCode("PL2 2LP")
            .build();

           String invalidEmail = "invalidEmail";
           String validEmail = "email@test.com";
           String invalidEmailError = invalidEmail + " is not a valid Email address.";
           String invalidAddressError = "Postcode field is required for " + solicitorRole + " solicitor address.";

        return Stream.of(
            Arguments.of(
                invalidAddress,
                invalidEmail,
                List.of(invalidAddressError, invalidEmailError)
            ),
            Arguments.of(
                validAddress,
                validEmail,
                List.of()
            ),
            Arguments.of(
                invalidAddress,
                validEmail,
                List.of(invalidAddressError)
            ),
            Arguments.of(
                validAddress,
                invalidEmail,
                List.of(invalidEmailError)
            )
        );
    }

    static Stream<Arguments> applicantSolicitorErrorScenarios() {
        return errorScenarios("applicant");
    }

    @ParameterizedTest
    @MethodSource("applicantSolicitorErrorScenarios")
    void when_applicantSolicitorWithAddressErrors_then_handleErrors(Address address,
                    String email,
                    List<String> expectedErrors) {

        FinremCaseData caseData = FinremCaseData.builder()
            .currentUserCaseRole(CaseRole.APP_SOLICITOR)
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .applicantSolicitorEmail(email)
                .applicantSolicitorAddress(address)
                .applicantRepresented(YesOrNo.YES)
                .build())
            .build();

        FinremCallbackRequest callbackRequest =
            FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), CONTESTED, UPDATE_CASE_DETAILS_SOLICITOR, caseData);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            underTest.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getErrors()).containsExactlyElementsOf(expectedErrors);
    }
}
