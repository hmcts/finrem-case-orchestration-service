package uk.gov.hmcts.reform.finrem.caseorchestration.handler.managebarrister;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import java.util.stream.Stream;

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

    @ParameterizedTest
    @MethodSource("caseworkerUserBarristerData")
    void givenUserIsCaseworkerAndBarristerEmailsAreValid_whenHandle_thenReturnNoErrors(CaseRole barristerCaseRole,
                                                                                       BarristerParty barristerParty) {
        FinremCallbackRequest callbackRequest = createCallbackRequest(barristerParty);
        mockCaseRoleService(CaseRole.CASEWORKER);
        mockManageBarristerService(callbackRequest, CaseRole.CASEWORKER, barristerParty, barristerCaseRole);

        mockBarristerValidationServiceNoErrors(barristerCaseRole, TEST_SYSTEM_TOKEN);
        when(systemUserService.getSysUserToken()).thenReturn(TEST_SYSTEM_TOKEN);

        var response = manageBarristerMidEventHandler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getErrors()).isEmpty();
    }

    private static Stream<Arguments> caseworkerUserBarristerData() {
        return Stream.of(
            Arguments.of(CaseRole.APP_BARRISTER, BarristerParty.APPLICANT),
            Arguments.of(CaseRole.RESP_BARRISTER, BarristerParty.RESPONDENT)
        );
    }

    @ParameterizedTest
    @MethodSource("solicitorUserBarristerData")
    void givenUserIsSolicitorAndBarristerEmailsAreValid_whenHandle_thenReturnNoErrors(CaseRole userCaseRole,
                                                                                      CaseRole barristerCaseRole,
                                                                                      BarristerParty barristerParty) {
        FinremCallbackRequest callbackRequest = createCallbackRequest(null);
        mockCaseRoleService(userCaseRole);
        mockManageBarristerService(callbackRequest, userCaseRole, barristerParty, barristerCaseRole);
        mockBarristerValidationServiceNoErrors(barristerCaseRole, AUTH_TOKEN);

        var response = manageBarristerMidEventHandler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getErrors()).isEmpty();
    }

    private static Stream<Arguments> solicitorUserBarristerData() {
        return Stream.of(
            Arguments.of(CaseRole.APP_SOLICITOR, CaseRole.APP_BARRISTER, BarristerParty.APPLICANT),
            Arguments.of(CaseRole.RESP_SOLICITOR, CaseRole.RESP_BARRISTER, BarristerParty.RESPONDENT),
            Arguments.of(CaseRole.INTVR_SOLICITOR_1, CaseRole.INTVR_BARRISTER_1, BarristerParty.INTERVENER1),
            Arguments.of(CaseRole.INTVR_SOLICITOR_2, CaseRole.INTVR_BARRISTER_2, BarristerParty.INTERVENER2),
            Arguments.of(CaseRole.INTVR_SOLICITOR_3, CaseRole.INTVR_BARRISTER_3, BarristerParty.INTERVENER3),
            Arguments.of(CaseRole.INTVR_SOLICITOR_4, CaseRole.INTVR_BARRISTER_4, BarristerParty.INTERVENER4)
        );
    }

    @Test
    void givenBarristerEmailsAreInvalid_whenHandle_thenReturnResponseWithErrors() {
        FinremCallbackRequest callbackRequest = createCallbackRequest(BarristerParty.APPLICANT);
        mockCaseRoleService(CaseRole.CASEWORKER);
        mockManageBarristerService(callbackRequest, CaseRole.CASEWORKER, BarristerParty.APPLICANT, CaseRole.APP_BARRISTER);
        mockBarristerValidationServiceErrors();
        when(systemUserService.getSysUserToken()).thenReturn(TEST_SYSTEM_TOKEN);

        var response = manageBarristerMidEventHandler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getErrors()).containsExactly("Invalid email");
    }

    @ParameterizedTest
    @MethodSource("caseworkerUserBarristerData")
    void givenBarristerPartySet_whenHandle_thenUsesSystemAuthToken(CaseRole barristerCaseRole, BarristerParty barristerParty) {
        FinremCallbackRequest callbackRequest = createCallbackRequest(barristerParty);
        mockCaseRoleService(CaseRole.CASEWORKER);
        mockManageBarristerService(callbackRequest, CaseRole.CASEWORKER, barristerParty, barristerCaseRole);
        mockBarristerValidationServiceNoErrors(barristerCaseRole, TEST_SYSTEM_TOKEN);
        when(systemUserService.getSysUserToken()).thenReturn(TEST_SYSTEM_TOKEN);

        var response = manageBarristerMidEventHandler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response).isNotNull();
    }

    @ParameterizedTest
    @MethodSource("solicitorUserBarristerData")
    void givenUserIsSolicitorAndBarristerPartyNotSet_whenHandle_thenDoesNotReturnError(CaseRole userCaseRole,
                                                                                       CaseRole barristerCaseRole,
                                                                                       BarristerParty barristerParty) {
        FinremCallbackRequest callbackRequest = createCallbackRequest(null);
        mockCaseRoleService(userCaseRole);
        mockManageBarristerService(callbackRequest, userCaseRole, barristerParty, barristerCaseRole);
        mockBarristerValidationServiceNoErrors(barristerCaseRole, AUTH_TOKEN);

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

    private void mockManageBarristerService(FinremCallbackRequest callbackRequest, CaseRole userCaseRole,
                                            BarristerParty barristerParty, CaseRole barristerCaseRole) {
        when(manageBarristerService.getManageBarristerParty(callbackRequest.getCaseDetails(), userCaseRole))
            .thenReturn(barristerParty);
        when(manageBarristerService.getEventBarristers(callbackRequest.getCaseDetails().getData(), barristerParty))
            .thenReturn(List.of());
        when(manageBarristerService.getBarristerCaseRole(barristerParty)).thenReturn(barristerCaseRole);
        when(manageBarristerService.getManageBarristerParty(callbackRequest.getCaseDetails(), userCaseRole))
            .thenReturn(barristerParty);
        when(manageBarristerService.getEventBarristers(callbackRequest.getCaseDetails().getData(), barristerParty))
            .thenReturn(List.of());
        when(manageBarristerService.getBarristerCaseRole(barristerParty)).thenReturn(barristerCaseRole);
    }

    private void mockCaseRoleService(CaseRole caseRole) {
        when(caseRoleService.getUserOrCaseworkerCaseRole(CASE_ID, AUTH_TOKEN)).thenReturn(caseRole);
    }

    private void mockBarristerValidationServiceNoErrors(CaseRole barristerCaseRole, String authToken) {
        when(barristerValidationService.validateBarristerEmails(anyList(), eq(authToken),
            eq(CASE_ID), eq(barristerCaseRole)))
            .thenReturn(Collections.emptyList());
    }

    private void mockBarristerValidationServiceErrors() {
        when(barristerValidationService.validateBarristerEmails(anyList(), eq(TEST_SYSTEM_TOKEN),
            eq(CASE_ID), eq(CaseRole.APP_BARRISTER)))
            .thenReturn(List.of("Invalid email"));
    }
}
