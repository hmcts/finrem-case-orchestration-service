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
import uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRolesResource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseAssignedRoleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASEWORKER_ROLE_FIELD_SHOW_LABEL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdServiceTest.AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class ManageBarristerAboutToStartHandlerTest {

    @InjectMocks
    private ManageBarristerAboutToStartHandler manageBarristerAboutToStartHandler;

    @Mock
    private CaseAssignedRoleService caseAssignedRoleService;

    @Test
    void testCanHandle() {
        Assertions.assertCanHandle(manageBarristerAboutToStartHandler, CallbackType.ABOUT_TO_START, CaseType.CONTESTED,
            EventType.MANAGE_BARRISTER);
    }

    @ParameterizedTest
    @MethodSource
    void givenUserCaseRole_whenHandle_thenInitialisesEventData(CaseRole caseRole, String expectedCaseRoleLabel) {
        mockCaseAssignRoleService(caseRole);
        FinremCallbackRequest callbackRequest = createCallbackRequest();

        var response = manageBarristerAboutToStartHandler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData caseData = response.getData();
        assertThat(caseData.getCurrentUserCaseRole()).isEqualTo(caseRole);
        assertThat(caseData.getCurrentUserCaseRoleLabel()).isEqualTo(expectedCaseRoleLabel);
    }

    private static Stream<Arguments> givenUserCaseRole_whenHandle_thenInitialisesEventData() {
        return Stream.of(
            Arguments.of(CaseRole.APP_SOLICITOR, "APPSOLICITOR"),
            Arguments.of(CaseRole.RESP_SOLICITOR, "RESPSOLICITOR"),
            Arguments.of(CaseRole.CASEWORKER, CASEWORKER_ROLE_FIELD_SHOW_LABEL),
            Arguments.of(CaseRole.INTVR_SOLICITOR_1, "INTVRSOLICITOR1"),
            Arguments.of(CaseRole.INTVR_SOLICITOR_2, "INTVRSOLICITOR2"),
            Arguments.of(CaseRole.INTVR_SOLICITOR_3, "INTVRSOLICITOR3"),
            Arguments.of(CaseRole.INTVR_SOLICITOR_4, "INTVRSOLICITOR4")
        );
    }

    private void mockCaseAssignRoleService(CaseRole caseRole) {
        CaseAssignedUserRolesResource caseAssignedUserRolesResource = CaseAssignedUserRolesResource.builder()
            .caseAssignedUserRoles(List.of(CaseAssignedUserRole.builder()
                .userId("testUserId")
                .caseRole(caseRole.getCcdCode())
                .caseDataId(CASE_ID)
                .build()))
            .build();

        when(caseAssignedRoleService.getCaseAssignedUserRole(CASE_ID, AUTH_TOKEN))
            .thenReturn(caseAssignedUserRolesResource);
    }

    private FinremCallbackRequest createCallbackRequest() {
        FinremCaseData caseData = FinremCaseData.builder()
            .build();
        return FinremCallbackRequestFactory.create(Long.valueOf(TestConstants.CASE_ID), CaseType.CONTESTED,
            EventType.MANAGE_BARRISTER, caseData);
    }
}
