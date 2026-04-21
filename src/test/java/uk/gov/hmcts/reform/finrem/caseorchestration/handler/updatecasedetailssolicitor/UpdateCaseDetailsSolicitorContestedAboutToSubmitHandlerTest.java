package uk.gov.hmcts.reform.finrem.caseorchestration.handler.updatecasedetailssolicitor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenerateCoverSheetService;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.UPDATE_CASE_DETAILS_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

// Common methods tested in Abstract test class.

@ExtendWith(MockitoExtension.class)
class UpdateCaseDetailsSolicitorContestedAboutToSubmitHandlerTest {

    @InjectMocks
    private UpdateCaseDetailsSolicitorContestedAboutToSubmitHandler handler;

    @Mock
    private GenerateCoverSheetService generateCoverSheetService;

    @Test
    void testCanHandle() {
        assertCanHandle(handler,
            Arguments.of(ABOUT_TO_SUBMIT, CONTESTED, UPDATE_CASE_DETAILS_SOLICITOR),
            Arguments.of(ABOUT_TO_SUBMIT, CONSENTED, UPDATE_CASE_DETAILS_SOLICITOR)
        );
    }

    @Test
    void when_handle_then_shouldNotClearTemporaryFields() {
        assertThat(handler.clearsTemporaryFields()).isTrue();
    }

