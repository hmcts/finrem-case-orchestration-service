package uk.gov.hmcts.reform.finrem.caseorchestration.handler.draftorders.upload;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRolesResource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseAssignedRoleService;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class UploadDraftOrderAboutToStartHandlerTest {

    @InjectMocks
    private UploadDraftOrdersAboutToStartHandler handler;

    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;
    @Mock
    private CaseAssignedRoleService caseAssignedRoleService;

    @Test
    void canHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.DRAFT_ORDERS);
    }

    @Test
    void givenUserIsCaseWorkerWhenHandleThenShowUploadQuestionIsYes() {
        long caseID = 1727874196328932L;
        FinremCallbackRequest request = FinremCallbackRequestFactory.fromId(caseID);
        when(caseAssignedRoleService.getCaseAssignedUserRole(String.valueOf(caseID), AUTH_TOKEN)).thenReturn(
            CaseAssignedUserRolesResource.builder()
                .caseAssignedUserRoles(Collections.emptyList())
                .build());

        var response = handler.handle(request, AUTH_TOKEN);

        assertThat(response.getData().getDraftOrdersWrapper().getShowUploadPartyQuestion()).isEqualTo(YesOrNo.YES);
    }

    @Test
    void givenUserIsNotCaseWorkerWhenHandleThenShowUploadQuestionIsNo() {
        long caseID = 1727874196328932L;
        FinremCallbackRequest request = FinremCallbackRequestFactory.fromId(caseID);
        CaseAssignedUserRole caseAssignedUserRole = CaseAssignedUserRole.builder()
            .caseRole(CaseRole.APP_SOLICITOR.getCcdCode())
            .build();
        when(caseAssignedRoleService.getCaseAssignedUserRole(String.valueOf(caseID), AUTH_TOKEN)).thenReturn(
            CaseAssignedUserRolesResource.builder()
                .caseAssignedUserRoles(List.of(caseAssignedUserRole))
                .build());

        var response = handler.handle(request, AUTH_TOKEN);

        assertThat(response.getData().getDraftOrdersWrapper().getShowUploadPartyQuestion()).isEqualTo(YesOrNo.NO);
    }
}
