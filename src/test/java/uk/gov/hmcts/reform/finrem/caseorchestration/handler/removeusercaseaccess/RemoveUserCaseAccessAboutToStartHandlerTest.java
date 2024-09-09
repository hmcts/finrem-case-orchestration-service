package uk.gov.hmcts.reform.finrem.caseorchestration.handler.removeusercaseaccess;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DataStoreClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.IdamAuthApi;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRolesResource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SERVICE_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.handler.removeusercaseaccess.RemoveUserCaseAccessAboutToStartHandler.CODE_VALUES_SEPARATOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class RemoveUserCaseAccessAboutToStartHandlerTest {

    @InjectMocks
    private RemoveUserCaseAccessAboutToStartHandler handler;
    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private DataStoreClient dataStoreClient;
    @Mock
    private IdamAuthApi idamAuthApi;

    @Test
    void testHandle() {
        assertCanHandle(handler,
            Arguments.of(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.REMOVE_USER_CASE_ACCESS),
            Arguments.of(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.REMOVE_USER_CASE_ACCESS)
        );
    }

    @Test
    void givenNoCaseUsersWhenHandleThenUserCaseAccessListIsEmpty() {
        Long caseId = 12345L;
        CaseAssignedUserRolesResource rolesResource = CaseAssignedUserRolesResource.builder()
            .caseAssignedUserRoles(Collections.emptyList())
            .build();
        when(dataStoreClient.getUserRoles(AUTH_TOKEN, TEST_SERVICE_TOKEN,String.valueOf(caseId), null))
            .thenReturn(rolesResource);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);

        FinremCallbackRequest request = FinremCallbackRequestFactory.fromId(caseId);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(request, AUTH_TOKEN);

        assertThat(response.getErrors()).isEmpty();
        assertThat(response.getWarnings()).isEmpty();

        DynamicList userCaseAccessList = response.getData().getUserCaseAccessList();
        assertThat(userCaseAccessList.getListItems()).isEmpty();
        assertThat(userCaseAccessList.getValue()).isNull();
    }

    @Test
    void givenCaseUsersExistWhenHandleThenUserCaseAccessListIsPopulated() {
        String caseId = "12345";

        UserDetails userDetails1 = createUserDetails("user1", "user1@test.com", "Aye", "One");
        UserDetails userDetails2 = createUserDetails("user2", "user2@test.com", "Bee", "Two");
        UserDetails userDetails3 = createUserDetails("user3", "user3@test.com", "Cee", "Three");
        when(idamAuthApi.getUserByUserId(AUTH_TOKEN, userDetails1.getId())).thenReturn(userDetails1);
        when(idamAuthApi.getUserByUserId(AUTH_TOKEN, userDetails2.getId())).thenReturn(userDetails2);
        when(idamAuthApi.getUserByUserId(AUTH_TOKEN, userDetails3.getId())).thenReturn(userDetails3);

        CaseAssignedUserRolesResource rolesResource = CaseAssignedUserRolesResource.builder()
            .caseAssignedUserRoles(List.of(
                createCaseAssignedUserRole(caseId, userDetails1.getId(), "[APPSOLICITOR]"),
                createCaseAssignedUserRole(caseId, userDetails2.getId(), "[RESPSOLICITOR]"),
                createCaseAssignedUserRole(caseId, userDetails3.getId(), "[APPSOLICITOR]")
            ))
            .build();
        when(dataStoreClient.getUserRoles(AUTH_TOKEN, TEST_SERVICE_TOKEN, caseId, null))
            .thenReturn(rolesResource);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);

        FinremCallbackRequest request = FinremCallbackRequestFactory.fromId(Long.parseLong(caseId));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(request, AUTH_TOKEN);

        assertThat(response.getErrors()).isEmpty();
        assertThat(response.getWarnings()).isEmpty();

        DynamicList userCaseAccessList = response.getData().getUserCaseAccessList();
        assertThat(userCaseAccessList.getListItems()).hasSize(3);
        verifyDynamicListElement(userCaseAccessList.getListItems().get(0), userDetails1, "[APPSOLICITOR]");
        verifyDynamicListElement(userCaseAccessList.getListItems().get(1), userDetails2, "[RESPSOLICITOR]");
        verifyDynamicListElement(userCaseAccessList.getListItems().get(2), userDetails3, "[APPSOLICITOR]");

        assertThat(userCaseAccessList.getValue()).isNull();
    }

    private CaseAssignedUserRole createCaseAssignedUserRole(String caseId, String userId, String role) {
        return CaseAssignedUserRole.builder()
            .caseDataId(caseId)
            .caseRole(role)
            .userId(userId)
            .build();
    }

    private UserDetails createUserDetails(String userId, String email, String forename, String surname) {
        return UserDetails.builder()
            .id(userId)
            .email(email)
            .forename(forename)
            .surname(surname)
            .build();
    }

    private void verifyDynamicListElement(DynamicListElement element, UserDetails userDetails, String expectedRole) {
        assertThat(element.getCode()).isEqualTo(String.format("%s%s%s", userDetails.getId(),
            CODE_VALUES_SEPARATOR, expectedRole));
        assertThat(element.getLabel()).isEqualTo(String.format("%s (%s) - %s", userDetails.getFullName(),
            userDetails.getEmail(), expectedRole));
    }
}
