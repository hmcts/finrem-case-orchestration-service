package uk.gov.hmcts.reform.finrem.caseorchestration.handler.updatecasedetailssolicitor;

import org.junit.jupiter.api.BeforeEach;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationService;

import java.lang.reflect.Field;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID_IN_LONG;
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

    @Mock
    private UpdateRepresentationService updateRepresentationService;

    @BeforeEach
    void injectMocks() throws Exception {
        Field field = handler.getClass().getSuperclass().getDeclaredField("updateRepresentationService");
        field.setAccessible(true);
        field.set(handler, updateRepresentationService);
        // Correctly stub the void method
        org.mockito.Mockito.doNothing().when(updateRepresentationService)
            .validateEmailActiveForOrganisation(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
    }

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
        ContactDetailsWrapper beforeWrapper = ContactDetailsWrapper.builder()
            .applicantSolicitorName("A")
            .applicantSolicitorFirm("Firm1")
            .currentUserIsApplicantSolicitor(uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo.YES)
            .build();
        ContactDetailsWrapper afterWrapper = ContactDetailsWrapper.builder()
            .applicantSolicitorName("A")
            .applicantSolicitorFirm("Firm1")
            .currentUserIsApplicantSolicitor(uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo.YES)
            .build();
        FinremCaseData beforeData = FinremCaseData.builder().contactDetailsWrapper(beforeWrapper).build();
        FinremCaseData afterData = FinremCaseData.builder().contactDetailsWrapper(afterWrapper).build();
        FinremCaseDetails beforeDetails = FinremCaseDetails.builder().data(beforeData).id(CASE_ID_IN_LONG).build();
        FinremCaseDetails afterDetails = FinremCaseDetails.builder().data(afterData).id(CASE_ID_IN_LONG).build();
        FinremCallbackRequest request = FinremCallbackRequest.builder()
            .caseDetails(afterDetails)
            .caseDetailsBefore(beforeDetails)
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
            .currentUserIsApplicantSolicitor(uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo.YES)
            .build();
        ContactDetailsWrapper afterWrapper = ContactDetailsWrapper.builder()
            .applicantSolicitorName("New AppSol Name")
            .applicantSolicitorFirm("New AppSol Firm")
            .applicantSolicitorAddress(applicantSolicitorAddressAfter)
            .respondentSolicitorName("New RespSol Name")
            .respondentSolicitorFirm("New RespSol Firm")
            .respondentSolicitorAddress(respondentSolicitorAddressAfter)
            .currentUserIsApplicantSolicitor(uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo.YES)
            .build();

        FinremCaseData beforeData = FinremCaseData.builder().contactDetailsWrapper(beforeWrapper).build();
        FinremCaseData afterData = FinremCaseData.builder().contactDetailsWrapper(afterWrapper).build();
        FinremCaseDetails beforeDetails = FinremCaseDetails.builder().data(beforeData).id(CASE_ID_IN_LONG).build();
        FinremCaseDetails afterDetails = FinremCaseDetails.builder().data(afterData).id(CASE_ID_IN_LONG).build();

        // Act
        handler.handle(FinremCallbackRequest.builder()
            .caseDetails(afterDetails)
            .caseDetailsBefore(beforeDetails)
            .build(), AUTH_TOKEN);

        // Assert
        verify(generateCoverSheetService).generateAndSetApplicantCoverSheet(afterDetails, AUTH_TOKEN);
        verify(generateCoverSheetService).generateAndSetRespondentCoverSheet(afterDetails, AUTH_TOKEN);
    }
}
