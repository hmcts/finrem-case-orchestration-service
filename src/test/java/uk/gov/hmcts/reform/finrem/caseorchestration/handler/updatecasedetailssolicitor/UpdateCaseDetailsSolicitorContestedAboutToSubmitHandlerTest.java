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
        String applicantSolicitorName, String applicantSolicitorFirm, String applicantSolicitorEmail, boolean isCurrentUserApplicantSolicitor,
        boolean includeRespondent, String respondentSolicitorName, String respondentSolicitorFirm) {

        ContactDetailsWrapper.ContactDetailsWrapperBuilder builder = ContactDetailsWrapper.builder()
            .applicantSolicitorName(applicantSolicitorName)
            .applicantSolicitorFirm(applicantSolicitorFirm)
            .applicantSolicitorEmail(applicantSolicitorEmail)
            .applicantSolicitorAddress(buildAddress())
            .currentUserIsApplicantSolicitor(isCurrentUserApplicantSolicitor ? YesOrNo.YES : YesOrNo.NO);

        if (includeRespondent) {
            builder.respondentSolicitorName(respondentSolicitorName)
                .respondentSolicitorFirm(respondentSolicitorFirm)
                .respondentSolicitorAddress(buildAddress());
        }
        return builder.build();
    }

    static FinremCaseDetails buildCaseDetails(ContactDetailsWrapper wrapper) {
        FinremCaseData data = FinremCaseData.builder().contactDetailsWrapper(wrapper).build();
        return FinremCaseDetails.builder().data(data).id(CASE_ID_IN_LONG).build();
    }

    @ParameterizedTest
    @MethodSource("provideSolicitorChangeScenarios")
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
    static Stream<Arguments> provideSolicitorChangeScenarios() {
        // 1. Both applicant and respondent changed
        ContactDetailsWrapper before1 = buildContactDetailsWrapper(
            "Old AppSol Name", "Old AppSol Firm", "OldAppSol@email.com", true,
            true, "Old RespSol Name", "Old RespSol Firm");

        ContactDetailsWrapper after1 = buildContactDetailsWrapper(
            "New AppSol Name", "New AppSol Firm", "NewAppSol@email.com", true,
            true, "New RespSol Name", "New RespSol Firm");

        // 2. No change both Applicant Solicitor and Respondent solicitor
        ContactDetailsWrapper before2 = buildContactDetailsWrapper(
            "Old AppSol Name", "Old AppSol Firm", "OldAppSol@email.com", true,
            true, "Old RespSol Name", "Old RespSol Firm");

        ContactDetailsWrapper after2 = buildContactDetailsWrapper(
            "Old AppSol Name", "Old AppSol Firm", "OldAppSol@email.com", true,
            true, "Old RespSol Name", "Old RespSol Firm");

        // 3. Only applicant changed
        ContactDetailsWrapper before3 = buildContactDetailsWrapper(
            "Old AppSol Name", "Old AppSol Firm", "OldAppSol@email.com", true,
            true, "Old RespSol Name", "Old RespSol Firm");

        ContactDetailsWrapper after3 = buildContactDetailsWrapper(
            "New AppSol Name", "New AppSol Firm", "NewAppSol@email.com", true,
            true, "Old RespSol Name", "Old RespSol Firm");

        // 4. Only respondent changed
        ContactDetailsWrapper before4 = buildContactDetailsWrapper(
            "Old AppSol Name", "Old AppSol Firm", "OldAppSol@email.com", true,
            true, "Old RespSol Name", "Old RespSol Firm");

        ContactDetailsWrapper after4 = buildContactDetailsWrapper(
            "Old AppSol Name", "Old AppSol Firm", "OldAppSol@email.com", true,
            true, "New RespSol Name", "New RespSol Firm");

        // 5. Only Applicant Change only by case sensitive
        ContactDetailsWrapper before5 = buildContactDetailsWrapper(
            "old appsol name", "old appsol firm", "OldAppSol@email.com", true,
            false, null, null);

        ContactDetailsWrapper after5 = buildContactDetailsWrapper(
            "OLD APPSOL NAME", "OLD APPSOL FIRM", "OldAppSol@email.com", true,
            false, null, null);

        // 6. Only Respondent Change only by case sensitive
        ContactDetailsWrapper before6 = buildContactDetailsWrapper(
            "Old AppSol Name", "Old AppSol Firm", "OldAppSol@email.com", true,
            true, "Old RespSol Name", "Old RespSol Firm");

        ContactDetailsWrapper after6 = buildContactDetailsWrapper(
            "Old AppSol Name", "Old AppSol Firm", "OldAppSol@email.com", true,
            true, "OLD RESPSOL NAME", "OLD RESPSOL FIRM");

        // 7. No change Applicant Solicitor, Respondent not represented.
        ContactDetailsWrapper before7 = buildContactDetailsWrapper(
            "Old AppSol Name", "Old AppSol Firm", "OldAppSol@email.com", true,
            false, null, null);

        ContactDetailsWrapper after7 = buildContactDetailsWrapper(
            "Old AppSol Name", "Old AppSol Firm", "OldAppSol@email.com", true,
            false, null, null);

        // Arguments denotes: Before ContactDetails of Solicitors, After ontactDetails of Solicitors, Expected applicant cover sheet, Expected respondent cover sheet
        return Stream.of(
            Arguments.of(before1, after1, true, true),
            Arguments.of(before2, after2, false, false),
            Arguments.of(before3, after3, true, false),
            Arguments.of(before4, after4, false, true),
            Arguments.of(before5, after5, true, false),
            Arguments.of(before6, after6, false, true),
            Arguments.of(before7, after7, false, false)
        );
    }

    @Test
    void whenHandleWithNonNullCaseDetailsBeforeData_thenNoExceptionAndReturnsNotNull() {
        when(updateRepresentationService.validateEmailActiveForOrganisation(anyString(), any(), anyString()))
            .thenReturn(new ArrayList<>());

        ContactDetailsWrapper beforeWrapper = buildContactDetailsWrapper(
            "Old AppSol Name", "Old AppSol Firm", "OldAppSol@email.com", true,
            true, "Old RespSol Name", "Old RespSol Firm");

        ContactDetailsWrapper afterWrapper = buildContactDetailsWrapper(
            "New AppSol Name", "New AppSol Firm", "NewAppSol@email.com", true,
            true, "Old RespSol Name", "Old RespSol Firm");

        FinremCaseDetails beforeDetails = buildCaseDetails(beforeWrapper);
        FinremCaseDetails afterDetails = buildCaseDetails(afterWrapper);

        FinremCallbackRequest request = FinremCallbackRequest.builder()
            .caseDetails(afterDetails)
            .caseDetailsBefore(beforeDetails)
            .build();
        assertThat(underTest.handle(request, AUTH_TOKEN)).isNotNull();
    }

    @ParameterizedTest
    @MethodSource("provideSolicitorAndAddressChangeScenarios")
    void testSolicitorAndAddressChangeScenarios(
        String beforeAppSolName, String beforeAppSolFirm, String beforeAppSolEmail, Address beforeAppSolAddress,
        String beforeRespSolName, String beforeRespSolFirm, Address beforeRespSolAddress,
        String afterAppSolName, String afterAppSolFirm, String afterAppSolEmail, Address afterAppSolAddress,
        String afterRespSolName, String afterRespSolFirm, Address afterRespSolAddress,
        boolean expectApplicantCoverSheet, boolean expectRespondentCoverSheet) {

        when(updateRepresentationService.validateEmailActiveForOrganisation(anyString(), any(), anyString()))
            .thenReturn(new ArrayList<>());

        ContactDetailsWrapper beforeWrapper = ContactDetailsWrapper.builder()
            .applicantSolicitorName(beforeAppSolName)
            .applicantSolicitorFirm(beforeAppSolFirm)
            .applicantSolicitorEmail(beforeAppSolEmail)
            .applicantSolicitorAddress(beforeAppSolAddress)
            .respondentSolicitorName(beforeRespSolName)
            .respondentSolicitorFirm(beforeRespSolFirm)
            .respondentSolicitorAddress(beforeRespSolAddress)
            .currentUserIsApplicantSolicitor(YesOrNo.YES)
            .build();

        ContactDetailsWrapper afterWrapper = ContactDetailsWrapper.builder()
            .applicantSolicitorName(afterAppSolName)
            .applicantSolicitorFirm(afterAppSolFirm)
            .applicantSolicitorEmail(afterAppSolEmail)
            .applicantSolicitorAddress(afterAppSolAddress)
            .respondentSolicitorName(afterRespSolName)
            .respondentSolicitorFirm(afterRespSolFirm)
            .respondentSolicitorAddress(afterRespSolAddress)
            .currentUserIsApplicantSolicitor(YesOrNo.YES)
            .build();

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
    static Stream<Arguments> provideSolicitorAndAddressChangeScenarios() {
        Address oldAddress = buildAddress();
        Address newAddress = Address.builder().addressLine1("OtherLine1").postCode("ZZ1 1ZZ").build();
        Address addressNull = null;

        return Stream.of(
            // Both applicant and respondent address changed
            Arguments.of(
                "Old AppSol Name", "Old AppSol Firm", "OldAppSol@email.com", oldAddress,
                "Old RespSol Name", "Old AppSol Firm", oldAddress,
                "New AppSol Firm", "New AppSol Firm", "NewAppSol@email.com", newAddress,
                "New RespSol Name", "New RespSol Firm", newAddress,
                true, true),
            // Only applicant address changed
            Arguments.of(
                "Old AppSol Name", "Old AppSol Firm", "OldAppSol@email.com", oldAddress,
                "Old RespSol Name", "Old AppSol Firm", oldAddress,
                "Old AppSol Name", "Old AppSol Firm", "OldAppSol@email.com", newAddress,
                "Old RespSol Name", "Old AppSol Firm", oldAddress,
                true, false),
            // Only respondent address changed
            Arguments.of(
                "Old AppSol Name", "Old AppSol Firm", "OldAppSol@email.com", oldAddress,
                "Old RespSol Name", "Old AppSol Firm", oldAddress,
                "Old AppSol Name", "Old AppSol Firm", "OldAppSol@email.com", oldAddress,
                "Old RespSol Name", "Old AppSol Firm", newAddress,
                false, true),
            // No address change
            Arguments.of(
                "Old AppSol Name", "Old AppSol Firm", "OldAppSol@email.com", oldAddress,
                "Old RespSol Name", "Old AppSol Firm", oldAddress,
                "Old AppSol Name", "Old AppSol Firm", "OldAppSol@email.com", oldAddress,
                "Old RespSol Name", "Old AppSol Firm", oldAddress,
                false, false),
            // Applicant address becomes null
            Arguments.of(
                "Old AppSol Name", "Old AppSol Firm", "OldAppSol@email.com", oldAddress,
                "Old RespSol Name", "Old AppSol Firm", oldAddress,
                "Old AppSol Name", "Old AppSol Firm", "OldAppSol@email.com", addressNull,
                "Old RespSol Name", "Old AppSol Firm", oldAddress,
                true, false),
            // Respondent address becomes null
            Arguments.of(
                "Old AppSol Name", "Old AppSol Firm", "OldAppSol@email.com", oldAddress,
                "Old RespSol Name", "Old AppSol Firm", oldAddress,
                "Old AppSol Name", "Old AppSol Firm", "OldAppSol@email.com", oldAddress,
                "Old RespSol Name", "Old AppSol Firm", addressNull,
                false, true)
        );
    }
}
