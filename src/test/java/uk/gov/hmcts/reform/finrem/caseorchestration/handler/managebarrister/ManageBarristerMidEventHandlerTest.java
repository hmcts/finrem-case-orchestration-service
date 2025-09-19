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
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.IntervenerServiceTest.CASE_ID;

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

    @Test
    void testCanHandle() {
        Assertions.assertCanHandle(manageBarristerMidEventHandler, CallbackType.MID_EVENT, CaseType.CONTESTED,
            EventType.MANAGE_BARRISTER);
    }

    @Test
    void givenBarristerEmailsAreValid_whenHandle_thenReturnResponseWithNoErrors() {
        FinremCallbackRequest callbackRequest = createCallbackRequest(BarristerParty.APPLICANT);
        mockManageBarristerService(callbackRequest);
        when(systemUserService.getSysUserToken()).thenReturn("systemUserToken");
        when(barristerValidationService.validateBarristerEmails(anyList(), eq("systemUserToken"),
            eq(String.valueOf(CASE_ID)), eq(CaseRole.APP_SOLICITOR.getCcdCode())))
            .thenReturn(Collections.emptyList());

        var response = manageBarristerMidEventHandler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void givenBarristerEmailsAreInvalid_whenHandle_thenReturnResponseWithErrors() {
        FinremCallbackRequest callbackRequest = createCallbackRequest(BarristerParty.APPLICANT);
        mockManageBarristerService(callbackRequest);
        when(systemUserService.getSysUserToken()).thenReturn("systemUserToken");
        when(barristerValidationService.validateBarristerEmails(anyList(), eq("systemUserToken"),
            eq(String.valueOf(CASE_ID)), eq(CaseRole.APP_SOLICITOR.getCcdCode())))
            .thenReturn(List.of("Invalid email"));

        var response = manageBarristerMidEventHandler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getErrors()).containsExactly("Invalid email");
    }

    @Test
    void givenBarristerPartySet_whenHandle_thenUsesSystemAuthToken() {
        FinremCallbackRequest callbackRequest = createCallbackRequest(BarristerParty.APPLICANT);
        mockManageBarristerService(callbackRequest);
        when(systemUserService.getSysUserToken()).thenReturn("systemUserToken");
        when(barristerValidationService.validateBarristerEmails(anyList(), eq("systemUserToken"),
            eq(String.valueOf(CASE_ID)), eq(CaseRole.APP_SOLICITOR.getCcdCode())))
            .thenReturn(List.of("Invalid email"));

        var response = manageBarristerMidEventHandler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response).isNotNull();
    }

    @Test
    void givenBarristerPartyNotSet_whenHandle_thenDoesNotUseSystemAuthToken() {
        FinremCallbackRequest callbackRequest = createCallbackRequest(null);
        mockManageBarristerService(callbackRequest);
        when(barristerValidationService.validateBarristerEmails(anyList(), eq(AUTH_TOKEN),
            eq(String.valueOf(CASE_ID)), eq(CaseRole.APP_SOLICITOR.getCcdCode())))
            .thenReturn(List.of("Invalid email"));

        var response = manageBarristerMidEventHandler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response).isNotNull();
        verifyNoInteractions(systemUserService);
    }

    private FinremCallbackRequest createCallbackRequest(BarristerParty barristerParty) {
        FinremCaseData caseData = FinremCaseData.builder()
            .barristerParty(barristerParty)
            .build();
        return FinremCallbackRequestFactory.from(CASE_ID, caseData);
    }

    private void mockManageBarristerService(FinremCallbackRequest callbackRequest) {
        when(manageBarristerService.getManageBarristerParty(callbackRequest.getCaseDetails(), AUTH_TOKEN))
            .thenReturn(BarristerParty.APPLICANT);
        when(manageBarristerService.getEventBarristers(callbackRequest.getCaseDetails().getData(), BarristerParty.APPLICANT))
            .thenReturn(List.of());
        when(manageBarristerService.getCaseRole(CASE_ID, AUTH_TOKEN)).thenReturn(CaseRole.APP_SOLICITOR);
    }
}
