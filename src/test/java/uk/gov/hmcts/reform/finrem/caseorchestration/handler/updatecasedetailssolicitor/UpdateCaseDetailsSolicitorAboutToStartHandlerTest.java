package uk.gov.hmcts.reform.finrem.caseorchestration.handler.updatecasedetailssolicitor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRolesResource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseAssignedRoleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class UpdateCaseDetailsSolicitorAboutToStartHandlerTest {

    @InjectMocks
    private UpdateCaseDetailsSolicitorAboutToStartHandler handler;

    @Mock
    private CaseAssignedRoleService caseAssignedRoleService;

    @Mock
    private CaseDataService caseDataService;

    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Test
    void canHandle() {
        assertCanHandle(handler,
            Arguments.of(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.UPDATE_CASE_DETAILS_SOLICITOR),
            Arguments.of(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.UPDATE_CASE_DETAILS_SOLICITOR)
        );
    }

    @Test
    void givenUserIsApplicantSolicitorWhenHandleThenUpdateCaseData() {
        FinremCaseData caseData = setUpCaseData();
        FinremCallbackRequest callbackRequest = setUpCallbackRequest(caseData);
        CaseAssignedUserRolesResource resource = CaseAssignedUserRolesResource.builder()
                .caseAssignedUserRoles(List.of(CaseAssignedUserRole.builder().caseRole(APP_SOLICITOR_POLICY).build())).build();

        when(caseAssignedRoleService.getCaseAssignedUserRole(callbackRequest.getCaseDetails().getId().toString(), AUTH_TOKEN))
            .thenReturn(resource);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            handler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getData().getCurrentUserCaseRoleLabel()).isEqualTo("APPSOLICITOR");
        assertThat(response.getData().getContactDetailsWrapper().getApplicantRepresented()).isEqualTo(YesOrNo.YES);

    }

    @Test
    void givenUserIsConsentedRespondentSolicitorWhenHandleThenUpdateCaseData() {
        FinremCaseData caseData = setUpCaseData();
        FinremCallbackRequest callbackRequest = setUpCallbackRequest(caseData);

        CaseAssignedUserRolesResource resource = CaseAssignedUserRolesResource.builder()
            .caseAssignedUserRoles(List.of(CaseAssignedUserRole.builder().caseRole(RESP_SOLICITOR_POLICY).build())).build();

        when(caseAssignedRoleService.getCaseAssignedUserRole(callbackRequest.getCaseDetails().getId().toString(), AUTH_TOKEN))
            .thenReturn(resource);
        when(caseDataService.isConsentedApplication(callbackRequest.getCaseDetails())).thenReturn(true);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            handler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getData().getCurrentUserCaseRoleLabel()).isEqualTo("RESPSOLICITOR");
        assertThat(response.getData().getContactDetailsWrapper().getConsentedRespondentRepresented()).isEqualTo(YesOrNo.YES);
    }

    @Test
    void givenUserIsContestedRespondentSolicitorWhenHandleThenUpdateCaseData() {
        FinremCaseData caseData = setUpCaseData();
        FinremCallbackRequest callbackRequest = setUpCallbackRequest(caseData);

        CaseAssignedUserRolesResource resource = CaseAssignedUserRolesResource.builder()
            .caseAssignedUserRoles(List.of(CaseAssignedUserRole.builder().caseRole(RESP_SOLICITOR_POLICY).build())).build();

        when(caseAssignedRoleService.getCaseAssignedUserRole(callbackRequest.getCaseDetails().getId().toString(), AUTH_TOKEN))
            .thenReturn(resource);
        when(caseDataService.isConsentedApplication(callbackRequest.getCaseDetails())).thenReturn(false);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            handler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getData().getCurrentUserCaseRoleLabel()).isEqualTo("RESPSOLICITOR");
        assertThat(response.getData().getContactDetailsWrapper().getContestedRespondentRepresented()).isEqualTo(YesOrNo.YES);
    }

    private FinremCaseData setUpCaseData() {
        return FinremCaseData.builder()
            .contactDetailsWrapper(ContactDetailsWrapper.builder().build())
            .build();
    }

    private FinremCallbackRequest setUpCallbackRequest(FinremCaseData caseData) {
        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .id(12345L)
            .data(caseData)
            .build();
        return FinremCallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
    }
}
