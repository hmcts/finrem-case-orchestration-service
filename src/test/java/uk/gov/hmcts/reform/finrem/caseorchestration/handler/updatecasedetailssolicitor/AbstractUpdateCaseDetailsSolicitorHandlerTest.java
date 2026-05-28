package uk.gov.hmcts.reform.finrem.caseorchestration.handler.updatecasedetailssolicitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@ExtendWith(MockitoExtension.class)
class AbstractUpdateCaseDetailsSolicitorHandlerTest extends BaseServiceTest {

    private AbstractUpdateCaseDetailsSolicitorHandler underTest;

    private static final String VALID_EMAIL = "validEmail@test.com";

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

    /*
     All CaseRoles other that applicant and respondent solicitor should fail.
     */
    @ParameterizedTest
    @EnumSource(value = CaseRole.class, mode = EnumSource.Mode.EXCLUDE, names = {"APP_SOLICITOR", "RESP_SOLICITOR"})
    void when_caseRolePartyWrong_then_exceptionThrown(CaseRole caseRole) {
        // Arrange
        FinremCaseData caseData = FinremCaseData.builder()
            .currentUserCaseRole(caseRole)
            .build();

        // Act and assert. Simpler to run test for each case type, rather than parameterise further.
        ArrayList<CaseType> caseTypes = new ArrayList<>(List.of(CONTESTED, CONSENTED));
        caseTypes.forEach(caseType -> {

            FinremCallbackRequest callbackRequest =
                FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), caseType, UPDATE_CASE_DETAILS_SOLICITOR, caseData);

            assertThat(assertThrows(IllegalArgumentException.class, () -> underTest.handle(callbackRequest, AUTH_TOKEN)).getMessage())
                .isEqualTo(String.format(
                    "Update Contact Details: Current user is not applicant or respondent solicitor. Case reference:%s",
                    CASE_ID)
                );
        });
    }

    @Test
    void when_noPartySpecified_then_exceptionThrown() {
        // Arrange
        FinremCaseData caseData = FinremCaseData.builder()
            .build();

        FinremCallbackRequest callbackRequest =
            FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), CONTESTED, UPDATE_CASE_DETAILS_SOLICITOR, caseData);

        // Act and assert
        assertThat(assertThrows(IllegalArgumentException.class, () -> underTest.handle(callbackRequest, AUTH_TOKEN)).getMessage())
            .isEqualTo(String.format(
                "Update Contact Details: Current user is not applicant or respondent solicitor. Case reference:%s",
                CASE_ID)
            );
    }

    /*
     * Test that applicant solicitor validation calls the right downstream methods with right params.
     * Then confirm representation service errors are shown in the response.
     */
    @ParameterizedTest
    @CsvSource({
        "[APPSOLICITOR],FinancialRemedyMVP2",
        "[APPSOLICITOR],FinancialRemedyContested",
        "[RESPSOLICITOR],FinancialRemedyMVP2",
        "[RESPSOLICITOR],FinancialRemedyContested"
    })
    void when_handle_thenCorrectValidationCalled(String caseRoleString, String caseTypeString) {
        // Arrange
        CaseRole caseRole = CaseRole.forValue(caseRoleString);
        CaseType caseType = CaseType.forValue(caseTypeString);

        ContactDetailsWrapper wrapper = getContactDetailsWrapper(caseRole, caseType);

        FinremCaseData caseData = FinremCaseData.builder()
            .contactDetailsWrapper(wrapper)
            .build();

        List<String> noErrors = new ArrayList<>();
        List<String> errors = new ArrayList<>(List.of("an error"));

        when(updateRepresentationService.validateEmailActiveForOrganisation(VALID_EMAIL, CASE_ID)).thenReturn(errors);

        // Act
        FinremCallbackRequest callbackRequest =
            FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), caseType, UPDATE_CASE_DETAILS_SOLICITOR, caseData);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = underTest.handle(callbackRequest, AUTH_TOKEN);

        // Assert validation methods called with the correct params.
        if (CaseRole.APP_SOLICITOR.equals(caseRole)) {
            verifyApplicantValidationAndParams(callbackRequest, caseData, wrapper, noErrors, underTest, updateRepresentationService);
        } else {
            verifyRespondentValidationAndParams(callbackRequest, caseData, wrapper, noErrors, underTest, updateRepresentationService);
        }

        // Assert mocked errors end up in the response
        assertThat(response.getErrors()).isEqualTo(errors);
    }

    private static void verifyApplicantValidationAndParams(FinremCallbackRequest callbackRequest, FinremCaseData caseData,
                                                           ContactDetailsWrapper wrapper, List<String> noErrors,
                                                           AbstractUpdateCaseDetailsSolicitorHandler underTest,
                                                           UpdateRepresentationService updateRepresentationService) {
        try (MockedStatic<ContactDetailsValidator> mocked = mockStatic(ContactDetailsValidator.class)) {
            underTest.handle(callbackRequest, AUTH_TOKEN);
            mocked.verify(() ->
                ContactDetailsValidator.checkForEmptyApplicantSolicitorPostcode(caseData, wrapper, noErrors)
            );
            mocked.verify(() ->
                ContactDetailsValidator.checkForApplicantSolicitorEmailAddress(caseData, wrapper, noErrors)
            );

            verify(updateRepresentationService)
                .validateEmailActiveForOrganisation(VALID_EMAIL, CASE_ID);
        }
    }

    private static void verifyRespondentValidationAndParams(FinremCallbackRequest callbackRequest,
                                                            FinremCaseData caseData, ContactDetailsWrapper wrapper,
                                                            List<String> noErrors,
                                                            AbstractUpdateCaseDetailsSolicitorHandler underTest,
                                                            UpdateRepresentationService updateRepresentationService) {
        try (MockedStatic<ContactDetailsValidator> mocked = mockStatic(ContactDetailsValidator.class)) {
            underTest.handle(callbackRequest, AUTH_TOKEN);
            mocked.verify(() ->
                ContactDetailsValidator.checkForEmptyRespondentSolicitorPostcode(caseData, wrapper, noErrors)
            );
            mocked.verify(() ->
                ContactDetailsValidator.checkForRespondentSolicitorEmail(caseData, wrapper, noErrors)
            );

            verify(updateRepresentationService)
                .validateEmailActiveForOrganisation(VALID_EMAIL, CASE_ID);
        }
    }

    private static ContactDetailsWrapper getContactDetailsWrapper(CaseRole caseRole, CaseType caseType) {
        return switch (caseRole) {
            case APP_SOLICITOR -> {
                if (CONSENTED.equals(caseType)) {
                    yield ContactDetailsWrapper.builder()
                        .solicitorEmail(VALID_EMAIL)
                        .currentUserIsApplicantSolicitor(YesOrNo.YES)
                        .build();
                } else if (CONTESTED.equals(caseType)) {
                    yield ContactDetailsWrapper.builder()
                        .applicantSolicitorEmail(VALID_EMAIL)
                        .currentUserIsApplicantSolicitor(YesOrNo.YES)
                        .build();
                } else {
                    throw new IllegalArgumentException("Unsupported caseType: " + caseType);
                }
            }

            case RESP_SOLICITOR -> ContactDetailsWrapper.builder()
                .respondentSolicitorEmail(VALID_EMAIL)
                .currentUserIsRespondentSolicitor(YesOrNo.YES)
                .build();

            default -> throw new IllegalArgumentException("Unsupported caseRole: " + caseRole);
        };
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
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .applicantSolicitorEmail(email)
                .applicantSolicitorAddress(address)
                .applicantRepresented(YesOrNo.YES)
                .currentUserIsApplicantSolicitor(YesOrNo.YES)
                .build())
            .build();

        FinremCallbackRequest callbackRequest =
            FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), CONTESTED, UPDATE_CASE_DETAILS_SOLICITOR, caseData);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            underTest.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getErrors()).containsExactlyElementsOf(expectedErrors);
    }

    static Stream<Arguments> respondentSolicitorErrorScenarios() {
        return errorScenarios("respondent");
    }

    /*
     * MethodSource for error scenarios.
     * Case built as contested, with contestedRespondentRepresented set to YES.
     */
    @ParameterizedTest
    @MethodSource("respondentSolicitorErrorScenarios")
    void when_respondentSolicitorWithAddressErrors_andContested_thenHandleErrors(Address address,
                                                                    String email,
                                                                    List<String> expectedErrors) {

        FinremCaseData caseData = FinremCaseData.builder()
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .respondentSolicitorEmail(email)
                .respondentSolicitorAddress(address)
                .contestedRespondentRepresented(YesOrNo.YES)
                .currentUserIsRespondentSolicitor(YesOrNo.YES)
                .build())
            .build();

        FinremCallbackRequest callbackRequest =
            FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), CONTESTED, UPDATE_CASE_DETAILS_SOLICITOR, caseData);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            underTest.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getErrors()).containsExactlyElementsOf(expectedErrors);
    }

    /*
     * MethodSource for error scenarios.
     * Case built as consented, with consentedRespondentRepresented set to YES.
     */
    @ParameterizedTest
    @MethodSource("respondentSolicitorErrorScenarios")
    void when_respondentSolicitorWithAddressErrors_andConsented_thenHandleErrors(Address address,
                                                                                 String email,
                                                                                 List<String> expectedErrors) {

        FinremCaseData caseData = FinremCaseData.builder()
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .respondentSolicitorEmail(email)
                .respondentSolicitorAddress(address)
                .consentedRespondentRepresented(YesOrNo.YES)
                .currentUserIsRespondentSolicitor(YesOrNo.YES)
                .build())
            .build();

        FinremCallbackRequest callbackRequest =
            FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), CONSENTED, UPDATE_CASE_DETAILS_SOLICITOR, caseData);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            underTest.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getErrors()).containsExactlyElementsOf(expectedErrors);
    }
}
