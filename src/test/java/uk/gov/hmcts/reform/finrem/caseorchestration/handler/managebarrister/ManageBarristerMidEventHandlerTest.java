package uk.gov.hmcts.reform.finrem.caseorchestration.handler.managebarrister;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseRoleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.BarristerValidationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.ManageBarristerService;
import uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SYSTEM_TOKEN;

@ExtendWith(MockitoExtension.class)
class ManageBarristerMidEventHandlerTest {

    @InjectMocks
    private ManageBarristerMidEventHandler manageBarristerMidEventHandler;
    @Mock
    private ManageBarristerService manageBarristerService;
    @Mock
    private BarristerValidationService barristerValidationService;
    @Mock
    private SystemUserService systemUserService;
    @Mock
    private CaseRoleService caseRoleService;

    @Test
    void testCanHandle() {
        Assertions.assertCanHandle(manageBarristerMidEventHandler, CallbackType.MID_EVENT, CaseType.CONTESTED,
            EventType.MANAGE_BARRISTER);
    }

    @Test
    void givenBarristerEmailsAreValid_whenHandle_thenReturnResponseWithNoErrors() {
        FinremCallbackRequest callbackRequest = createCallbackRequest(BarristerParty.APPLICANT);
        mockCaseRoleService(CaseRole.CASEWORKER);
        mockManageBarristerService(callbackRequest, CaseRole.CASEWORKER);
        mockBarristerValidationServiceNoErrors(CaseRole.CASEWORKER, TEST_SYSTEM_TOKEN);
        when(systemUserService.getSysUserToken()).thenReturn(TEST_SYSTEM_TOKEN);

        var response = manageBarristerMidEventHandler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void givenBarristerEmailsAreInvalid_whenHandle_thenReturnResponseWithErrors() {
        FinremCallbackRequest callbackRequest = createCallbackRequest(BarristerParty.APPLICANT);
        mockCaseRoleService(CaseRole.CASEWORKER);
        mockManageBarristerService(callbackRequest, CaseRole.CASEWORKER);
        mockBarristerValidationServiceErrors();
        when(systemUserService.getSysUserToken()).thenReturn(TEST_SYSTEM_TOKEN);

        var response = manageBarristerMidEventHandler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getErrors()).containsExactly("Invalid email");
    }

    @Test
    void givenBarristerPartySet_whenHandle_thenUsesSystemAuthToken() {
        FinremCallbackRequest callbackRequest = createCallbackRequest(BarristerParty.APPLICANT);
        mockCaseRoleService(CaseRole.CASEWORKER);
        mockManageBarristerService(callbackRequest, CaseRole.CASEWORKER);
        mockBarristerValidationServiceNoErrors(CaseRole.CASEWORKER, TEST_SYSTEM_TOKEN);
        when(systemUserService.getSysUserToken()).thenReturn(TEST_SYSTEM_TOKEN);

        var response = manageBarristerMidEventHandler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response).isNotNull();
    }

    @Test
    void givenUserIsSolicitorAndBarristerPartyNotSet_whenHandle_thenDoesNotReturnError() {
        FinremCallbackRequest callbackRequest = createCallbackRequest(null);
        mockCaseRoleService(CaseRole.APP_SOLICITOR);
        mockManageBarristerService(callbackRequest, CaseRole.APP_SOLICITOR);
        mockBarristerValidationServiceNoErrors(CaseRole.APP_SOLICITOR, AUTH_TOKEN);

        var response = manageBarristerMidEventHandler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getErrors()).isEmpty();
        verifyNoInteractions(systemUserService);
    }

    @Test
    void givenUserIsCaseworkerAndBarristerPartyNotSet_whenHandle_thenReturnsError() {
        FinremCallbackRequest callbackRequest = createCallbackRequest(null);
        mockCaseRoleService(CaseRole.CASEWORKER);
        when(manageBarristerService.getManageBarristerParty(callbackRequest.getCaseDetails(), CaseRole.CASEWORKER))
            .thenReturn(null);

        var response = manageBarristerMidEventHandler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getErrors()).containsExactly("Select which party's barrister you want to manage");
    }

    private FinremCallbackRequest createCallbackRequest(BarristerParty barristerParty) {
        FinremCaseData caseData = FinremCaseData.builder()
            .barristerParty(barristerParty)
            .build();
        return FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), caseData);
    }

    private void mockManageBarristerService(FinremCallbackRequest callbackRequest, CaseRole userCaseRole) {
        when(manageBarristerService.getManageBarristerParty(callbackRequest.getCaseDetails(), userCaseRole))
            .thenReturn(BarristerParty.APPLICANT);
        when(manageBarristerService.getEventBarristers(callbackRequest.getCaseDetails().getData(), BarristerParty.APPLICANT))
            .thenReturn(List.of());
    }

    private void mockCaseRoleService(CaseRole caseRole) {
        when(caseRoleService.getUserOrCaseworkerCaseRole(CASE_ID, AUTH_TOKEN)).thenReturn(caseRole);
    }

    private void mockBarristerValidationServiceNoErrors(CaseRole caseRole, String authToken) {
        when(barristerValidationService.validateBarristerEmails(anyList(), eq(authToken),
            eq(CASE_ID), eq(caseRole.getCcdCode())))
            .thenReturn(Collections.emptyList());
    }

    private void mockBarristerValidationServiceErrors() {
        when(barristerValidationService.validateBarristerEmails(anyList(), eq(TEST_SYSTEM_TOKEN),
            eq(CASE_ID), eq(CaseRole.CASEWORKER.getCcdCode())))
            .thenReturn(List.of("Invalid email"));
    }
}
