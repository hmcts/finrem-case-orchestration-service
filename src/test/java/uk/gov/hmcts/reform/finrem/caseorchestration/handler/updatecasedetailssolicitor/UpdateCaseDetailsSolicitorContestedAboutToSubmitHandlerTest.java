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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenerateCoverSheetService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationService;

import java.util.ArrayList;

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

    @Test
    void whenHandleWithNonNullCaseDetailsBeforeData_thenNoExceptionAndReturnsNotNull() {

        when(updateRepresentationService.validateEmailActiveForOrganisation(anyString(), any(), anyString())).thenReturn(new ArrayList<>());

        ContactDetailsWrapper beforeWrapper = ContactDetailsWrapper.builder()
            .applicantSolicitorName("Old AppSol Name")
            .applicantSolicitorFirm("Old AppSol Firm")
            .applicantSolicitorEmail("OldAppSol@email.com")
            .applicantSolicitorAddress(
                Address.builder()
                    .addressLine1("AddressLine1")
                    .addressLine2("AddressLine2")
                    .addressLine3("AddressLine3")
                    .county("County")
                    .country("Country")
                    .postTown("Town")
                    .postCode("EC1 3AS")
                    .build()
            )
            .respondentSolicitorName("Old RespSol Name")
            .respondentSolicitorFirm("Old RespSol Firm")
            .respondentSolicitorAddress(
                Address.builder()
                .addressLine1("AddressLine1")
                .addressLine2("AddressLine2")
                .addressLine3("AddressLine3")
                .county("County")
                .country("Country")
                .postTown("Town")
                .postCode("EC1 3AS")
                .build()
            )
            .currentUserIsApplicantSolicitor(YesOrNo.YES)
            .build();
        ContactDetailsWrapper afterWrapper = ContactDetailsWrapper.builder()
            .applicantSolicitorName("New AppSol Name")
            .applicantSolicitorFirm("New AppSol Firm")
            .applicantSolicitorEmail("NewAppSol@email.com")
            .applicantSolicitorAddress(
                Address.builder()
                    .addressLine1("AddressLine1")
                    .addressLine2("AddressLine2")
                    .addressLine3("AddressLine3")
                    .county("County")
                    .country("Country")
                    .postTown("Town")
                    .postCode("EC1 3AS")
                    .build()
            )
            .respondentSolicitorAddress(
                Address.builder()
                    .addressLine1("AddressLine1")
                    .addressLine2("AddressLine2")
                    .addressLine3("AddressLine3")
                    .county("County")
                    .country("Country")
                    .postTown("Town")
                    .postCode("EC1 3AS")
                    .build()
            )
            .currentUserIsApplicantSolicitor(YesOrNo.YES)
            .build();
        FinremCaseData beforeData = FinremCaseData.builder().contactDetailsWrapper(beforeWrapper).build();
        FinremCaseData afterData = FinremCaseData.builder().contactDetailsWrapper(afterWrapper).build();
        FinremCaseDetails beforeDetails = FinremCaseDetails.builder().data(beforeData).id(CASE_ID_IN_LONG).build();
        FinremCaseDetails afterDetails = FinremCaseDetails.builder().data(afterData).id(CASE_ID_IN_LONG).build();
        FinremCallbackRequest request = FinremCallbackRequest.builder()
            .caseDetails(afterDetails)
            .caseDetailsBefore(beforeDetails)
            .build();
        assertThat(underTest.handle(request, AUTH_TOKEN)).isNotNull();
    }

    @Test
    void shouldCallGenerateCoverSheetsForApplicantAndRespondentSolicitorChange() {

        when(updateRepresentationService.validateEmailActiveForOrganisation(anyString(), any(), anyString())).thenReturn(new ArrayList<>());

        // Arrange
        ContactDetailsWrapper beforeWrapper = ContactDetailsWrapper.builder()
            .applicantSolicitorName("Old AppSol Name")
            .applicantSolicitorFirm("Old AppSol Firm")
            .applicantSolicitorEmail("OldAppSol@email.com")
            .applicantSolicitorAddress(
                Address.builder()
                    .addressLine1("AddressLine1")
                    .addressLine2("AddressLine2")
                    .addressLine3("AddressLine3")
                    .county("County")
                    .country("Country")
                    .postTown("Town")
                    .postCode("EC1 3AS")
                    .build()
            )
            .respondentSolicitorName("Old RespSol Name")
            .respondentSolicitorFirm("Old RespSol Firm")
            .respondentSolicitorAddress(
                Address.builder()
                    .addressLine1("AddressLine1")
                    .addressLine2("AddressLine2")
                    .addressLine3("AddressLine3")
                    .county("County")
                    .country("Country")
                    .postTown("Town")
                    .postCode("EC1 3AS")
                    .build()
            )
            .currentUserIsApplicantSolicitor(YesOrNo.YES)
            .build();
        ContactDetailsWrapper afterWrapper = ContactDetailsWrapper.builder()
            .applicantSolicitorName("New AppSol Name")
            .applicantSolicitorFirm("New AppSol Firm")
            .applicantSolicitorEmail("NewAppSol@email.com")
            .applicantSolicitorAddress(
                Address.builder()
                    .addressLine1("AddressLine1")
                    .addressLine2("AddressLine2")
                    .addressLine3("AddressLine3")
                    .county("County")
                    .country("Country")
                    .postTown("Town")
                    .postCode("EC1 3AS")
                    .build()
            )
            .respondentSolicitorAddress(
                Address.builder()
                    .addressLine1("AddressLine1")
                    .addressLine2("AddressLine2")
                    .addressLine3("AddressLine3")
                    .county("County")
                    .country("Country")
                    .postTown("Town")
                    .postCode("EC1 3AS")
                    .build()
            )
            .currentUserIsApplicantSolicitor(YesOrNo.YES)
            .build();

        FinremCaseData beforeData = FinremCaseData.builder().contactDetailsWrapper(beforeWrapper).build();
        FinremCaseData afterData = FinremCaseData.builder().contactDetailsWrapper(afterWrapper).build();
        FinremCaseDetails beforeDetails = FinremCaseDetails.builder().data(beforeData).id(CASE_ID_IN_LONG).build();
        FinremCaseDetails afterDetails = FinremCaseDetails.builder().data(afterData).id(CASE_ID_IN_LONG).build();

        // Act
        underTest.handle(FinremCallbackRequest.builder()
            .caseDetails(afterDetails)
            .caseDetailsBefore(beforeDetails)
            .build(), AUTH_TOKEN);

        // Assert
        verify(generateCoverSheetService).generateAndSetApplicantCoverSheet(afterDetails, AUTH_TOKEN);
        verify(generateCoverSheetService).generateAndSetRespondentCoverSheet(afterDetails, AUTH_TOKEN);
    }

    @Test
    void whenSolicitorNameOrFirmNoChange_thenShouldNeverInvokeGenerateCoverSheetService() {
        when(updateRepresentationService.validateEmailActiveForOrganisation(anyString(), any(), anyString())).thenReturn(new ArrayList<>());

        ContactDetailsWrapper beforeWrapper = ContactDetailsWrapper.builder()
            .applicantSolicitorName("Old AppSol Name")
            .applicantSolicitorFirm("Old AppSol Firm")
            .applicantSolicitorEmail("OldAppSol@email.com")
            .applicantSolicitorAddress(
                Address.builder()
                    .addressLine1("AddressLine1")
                    .addressLine2("AddressLine2")
                    .addressLine3("AddressLine3")
                    .county("County")
                    .country("Country")
                    .postTown("Town")
                    .postCode("EC1 3AS")
                    .build()
            )
            .currentUserIsApplicantSolicitor(YesOrNo.YES)
            .build();

        ContactDetailsWrapper afterWrapper = ContactDetailsWrapper.builder()
            .applicantSolicitorName("Old AppSol Name")
            .applicantSolicitorFirm("Old AppSol Firm")
            .applicantSolicitorEmail("OldAppSol@email.com")
            .applicantSolicitorAddress(
                Address.builder()
                    .addressLine1("AddressLine1")
                    .addressLine2("AddressLine2")
                    .addressLine3("AddressLine3")
                    .county("County")
                    .country("Country")
                    .postTown("Town")
                    .postCode("EC1 3AS")
                    .build()
            )
            .currentUserIsApplicantSolicitor(YesOrNo.YES)
            .build();

        FinremCaseData beforeData = FinremCaseData.builder().contactDetailsWrapper(beforeWrapper).build();
        FinremCaseData afterData = FinremCaseData.builder().contactDetailsWrapper(afterWrapper).build();
        FinremCaseDetails beforeDetails = FinremCaseDetails.builder().data(beforeData).id(CASE_ID_IN_LONG).build();
        FinremCaseDetails afterDetails = FinremCaseDetails.builder().data(afterData).id(CASE_ID_IN_LONG).build();

        underTest.handle(FinremCallbackRequest.builder()
            .caseDetails(afterDetails)
            .caseDetailsBefore(beforeDetails)
            .build(), AUTH_TOKEN);

        // If case-only changes should trigger cover sheet generation:
        verify(generateCoverSheetService, never()).generateAndSetApplicantCoverSheet(afterDetails, AUTH_TOKEN);
    }

    @Test
    void whenSolicitorNameOrFirmDiffersOnlyByCase_thenShouldTreatAsChange() {
        when(updateRepresentationService.validateEmailActiveForOrganisation(anyString(), any(), anyString())).thenReturn(new ArrayList<>());

        ContactDetailsWrapper beforeWrapper = ContactDetailsWrapper.builder()
            .applicantSolicitorName("old appsol name")
            .applicantSolicitorFirm("old appsol firm")
            .applicantSolicitorEmail("OldAppSol@email.com")
            .applicantSolicitorAddress(
                Address.builder()
                    .addressLine1("AddressLine1")
                    .addressLine2("AddressLine2")
                    .addressLine3("AddressLine3")
                    .county("County")
                    .country("Country")
                    .postTown("Town")
                    .postCode("EC1 3AS")
                    .build()
            )
            .currentUserIsApplicantSolicitor(YesOrNo.YES)
            .build();

        ContactDetailsWrapper afterWrapper = ContactDetailsWrapper.builder()
            .applicantSolicitorName("OLD APPSOL NAME") // differs only by case
            .applicantSolicitorFirm("OLD APPSOL FIRM") // differs only by case
            .applicantSolicitorEmail("OldAppSol@email.com")
            .applicantSolicitorAddress(
                Address.builder()
                    .addressLine1("AddressLine1")
                    .addressLine2("AddressLine2")
                    .addressLine3("AddressLine3")
                    .county("County")
                    .country("Country")
                    .postTown("Town")
                    .postCode("EC1 3AS")
                    .build()
            )
            .currentUserIsApplicantSolicitor(YesOrNo.YES)
            .build();

        FinremCaseData beforeData = FinremCaseData.builder().contactDetailsWrapper(beforeWrapper).build();
        FinremCaseData afterData = FinremCaseData.builder().contactDetailsWrapper(afterWrapper).build();
        FinremCaseDetails beforeDetails = FinremCaseDetails.builder().data(beforeData).id(CASE_ID_IN_LONG).build();
        FinremCaseDetails afterDetails = FinremCaseDetails.builder().data(afterData).id(CASE_ID_IN_LONG).build();

        underTest.handle(FinremCallbackRequest.builder()
            .caseDetails(afterDetails)
            .caseDetailsBefore(beforeDetails)
            .build(), AUTH_TOKEN);

        // If case-only changes should trigger cover sheet generation:
        verify(generateCoverSheetService).generateAndSetApplicantCoverSheet(afterDetails, AUTH_TOKEN);
    }
}
