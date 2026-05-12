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
class UpdateCaseDetailsSolicitorAboutToSubmitHandlerTest {

    @InjectMocks
    private UpdateCaseDetailsSolicitorAboutToSubmitHandler underTest;

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
        String applicantSolicitorName,
        String applicantSolicitorFirm,
        String applicantSolicitorEmail,
        boolean isCurrentUserApplicantSolicitor,
        boolean includeRespondent,
        String respondentSolicitorName,
        String respondentSolicitorFirm,
        String respondentSolicitorEmail,
        boolean isCurrentUserRespondentSolicitor) {

        ContactDetailsWrapper.ContactDetailsWrapperBuilder builder = ContactDetailsWrapper.builder()
            .applicantSolicitorName(applicantSolicitorName)
            .applicantSolicitorFirm(applicantSolicitorFirm)
            .applicantSolicitorEmail(applicantSolicitorEmail)
            .applicantSolicitorAddress(buildAddress())
            .currentUserIsApplicantSolicitor(isCurrentUserApplicantSolicitor ? YesOrNo.YES : YesOrNo.NO);

        if (includeRespondent) {
            builder.respondentSolicitorName(respondentSolicitorName)
                .respondentSolicitorFirm(respondentSolicitorFirm)
                .respondentSolicitorEmail(respondentSolicitorEmail)
                .respondentSolicitorAddress(buildAddress())
                .currentUserIsRespondentSolicitor(isCurrentUserRespondentSolicitor ? YesOrNo.YES : YesOrNo.NO);
        }
        return builder.build();
    }

    static ContactDetailsWrapper buildContactDetailsWrapper(
        String applicantSolicitorName,
        String applicantSolicitorFirm,
        String applicantSolicitorEmail,
        Address applicantSolicitorAddress,
        String respondentSolicitorName,
        String respondentSolicitorFirm,
        Address respondentSolicitorAddress) {

        ContactDetailsWrapper.ContactDetailsWrapperBuilder builder = ContactDetailsWrapper.builder()
            .applicantSolicitorName(applicantSolicitorName)
            .applicantSolicitorFirm(applicantSolicitorFirm)
            .applicantSolicitorEmail(applicantSolicitorEmail)
            .applicantSolicitorAddress(applicantSolicitorAddress)
            .currentUserIsApplicantSolicitor(YesOrNo.YES);

        builder.respondentSolicitorName(respondentSolicitorName)
            .respondentSolicitorFirm(respondentSolicitorFirm)
            .respondentSolicitorAddress(respondentSolicitorAddress)
            .currentUserIsRespondentSolicitor(YesOrNo.NO);
        return builder.build();
    }

    static FinremCaseDetails buildCaseDetails(ContactDetailsWrapper wrapper) {
        FinremCaseData data = FinremCaseData.builder().contactDetailsWrapper(wrapper).build();
        return FinremCaseDetails.builder().data(data).id(CASE_ID_IN_LONG).build();
    }

    /**
     *  Tests various scenarios of solicitor name, firm, email, and address changes to verify the generation
     *  of cover sheets for both applicant and respondent.
     */
    @ParameterizedTest
    @MethodSource
    void testSolicitorChangeScenarios(ContactDetailsWrapper beforeWrapper, ContactDetailsWrapper afterWrapper,
                                      boolean expectApplicantCoverSheet, boolean expectRespondentCoverSheet) {
        when(updateRepresentationService.validateEmailActiveForOrganisation(anyString(), any()))
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
    }

    private static Stream<Arguments> testSolicitorChangeScenarios() {
        Address oldAddress = buildAddress();
        Address newAddress = Address.builder().addressLine1("OtherLine1").postCode("ZZ1 1ZZ").build();
        Address addressWithSpace = buildAddress();
        addressWithSpace.setPostCode("EC1 3AS ");

        return Stream.of(
            // 1. Both applicant and respondent changed (solicitor details)
            Arguments.of(
                buildContactDetailsWrapper("Old AppSol", "Old Firm", "old@email.com", true, true,
                    "Old RespSol", "Old Firm", "old@email.com", false),
                buildContactDetailsWrapper("New AppSol", "New Firm", "new@email.com", true, true,
                    "New RespSol", "New Firm", "new@email.com", false),
                true, true
            ),
            // 2. No changes
            Arguments.of(
                buildContactDetailsWrapper("Old AppSol", "Old Firm", "old@email.com", true, true,
                    "Old RespSol", "Old Firm", "old@email.com", false),
                buildContactDetailsWrapper("Old AppSol", "Old Firm", "old@email.com", true, true,
                    "Old RespSol", "Old Firm", "old@email.com", false),
                false, false
            ),
            // 3. Only applicant changed
            Arguments.of(
                buildContactDetailsWrapper("Old AppSol", "Old Firm", "old@email.com", true, true,
                    "Old RespSol", "Old Firm", "old@email.com", false),
                buildContactDetailsWrapper("New AppSol", "New Firm", "new@email.com", true, true,
                    "Old RespSol", "Old Firm", "old@email.com", false),
                true, false
            ),
            // 4. Only respondent changed
            Arguments.of(
                buildContactDetailsWrapper("Old AppSol", "Old Firm", "old@email.com", true, true,
                    "Old RespSol", "Old Firm", "old@email.com", false),
                buildContactDetailsWrapper("Old AppSol", "Old Firm", "old@email.com", true, true,
                    "New RespSol", "New Firm", "new@email.com", false),
                false, true
            ),
            // 5. Case-insensitive name change (triggers cover sheet due to current implementation)
            Arguments.of(
                buildContactDetailsWrapper("old appsol", "old firm", "old@email.com", true, false,
                    null, null, null, false),
                buildContactDetailsWrapper("OLD APPSOL", "OLD FIRM", "old@email.com", true, false,
                    null, null, null, false),
                true, false
            ),
            // 6. Address changes
            Arguments.of(
                buildContactDetailsWrapper("App", "Firm", "a@e.com", oldAddress, "Resp", "Firm", oldAddress),
                buildContactDetailsWrapper("App", "Firm", "a@e.com", newAddress, "Resp", "Firm", newAddress),
                true, true
            ),
            // 7. Address change with whitespace (triggers cover sheet due to current implementation)
            Arguments.of(
                buildContactDetailsWrapper("App", "Firm", "a@e.com", oldAddress, "Resp", "Firm", oldAddress),
                buildContactDetailsWrapper("App", "Firm", "a@e.com", addressWithSpace, "Resp", "Firm", oldAddress),
                true, false
            ),
            // 8. Applicant address becomes null
            Arguments.of(
                buildContactDetailsWrapper("App", "Firm", "a@e.com", oldAddress, "Resp", "Firm", oldAddress),
                buildContactDetailsWrapper("App", "Firm", "a@e.com", null, "Resp", "Firm", oldAddress),
                true, false
            )
        );
    }
}