    @Test
    void whenHandleWithNonNullCaseDetailsBeforeData_thenNoExceptionAndReturnsNotNull() {
        ContactDetailsWrapper wrapper = ContactDetailsWrapper.builder()
            .applicantSolicitorName("A")
            .applicantSolicitorFirm("Firm1")
            .build();
        FinremCaseData finremCaseData = FinremCaseData.builder().contactDetailsWrapper(wrapper).build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().data(finremCaseData).build();
        FinremCallbackRequest request = FinremCallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetails)
            .build();
        assertThat(handler.handle(request, AUTH_TOKEN)).isNotNull();
    }

    @Test
    void shouldCallGenerateCoverSheetsForApplicantAndRespondentSolicitorChange() {
        // Arrange
        Address applicantSolicitorAddressBefore = Address.builder().addressLine1("Old AppSol Address").build();
        Address applicantSolicitorAddressAfter = Address.builder().addressLine1("New AppSol Address").build();
        Address respondentSolicitorAddressBefore = Address.builder().addressLine1("Old RespSol Address").build();
        Address respondentSolicitorAddressAfter = Address.builder().addressLine1("New RespSol Address").build();

        ContactDetailsWrapper beforeWrapper = ContactDetailsWrapper.builder()
            .applicantSolicitorName("Old AppSol Name")
            .applicantSolicitorFirm("Old AppSol Firm")
            .applicantSolicitorAddress(applicantSolicitorAddressBefore)
            .respondentSolicitorName("Old RespSol Name")
            .respondentSolicitorFirm("Old RespSol Firm")
            .respondentSolicitorAddress(respondentSolicitorAddressBefore)
            .build();
        ContactDetailsWrapper afterWrapper = ContactDetailsWrapper.builder()
            .applicantSolicitorName("New AppSol Name")
            .applicantSolicitorFirm("New AppSol Firm")
            .applicantSolicitorAddress(applicantSolicitorAddressAfter)
            .respondentSolicitorName("New RespSol Name")
            .respondentSolicitorFirm("New RespSol Firm")
            .respondentSolicitorAddress(respondentSolicitorAddressAfter)
            .build();

        FinremCaseData beforeData = FinremCaseData.builder().contactDetailsWrapper(beforeWrapper).build();
        FinremCaseData afterData = FinremCaseData.builder().contactDetailsWrapper(afterWrapper).build();
        FinremCaseDetails beforeDetails = FinremCaseDetails.builder().data(beforeData).build();
        FinremCaseDetails afterDetails = FinremCaseDetails.builder().data(afterData).build();

        // Act
        handler.handle(FinremCallbackRequest.builder()
            .caseDetails(afterDetails)
            .caseDetailsBefore(beforeDetails)
            .build(), AUTH_TOKEN);

        // Assert
        verify(generateCoverSheetService).generateAndSetApplicantCoverSheet(afterDetails, AUTH_TOKEN);
        verify(generateCoverSheetService).generateAndSetRespondentCoverSheet(afterDetails, AUTH_TOKEN);
    }

    @Test
    void hasChangeApplicantSolicitorName_shouldDetectChangeAndNoChange() {
        ContactDetailsWrapper beforeWrapper = ContactDetailsWrapper.builder().applicantSolicitorName("A").build();
        ContactDetailsWrapper afterSameWrapper = ContactDetailsWrapper.builder().applicantSolicitorName("A").build();
        ContactDetailsWrapper afterDiffWrapper = ContactDetailsWrapper.builder().applicantSolicitorName("B").build();
        FinremCaseDetails before = FinremCaseDetails.builder()
            .data(FinremCaseData.builder().contactDetailsWrapper(beforeWrapper).build()).build();
        FinremCaseDetails afterSame = FinremCaseDetails.builder()
            .data(FinremCaseData.builder().contactDetailsWrapper(afterSameWrapper).build()).build();
        FinremCaseDetails afterDiff = FinremCaseDetails.builder()
            .data(FinremCaseData.builder().contactDetailsWrapper(afterDiffWrapper).build()).build();
        assertThat(handler.hasChangeApplicantSolicitorName(afterSame, before)).isFalse();
        assertThat(handler.hasChangeApplicantSolicitorName(afterDiff, before)).isTrue();
    }

    @Test
    void hasChangeApplicantSolicitorFirm_shouldDetectChangeAndNoChange() {
        ContactDetailsWrapper beforeWrapper = ContactDetailsWrapper.builder().applicantSolicitorFirm("Firm1").build();
        ContactDetailsWrapper afterSameWrapper = ContactDetailsWrapper.builder().applicantSolicitorFirm("Firm1").build();
        ContactDetailsWrapper afterDiffWrapper = ContactDetailsWrapper.builder().applicantSolicitorFirm("Firm2").build();
        FinremCaseDetails before = FinremCaseDetails.builder()
            .data(FinremCaseData.builder().contactDetailsWrapper(beforeWrapper).build()).build();
        FinremCaseDetails afterSame = FinremCaseDetails.builder()
            .data(FinremCaseData.builder().contactDetailsWrapper(afterSameWrapper).build()).build();
        FinremCaseDetails afterDiff = FinremCaseDetails.builder()
            .data(FinremCaseData.builder().contactDetailsWrapper(afterDiffWrapper).build()).build();
        assertThat(handler.hasChangeApplicantSolicitorFirm(afterSame, before)).isFalse();
        assertThat(handler.hasChangeApplicantSolicitorFirm(afterDiff, before)).isTrue();
    }

    @Test
    void hasChangeRespondentSolicitorName_shouldDetectChangeAndNoChange() {
        ContactDetailsWrapper beforeWrapper = ContactDetailsWrapper.builder().respondentSolicitorName("R").build();
        ContactDetailsWrapper afterSameWrapper = ContactDetailsWrapper.builder().respondentSolicitorName("R").build();
        ContactDetailsWrapper afterDiffWrapper = ContactDetailsWrapper.builder().respondentSolicitorName("S").build();
        FinremCaseDetails before = FinremCaseDetails.builder()
            .data(FinremCaseData.builder().contactDetailsWrapper(beforeWrapper).build()).build();
        FinremCaseDetails afterSame = FinremCaseDetails.builder()
            .data(FinremCaseData.builder().contactDetailsWrapper(afterSameWrapper).build()).build();
        FinremCaseDetails afterDiff = FinremCaseDetails.builder()
            .data(FinremCaseData.builder().contactDetailsWrapper(afterDiffWrapper).build()).build();
        assertThat(handler.hasChangeRespondentSolicitorName(afterSame, before)).isFalse();
        assertThat(handler.hasChangeRespondentSolicitorName(afterDiff, before)).isTrue();
    }

    @Test
    void hasChangeRespondentSolicitorFirm_shouldDetectChangeAndNoChange() {
        ContactDetailsWrapper beforeWrapper = ContactDetailsWrapper.builder().respondentSolicitorFirm("FirmA").build();
        ContactDetailsWrapper afterSameWrapper = ContactDetailsWrapper.builder().respondentSolicitorFirm("FirmA").build();
        ContactDetailsWrapper afterDiffWrapper = ContactDetailsWrapper.builder().respondentSolicitorFirm("FirmB").build();
        FinremCaseDetails before = FinremCaseDetails.builder()
            .data(FinremCaseData.builder().contactDetailsWrapper(beforeWrapper).build()).build();
        FinremCaseDetails afterSame = FinremCaseDetails.builder()
            .data(FinremCaseData.builder().contactDetailsWrapper(afterSameWrapper).build()).build();
        FinremCaseDetails afterDiff = FinremCaseDetails.builder()
            .data(FinremCaseData.builder().contactDetailsWrapper(afterDiffWrapper).build()).build();
        assertThat(handler.hasChangeRespondentSolicitorFirm(afterSame, before)).isFalse();
        assertThat(handler.hasChangeRespondentSolicitorFirm(afterDiff, before)).isTrue();
    }

    @Test
    void hasChangeApplicantSolicitorName_shouldBeCaseInsensitiveAndTrimmed() {
        ContactDetailsWrapper beforeWrapper = ContactDetailsWrapper.builder().applicantSolicitorName("  Alice  ").build();
        ContactDetailsWrapper afterWrapper = ContactDetailsWrapper.builder().applicantSolicitorName("alice").build();
        FinremCaseDetails before = FinremCaseDetails.builder()
            .data(FinremCaseData.builder().contactDetailsWrapper(beforeWrapper).build()).build();
        FinremCaseDetails after = FinremCaseDetails.builder()
            .data(FinremCaseData.builder().contactDetailsWrapper(afterWrapper).build()).build();
        assertThat(handler.hasChangeApplicantSolicitorName(after, before)).isFalse();
    }

    @Test
    void hasChangeApplicantSolicitorFirm_shouldBeCaseInsensitiveAndTrimmed() {
        ContactDetailsWrapper beforeWrapper = ContactDetailsWrapper.builder().applicantSolicitorFirm("  FirmX  ").build();
        ContactDetailsWrapper afterWrapper = ContactDetailsWrapper.builder().applicantSolicitorFirm("firmx").build();
        FinremCaseDetails before = FinremCaseDetails.builder()
            .data(FinremCaseData.builder().contactDetailsWrapper(beforeWrapper).build()).build();
        FinremCaseDetails after = FinremCaseDetails.builder()
            .data(FinremCaseData.builder().contactDetailsWrapper(afterWrapper).build()).build();
        assertThat(handler.hasChangeApplicantSolicitorFirm(after, before)).isFalse();
    }

    @Test
    void hasChangeRespondentSolicitorName_shouldBeCaseInsensitiveAndTrimmed() {
        ContactDetailsWrapper beforeWrapper = ContactDetailsWrapper.builder().respondentSolicitorName("  Bob  ").build();
        ContactDetailsWrapper afterWrapper = ContactDetailsWrapper.builder().respondentSolicitorName("bob").build();
        FinremCaseDetails before = FinremCaseDetails.builder()
            .data(FinremCaseData.builder().contactDetailsWrapper(beforeWrapper).build()).build();
        FinremCaseDetails after = FinremCaseDetails.builder()
            .data(FinremCaseData.builder().contactDetailsWrapper(afterWrapper).build()).build();
        assertThat(handler.hasChangeRespondentSolicitorName(after, before)).isFalse();
    }

    @Test
    void hasChangeRespondentSolicitorFirm_shouldBeCaseInsensitiveAndTrimmed() {
        ContactDetailsWrapper beforeWrapper = ContactDetailsWrapper.builder().respondentSolicitorFirm("  FirmY  ").build();
        ContactDetailsWrapper afterWrapper = ContactDetailsWrapper.builder().respondentSolicitorFirm("firmy").build();
        FinremCaseDetails before = FinremCaseDetails.builder()
            .data(FinremCaseData.builder().contactDetailsWrapper(beforeWrapper).build()).build();
        FinremCaseDetails after = FinremCaseDetails.builder()
            .data(FinremCaseData.builder().contactDetailsWrapper(afterWrapper).build()).build();
        assertThat(handler.hasChangeRespondentSolicitorFirm(after, before)).isFalse();
    }
}
