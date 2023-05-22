package uk.gov.hmcts.reform.finrem.caseorchestration.handler;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRolesResource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseAssignedRoleService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASEWORKER_ROLE_FIELD_SHOW_LABEL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdServiceTest.AUTH_TOKEN;


@ExtendWith(MockitoExtension.class)
class UploadContestedCaseDocumentsAboutToStartHandlerTest {

    private static final String USER_ID = "testUserId";
    public static final String CASE_ID = "1234567890";

    @InjectMocks
    private UploadContestedCaseDocumentsAboutToStartHandler handler;
    @Mock
    private CaseAssignedRoleService caseAssignedRoleService;


    private CallbackRequest callbackRequest;

    @BeforeEach
    public void setUp() throws Exception {
        Map<String, Object> caseData = new HashMap<>();
        callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(Long.parseLong(CASE_ID))
                .data(caseData)
                .build())
            .build();
    }

    @Test
    void givenHandlerCanHandleCallback_whenCanHandle_thenReturnTrue() {
        assertThat(handler.canHandle(
                CallbackType.ABOUT_TO_START,
                CaseType.CONTESTED,
                EventType.UPLOAD_CASE_FILES),
            is(true));
    }

    @Test
    void givenInvalidCallbackType_whenCanHandle_thenReturnFalse() {
        assertThat(handler.canHandle(
                CallbackType.ABOUT_TO_SUBMIT,
                CaseType.CONTESTED,
                EventType.UPLOAD_CASE_FILES),
            is(false));
    }

    @Test
    void givenInvalidCaseType_whenCanHandle_thenReturnFalse() {
        assertThat(handler.canHandle(
                CallbackType.ABOUT_TO_START,
                CaseType.CONSENTED,
                EventType.UPLOAD_CASE_FILES),
            is(false));
    }

    @Test
    void givenInvalidEventType_whenCanHandle_thenReturnFalse() {
        assertThat(handler.canHandle(
                CallbackType.ABOUT_TO_START,
                CaseType.CONTESTED,
                EventType.SEND_ORDER),
            is(false));
    }

    @Test
    void givenUserIsApplicantSolicitor_whenHandle_thenSetCurrentUserCaseRoleToAppSolicitor() {
        when(caseAssignedRoleService.getCaseAssignedUserRole(callbackRequest.getCaseDetails(), AUTH_TOKEN))
            .thenReturn(getCaseAssignedUserRolesResource(APP_SOLICITOR_POLICY));

        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>>
            response = handler.handle(callbackRequest, AUTH_TOKEN);
        String expected = APP_SOLICITOR_POLICY
            .replace("[", "").replace("]","");
        assertThat(response.getData().get(CASE_ROLE), is(expected));
    }

    @Test
    void givenUserIsRespondentSolicitor_whenHandle_thenSetCurrentUserCaseRoleToRespSolicitor() {
        when(caseAssignedRoleService.getCaseAssignedUserRole(callbackRequest.getCaseDetails(), AUTH_TOKEN))
            .thenReturn(getCaseAssignedUserRolesResource(RESP_SOLICITOR_POLICY));

        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>>
            response = handler.handle(callbackRequest, AUTH_TOKEN);

        String expected = RESP_SOLICITOR_POLICY
            .replace("[", "").replace("]","");
        assertThat(response.getData().get(CASE_ROLE), is(expected));
    }

    @Test
    void givenUserIsCaseWorker_whenHandle_thenSetCurrentUserCaseRoleToCaseWorker() {
        when(caseAssignedRoleService.getCaseAssignedUserRole(callbackRequest.getCaseDetails(), AUTH_TOKEN))
            .thenReturn(CaseAssignedUserRolesResource.builder().build());

        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>>
            response = handler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getData().get(CASE_ROLE), is(CASEWORKER_ROLE_FIELD_SHOW_LABEL));
    }

    private CaseAssignedUserRolesResource getCaseAssignedUserRolesResource(String caseRole) {
        return CaseAssignedUserRolesResource.builder()
            .caseAssignedUserRoles(List.of(CaseAssignedUserRole.builder()
                .userId(USER_ID)
                .caseRole(caseRole)
                .caseDataId(CASE_ID)
                .build()))
            .build();
    }
}