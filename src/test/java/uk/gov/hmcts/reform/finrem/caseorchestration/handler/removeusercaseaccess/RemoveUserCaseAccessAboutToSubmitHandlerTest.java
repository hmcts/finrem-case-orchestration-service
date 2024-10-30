package uk.gov.hmcts.reform.finrem.caseorchestration.handler.removeusercaseaccess;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdDataStoreService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SYSTEM_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class RemoveUserCaseAccessAboutToSubmitHandlerTest {

    @InjectMocks
    private RemoveUserCaseAccessAboutToSubmitHandler handler;
    @Mock
    private CcdDataStoreService ccdDataStoreService;
    @Mock
    private SystemUserService systemUserService;

    @Test
    void testHandle() {
        assertCanHandle(handler,
            Arguments.of(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.REMOVE_USER_CASE_ACCESS),
            Arguments.of(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.REMOVE_USER_CASE_ACCESS)
        );
    }

    @Test
    void givenNoUserSelectedWhenHandleThenNoUserRemoved() {
        Long caseId = 12345L;
        FinremCallbackRequest request = FinremCallbackRequestFactory.fromId(caseId);
        request.getCaseDetails().getData().setUserCaseAccessList(createUserCaseAccessList(false));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(request, AUTH_TOKEN);
        assertThat(response.getData()).isNotNull();
        assertThat(response.getErrors()).isEmpty();
        assertThat(response.getWarnings()).isEmpty();

        verifyNoInteractions(ccdDataStoreService);
    }

    @Test
    void givenNUserSelectedWhenHandleThenUserRemoved() {
        Long caseId = 12345L;
        FinremCallbackRequest request = FinremCallbackRequestFactory.fromId(caseId);
        request.getCaseDetails().getData().setUserCaseAccessList(createUserCaseAccessList(true));

        when(systemUserService.getSysUserToken()).thenReturn(TEST_SYSTEM_TOKEN);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(request, AUTH_TOKEN);
        assertThat(response.getData()).isNotNull();
        assertThat(response.getErrors()).isEmpty();
        assertThat(response.getWarnings()).isEmpty();

        verify(ccdDataStoreService, times(1)).removeUserCaseRole(String.valueOf(caseId), TEST_SYSTEM_TOKEN, "user1",
            "[APPSOLICITOR]");
        verifyNoMoreInteractions(ccdDataStoreService);

        verify(systemUserService, times(1)).getSysUserToken();
        verifyNoMoreInteractions(systemUserService);
    }

    private DynamicList createUserCaseAccessList(boolean valueSelected) {
        List<DynamicListElement> listItems = List.of(
            new DynamicListElement("user1~#~[APPSOLICITOR]", "A User (a@test.com) [APPSOLICITOR]"),
            new DynamicListElement("user2~#~[RESPSOLICITOR]", "B User (a@test.com) [RESPSOLICITOR]")

        );
        return DynamicList.builder()
            .listItems(listItems)
            .value(valueSelected ? listItems.get(0) : null)
            .build();
    }
}
