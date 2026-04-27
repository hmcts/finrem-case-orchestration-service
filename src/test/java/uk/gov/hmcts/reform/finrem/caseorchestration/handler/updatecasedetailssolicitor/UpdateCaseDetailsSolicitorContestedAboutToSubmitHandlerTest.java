package uk.gov.hmcts.reform.finrem.caseorchestration.handler.updatecasedetailssolicitor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenerateCoverSheetService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationService;

import java.util.ArrayList;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID_IN_LONG;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.UPDATE_CASE_DETAILS_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class UpdateCaseDetailsSolicitorContestedAboutToSubmitHandlerTest {

    @InjectMocks
    private UpdateCaseDetailsSolicitorContestedAboutToSubmitHandler underTest;

    @Mock
    private GenerateCoverSheetService generateCoverSheetService;

    @Mock
    private UpdateRepresentationService updateRepresentationService;

    @Test
    void testCanHandle() {
        assertCanHandle(underTest,
            Arguments.of(ABOUT_TO_SUBMIT, CONTESTED, UPDATE_CASE_DETAILS_SOLICITOR),
            Arguments.of(ABOUT_TO_SUBMIT, CONSENTED, UPDATE_CASE_DETAILS_SOLICITOR)
        );
    }

    @Test
    void when_handle_then_shouldNotClearTemporaryFields() {
        assertThat(underTest.clearsTemporaryFields()).isTrue();
    }

    static Address buildAddress() {
        return Address.builder()
            .addressLine1("AddressLine1")
            .addressLine2("AddressLine2")
            .addressLine3("AddressLine3")
            .county("County")
            .country("Country")
            .postTown("Town")
            .postCode("EC1 3AS")
            .build();
    }

    static ContactDetailsWrapper buildContactDetailsWrapper(
        String applicantSolicitorName, String applicantSolicitorFirm,  String applicantSolicitorEmail, boolean isCurrentUserApplicantSolicitor,
        boolean includeRespondent, String respondentSolicitorName, String respondentSolicitorFirm, boolean isCurrentUserRespondentSolicitor) {

        ContactDetailsWrapper.ContactDetailsWrapperBuilder builder = ContactDetailsWrapper.builder()
            .applicantSolicitorName(applicantSolicitorName)
            .applicantSolicitorFirm(applicantSolicitorFirm)
            .applicantSolicitorEmail(applicantSolicitorEmail)
            .applicantSolicitorAddress(buildAddress())
            .currentUserIsApplicantSolicitor(isCurrentUserApplicantSolicitor ? YesOrNo.YES : YesOrNo.NO);

        if (includeRespondent) {
            builder.respondentSolicitorName(respondentSolicitorName)
                .respondentSolicitorFirm(respondentSolicitorFirm)
                .respondentSolicitorAddress(buildAddress())
                .currentUserIsRespondentSolicitor(isCurrentUserRespondentSolicitor ? YesOrNo.YES : YesOrNo.NO);
        }
        return builder.build();
    }

    static ContactDetailsWrapper buildContactDetailsWrapper(
        String applicantSolicitorName, String applicantSolicitorFirm, String applicantSolicitorEmail, Address applicantSolicitorAddress,
        boolean isCurrentUserApplicantSolicitor,
        boolean includeRespondent, String respondentSolicitorName, String respondentSolicitorFirm, Address respondentSolicitorAddress,
        boolean isCurrentUserRespondentSolicitor) {

        ContactDetailsWrapper.ContactDetailsWrapperBuilder builder = ContactDetailsWrapper.builder()
            .applicantSolicitorName(applicantSolicitorName)
            .applicantSolicitorFirm(applicantSolicitorFirm)
            .applicantSolicitorEmail(applicantSolicitorEmail)
            .applicantSolicitorAddress(applicantSolicitorAddress)
            .currentUserIsApplicantSolicitor(isCurrentUserApplicantSolicitor ? YesOrNo.YES : YesOrNo.NO);

        if (includeRespondent) {
            builder.respondentSolicitorName(respondentSolicitorName)
                .respondentSolicitorFirm(respondentSolicitorFirm)
                .respondentSolicitorAddress(respondentSolicitorAddress)
                .currentUserIsRespondentSolicitor(isCurrentUserRespondentSolicitor ? YesOrNo.YES : YesOrNo.NO);
        }
        return builder.build();
    }

    static FinremCaseDetails buildCaseDetails(ContactDetailsWrapper wrapper) {
        FinremCaseData data = FinremCaseData.builder().contactDetailsWrapper(wrapper).build();
        return FinremCaseDetails.builder().data(data).id(CASE_ID_IN_LONG).build();
    }

    /**
     *  Tests various scenarios of solicitor name and firm changes to verify the generation of cover sheets for both applicant and respondent.
     * @param beforeWrapper - Contact details wrapper representing the application and respondent solicitor details before the update
     * @param afterWrapper - Contact details wrapper representing the application and respondent solicitor details after the update
     * @param expectApplicantCoverSheet -   boolean flag indicating whether an applicant cover sheet is expected to be generated based on the changes
     * @param expectRespondentCoverSheet -  boolean flag indicating whether a respondent cover sheet is expected to be generated based on the changes
     */
    @ParameterizedTest
    @MethodSource
    void testSolicitorChangeScenarios(ContactDetailsWrapper beforeWrapper, ContactDetailsWrapper afterWrapper,
                                      boolean expectApplicantCoverSheet, boolean expectRespondentCoverSheet) {
        when(updateRepresentationService.validateEmailActiveForOrganisation(anyString(), any(), anyString()))
            .thenReturn(new ArrayList<>());

        FinremCaseDetails beforeDetails = buildCaseDetails(beforeWrapper);
        FinremCaseDetails afterDetails = buildCaseDetails(afterWrapper);

        underTest.handle(FinremCallbackRequest.builder()
            .caseDetails(afterDetails)
            .caseDetailsBefore(beforeDetails)
            .build(), AUTH_TOKEN);

        if (expectApplicantCoverSheet) {
            verify(generateCoverSheetService).generateAndSetApplicantCoverSheet(afterDetails, AUTH_TOKEN);
        } else {
            verify(generateCoverSheetService, never()).generateAndSetApplicantCoverSheet(afterDetails, AUTH_TOKEN);
        }
        if (expectRespondentCoverSheet) {
            verify(generateCoverSheetService).generateAndSetRespondentCoverSheet(afterDetails, AUTH_TOKEN);
        } else {
            verify(generateCoverSheetService, never()).generateAndSetRespondentCoverSheet(afterDetails, AUTH_TOKEN);
        }
        reset(generateCoverSheetService); // Reset for next parameterized run
    }

    /**
     *  Provides various scenarios of solicitor name and firm changes to test the generation of cover sheets.
     *  Stream of test scenario arguments lines corresponds to the following :-
     *  1. Old Applicant Solicitor details
     *  2. Old Respondent Solicitor details
     *  3. New Applicant Solicitor details
     *  4. New Respondent Solicitor details
     *  5. Expected cover sheet generation for applicant and respondent (boolean flags)
     */
    static Stream<Arguments> testSolicitorChangeScenarios() {
        return Stream.of(
            // 1. Both applicant and respondent changed
            Arguments.of(
                buildContactDetailsWrapper("Old AppSol Name", "Old AppSol Firm", "OldAppSol@email.com", true,
                                           true, "Old RespSol Name", "Old RespSol Firm", false),
                buildContactDetailsWrapper("New AppSol Name", "New AppSol Firm", "NewAppSol@email.com", true,
                                           true, "New RespSol Name", "New RespSol Firm", false),
                true, true
            ),
            // 2. No change both Applicant Solicitor and Respondent solicitor
            Arguments.of(
                buildContactDetailsWrapper("Old AppSol Name", "Old AppSol Firm", "OldAppSol@email.com", true,
                                           true, "Old RespSol Name", "Old RespSol Firm", false),
                buildContactDetailsWrapper("Old AppSol Name", "Old AppSol Firm", "OldAppSol@email.com", true,
                                           true, "Old RespSol Name", "Old RespSol Firm", false),
                false, false
            ),
            // 3. Only applicant changed
            Arguments.of(
                buildContactDetailsWrapper("Old AppSol Name", "Old AppSol Firm", "OldAppSol@email.com", true,
                                           true, "Old RespSol Name", "Old RespSol Firm", false),
                buildContactDetailsWrapper("New AppSol Name", "New AppSol Firm", "NewAppSol@email.com", true,
                                           true, "Old RespSol Name", "Old RespSol Firm", false),
                true, false
            ),
            // 4. Only respondent changed
            Arguments.of(
                buildContactDetailsWrapper("Old AppSol Name", "Old AppSol Firm", "OldAppSol@email.com", true,
                                           true, "Old RespSol Name", "Old RespSol Firm", false),
                buildContactDetailsWrapper("Old AppSol Name", "Old AppSol Firm", "OldAppSol@email.com", true,
                                           true, "New RespSol Name", "New RespSol Firm", false),
                false, true
            ),
            // 5. Only Applicant Change only by case sensitive
            Arguments.of(
                buildContactDetailsWrapper("old appsol name", "old appsol firm", "OldAppSol@email.com", true,
                                           false, null, null, false),
                buildContactDetailsWrapper("OLD APPSOL NAME", "OLD APPSOL FIRM", "OldAppSol@email.com", true,
                                           false, null, null, false),
                true, false
            ),
            // 6. Only Respondent Change only by case sensitive
            Arguments.of(
                buildContactDetailsWrapper("Old AppSol Name", "Old AppSol Firm", "OldAppSol@email.com", true,
                                           true, "Old RespSol Name", "Old RespSol Firm", false),
                buildContactDetailsWrapper("Old AppSol Name", "Old AppSol Firm", "OldAppSol@email.com", true,
                                           true, "OLD RESPSOL NAME", "OLD RESPSOL FIRM", false),
                false, true
            ),
            // 7. No change Applicant Solicitor, Respondent not represented.
            Arguments.of(
                buildContactDetailsWrapper("Old AppSol Name", "Old AppSol Firm", "OldAppSol@email.com", true,
                                           false, null, null, false),
                buildContactDetailsWrapper("Old AppSol Name", "Old AppSol Firm", "OldAppSol@email.com", true,
                                           false, null, null, false),
                false, false
            ),
            // 8. No change Respondent Solicitor, Applicant not represented.
            Arguments.of(
                buildContactDetailsWrapper(null, null, null, false,
                                           true, "Old RespSol Name", "Old RespSol Firm", true),
                buildContactDetailsWrapper(null, null, null, false,
                                           true, "Old RespSol Name", "Old RespSol Firm", true),
                false, false
            ),
            // 9. White space change for Applicant Solicitor, Respondent not represented.
            Arguments.of(
                buildContactDetailsWrapper("Old AppSol Name", "Old AppSol Firm", "OldAppSol@email.com", true,
                                           false, null, null, false),
                buildContactDetailsWrapper("Old AppSol Name ", "Old AppSol Firm ", "OldAppSol@email.com", true,
                                           false, null, null, false),
                false, false
            ),
            // 10. White space change Respondent Solicitor, Applicant not represented.
            Arguments.of(
                buildContactDetailsWrapper(null, null, null, false,
                                           true, "Old RespSol Name", "Old RespSol Firm", true),
                buildContactDetailsWrapper(null, null, null, false,
                                           true, "Old RespSol Name ", "Old RespSol Firm ", true),
                false, false
            )
        );
    }

    @Test
    void whenHandleWithNonNullCaseDetailsBeforeData_thenNoExceptionAndReturnsNotNull() {
        when(updateRepresentationService.validateEmailActiveForOrganisation(anyString(), any(), anyString()))
            .thenReturn(new ArrayList<>());

        ContactDetailsWrapper beforeWrapper = buildContactDetailsWrapper(
            "Old AppSol Name", "Old AppSol Firm", "OldAppSol@email.com", true,
            true, "Old RespSol Name", "Old RespSol Firm", false);

        ContactDetailsWrapper afterWrapper = buildContactDetailsWrapper(
            "New AppSol Name", "New AppSol Firm", "NewAppSol@email.com", true,
            true, "Old RespSol Name", "Old RespSol Firm", false);

        FinremCaseDetails beforeDetails = buildCaseDetails(beforeWrapper);
        FinremCaseDetails afterDetails = buildCaseDetails(afterWrapper);

        FinremCallbackRequest request = FinremCallbackRequest.builder()
            .caseDetails(afterDetails)
            .caseDetailsBefore(beforeDetails)
            .build();
        assertThat(underTest.handle(request, AUTH_TOKEN)).isNotNull();
    }

    /**
     *  Tests various scenarios of solicitor name, firm, email, and address changes to verify the generation
     *  of cover sheets for both applicant and respondent.
     */
    @ParameterizedTest
    @MethodSource
    void testSolicitorAndAddressChangeScenarios(
        String beforeAppSolName, String beforeAppSolFirm, String beforeAppSolEmail, Address beforeAppSolAddress,
        String beforeRespSolName, String beforeRespSolFirm, Address beforeRespSolAddress,
        String afterAppSolName, String afterAppSolFirm, String afterAppSolEmail, Address afterAppSolAddress,
        String afterRespSolName, String afterRespSolFirm, Address afterRespSolAddress,
        boolean expectApplicantCoverSheet, boolean expectRespondentCoverSheet) {

        when(updateRepresentationService.validateEmailActiveForOrganisation(anyString(), any(), anyString()))
            .thenReturn(new ArrayList<>());

        ContactDetailsWrapper beforeWrapper = buildContactDetailsWrapper(
            beforeAppSolName, beforeAppSolFirm, beforeAppSolEmail, beforeAppSolAddress, true,
            true, beforeRespSolName, beforeRespSolFirm, beforeRespSolAddress, false);


        ContactDetailsWrapper afterWrapper =buildContactDetailsWrapper(
            afterAppSolName, afterAppSolFirm, afterAppSolEmail,afterAppSolAddress,true,
            true, afterRespSolName, afterRespSolFirm, afterRespSolAddress, false);

        FinremCaseDetails beforeDetails = buildCaseDetails(beforeWrapper);
        FinremCaseDetails afterDetails = buildCaseDetails(afterWrapper);

        underTest.handle(FinremCallbackRequest.builder()
            .caseDetails(afterDetails)
            .caseDetailsBefore(beforeDetails)
            .build(), AUTH_TOKEN);

        if (expectApplicantCoverSheet) {
            verify(generateCoverSheetService).generateAndSetApplicantCoverSheet(afterDetails, AUTH_TOKEN);
        } else {
            verify(generateCoverSheetService, never()).generateAndSetApplicantCoverSheet(afterDetails, AUTH_TOKEN);
        }
        if (expectRespondentCoverSheet) {
            verify(generateCoverSheetService).generateAndSetRespondentCoverSheet(afterDetails, AUTH_TOKEN);
        } else {
            verify(generateCoverSheetService, never()).generateAndSetRespondentCoverSheet(afterDetails, AUTH_TOKEN);
        }
        reset(generateCoverSheetService);
    }

    /**
     * Provides various scenarios of solicitor name, firm, email, and address changes to test the generation of cover sheets.
     * Stream of test scenario arguments lines corresponds to the following :-
     *  1. Old Applicant Solicitor details
     *  2. Old Respondent Solicitor details
     *  3. New Applicant Solicitor details
     *  4. New Respondent Solicitor details
     *  5. Expected cover sheet generation for applicant and respondent (boolean flags)
     */
    static Stream<Arguments> testSolicitorAndAddressChangeScenarios() {
        Address oldAddress = buildAddress();
        Address newAddress = Address.builder().addressLine1("OtherLine1").postCode("ZZ1 1ZZ").build();
        Address addressWhiteSpace = buildAddress();
        addressWhiteSpace.setPostCode("EC1 3AS ");
        Address addressNull = null;

        return Stream.of(
            // 1. Both applicant and respondent address changed
            Arguments.of(
                "Old AppSol Name", "Old AppSol Firm", "OldAppSol@email.com", oldAddress,
                "Old RespSol Name", "Old AppSol Firm", oldAddress,
                "New AppSol Firm", "New AppSol Firm", "NewAppSol@email.com", newAddress,
                "New RespSol Name", "New RespSol Firm", newAddress,
                true, true),
            // 2. Only applicant address changed
            Arguments.of(
                "Old AppSol Name", "Old AppSol Firm", "OldAppSol@email.com", oldAddress,
                "Old RespSol Name", "Old AppSol Firm", oldAddress,
                "Old AppSol Name", "Old AppSol Firm", "OldAppSol@email.com", newAddress,
                "Old RespSol Name", "Old AppSol Firm", oldAddress,
                true, false),
            // 3. Only respondent address changed
            Arguments.of(
                "Old AppSol Name", "Old AppSol Firm", "OldAppSol@email.com", oldAddress,
                "Old RespSol Name", "Old AppSol Firm", oldAddress,
                "Old AppSol Name", "Old AppSol Firm", "OldAppSol@email.com", oldAddress,
                "Old RespSol Name", "Old AppSol Firm", newAddress,
                false, true),
            // 4. No address change
            Arguments.of(
                "Old AppSol Name", "Old AppSol Firm", "OldAppSol@email.com", oldAddress,
                "Old RespSol Name", "Old AppSol Firm", oldAddress,
                "Old AppSol Name", "Old AppSol Firm", "OldAppSol@email.com", oldAddress,
                "Old RespSol Name", "Old AppSol Firm", oldAddress,
                false, false),
            // 5. Applicant address becomes null
            Arguments.of(
                "Old AppSol Name", "Old AppSol Firm", "OldAppSol@email.com", oldAddress,
                "Old RespSol Name", "Old AppSol Firm", oldAddress,
                "Old AppSol Name", "Old AppSol Firm", "OldAppSol@email.com", addressNull,
                "Old RespSol Name", "Old AppSol Firm", oldAddress,
                true, false),
            // 6. Respondent address becomes null
            Arguments.of(
                "Old AppSol Name", "Old AppSol Firm", "OldAppSol@email.com", oldAddress,
                "Old RespSol Name", "Old AppSol Firm", oldAddress,
                "Old AppSol Name", "Old AppSol Firm", "OldAppSol@email.com", oldAddress,
                "Old RespSol Name", "Old AppSol Firm", addressNull,
                false, true),
            // 7. Applicant address with whitespace trim
            Arguments.of(
                "Old AppSol Name", "Old AppSol Firm", "OldAppSol@email.com", oldAddress,
                "Old RespSol Name", "Old AppSol Firm", oldAddress,
                "Old AppSol Name", "Old AppSol Firm", "OldAppSol@email.com", addressWhiteSpace,
                "Old RespSol Name", "Old AppSol Firm", oldAddress,
                false, false),
            // 8. Respondent address with whitespace trim
            Arguments.of(
                "Old AppSol Name", "Old AppSol Firm", "OldAppSol@email.com", oldAddress,
                "Old RespSol Name", "Old AppSol Firm", oldAddress,
                "Old AppSol Name", "Old AppSol Firm", "OldAppSol@email.com", oldAddress,
                "Old RespSol Name", "Old AppSol Firm", addressWhiteSpace,
                false, false)
        );
    }
}
