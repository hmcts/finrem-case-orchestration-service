package uk.gov.hmcts.reform.finrem.caseorchestration.handler.managebarrister;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.BarristerChange;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseRoleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.BarristerChangeCaseAccessUpdater;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.ManageBarristerService;
import uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;

@ExtendWith(MockitoExtension.class)
class ManageBarristerAboutToSubmitHandlerTest {

    @InjectMocks
    private ManageBarristerAboutToSubmitHandler manageBarristerAboutToSubmitHandler;
    @Mock
    private CaseRoleService caseRoleService;
    @Mock
    private ManageBarristerService manageBarristerService;
    @Mock
    private BarristerChangeCaseAccessUpdater barristerChangeCaseAccessUpdater;

    @Test
    void testCanHandle() {
        Assertions.assertCanHandle(manageBarristerAboutToSubmitHandler, CallbackType.ABOUT_TO_SUBMIT,
            CaseType.CONTESTED, EventType.MANAGE_BARRISTER);
    }

    @Test
    void givenValidCaseSubmission_whenHandle_thenUpdatesCaseAccess() {
        FinremCallbackRequest callbackRequest = createCallbackRequest();

        when(caseRoleService.getUserOrCaseworkerCaseRole(CASE_ID, AUTH_TOKEN)).thenReturn(CaseRole.CASEWORKER);
        when(manageBarristerService.getManageBarristerParty(callbackRequest.getCaseDetails(), CaseRole.CASEWORKER))
            .thenReturn(BarristerParty.APPLICANT);
        List<BarristerCollectionItem> eventBarristers = createEventBarristers();
        when(manageBarristerService.getEventBarristers(callbackRequest.getCaseDetails().getData(),
            BarristerParty.APPLICANT)).thenReturn(eventBarristers);

        BarristerChange barristerChange = BarristerChange.builder().build();
        when(manageBarristerService.getBarristerChange(callbackRequest.getCaseDetails(),
            callbackRequest.getCaseDetailsBefore().getData(), CaseRole.CASEWORKER))
            .thenReturn(barristerChange);

        var response = manageBarristerAboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getData()).isNotNull();
        assertThat(response.getErrors()).isEmpty();
        assertThat(response.getWarnings()).isEmpty();
        verify(manageBarristerService).addUserIdToBarristerData(eventBarristers);
        verify(barristerChangeCaseAccessUpdater).update(callbackRequest.getCaseDetails(), AUTH_TOKEN, barristerChange);
    }

    @Test
    void givenNoBarristerPartySet_whenHandle_thenReturnsError() {
        FinremCallbackRequest callbackRequest = createCallbackRequest();

        when(caseRoleService.getUserOrCaseworkerCaseRole(CASE_ID, AUTH_TOKEN)).thenReturn(CaseRole.CASEWORKER);
        when(manageBarristerService.getManageBarristerParty(callbackRequest.getCaseDetails(), CaseRole.CASEWORKER))
            .thenReturn(null);

        var response = manageBarristerAboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getData()).isNotNull();
        assertThat(response.getErrors()).containsExactly("Select which party's barrister you want to manage");
        assertThat(response.getWarnings()).isEmpty();

        verifyNoInteractions(barristerChangeCaseAccessUpdater);
    }

    private FinremCallbackRequest createCallbackRequest() {
        return FinremCallbackRequest.builder()
            .caseDetails(FinremCaseDetails.builder()
                .id(Long.valueOf(CASE_ID))
                .data(FinremCaseData.builder().build())
                .build())
            .caseDetailsBefore(FinremCaseDetails.builder()
                .data(FinremCaseData.builder().build())
                .build())
            .build();
    }

    private List<BarristerCollectionItem> createEventBarristers() {
        return List.of(BarristerCollectionItem.builder()
            .id("1")
            .value(Barrister.builder().build())
            .build());
    }
}
