package uk.gov.hmcts.reform.finrem.caseorchestration.handler.removeusercaseaccess;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DataStoreClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.IdamAuthApi;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRolesResource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.List;

@Service
@Slf4j
public class RemoveUserCaseAccessAboutToStartHandler extends FinremCallbackHandler {

    // Separator element for DynamicListElement code
    static final String CODE_VALUES_SEPARATOR = "~#~";

    private final AuthTokenGenerator authTokenGenerator;
    private final DataStoreClient dataStoreClient;
    private final IdamAuthApi idamAuthApi;
    private final SystemUserService systemUserService;

    public RemoveUserCaseAccessAboutToStartHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                   AuthTokenGenerator authTokenGenerator,
                                                   DataStoreClient dataStoreClient, IdamAuthApi idamAuthApi,
                                                   SystemUserService systemUserService) {
        super(finremCaseDetailsMapper);
        this.authTokenGenerator = authTokenGenerator;
        this.dataStoreClient = dataStoreClient;
        this.idamAuthApi = idamAuthApi;
        this.systemUserService = systemUserService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && EventType.REMOVE_USER_CASE_ACCESS.equals(eventType)
            && (CaseType.CONSENTED.equals(caseType) || CaseType.CONTESTED.equals(caseType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(
        FinremCallbackRequest finremCallbackRequest, String authToken) {

        String caseId = String.valueOf(finremCallbackRequest.getCaseDetails().getId());
        log.info("Remove User Case Access about to start callback for Case ID: {}", caseId);

        String sysAuthToken = systemUserService.getSysUserToken();

        DynamicList caseUserAccessList = getUserCaseAccessList(sysAuthToken, caseId);
        FinremCaseData caseData = finremCallbackRequest.getCaseDetails().getData();
        caseData.setUserCaseAccessList(caseUserAccessList);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData)
            .build();
    }

    private DynamicList getUserCaseAccessList(String authToken, String caseId) {
        List<CaseAssignedUserRole> caseAssignedUserRoles = getCaseAssignedUserRoles(authToken, caseId);
        List<DynamicListElement> dynamicListElements = caseAssignedUserRoles.stream()
            .map(c -> getUserCaseAccess(authToken, c))
            .map(this::mapToDynamicListElement)
            .toList();
        return DynamicList.builder()
            .listItems(dynamicListElements)
            .build();
    }

    private List<CaseAssignedUserRole> getCaseAssignedUserRoles(String authToken, String caseId) {
        String serviceToken = authTokenGenerator.generate();
        CaseAssignedUserRolesResource caseAssignedUserRolesResource = dataStoreClient.getUserRoles(authToken,
            serviceToken, caseId, null);

        return caseAssignedUserRolesResource.getCaseAssignedUserRoles();
    }

    private UserCaseAccess getUserCaseAccess(String authToken, CaseAssignedUserRole caseAssignedUserRole) {
        UserDetails userDetails = idamAuthApi.getUserByUserId(authToken, caseAssignedUserRole.getUserId());
        return UserCaseAccess.of(caseAssignedUserRole, userDetails);
    }

    private DynamicListElement mapToDynamicListElement(UserCaseAccess userCaseAccess) {
        return DynamicListElement.builder()
            .code(String.format("%s%s%s", userCaseAccess.getUserId(), CODE_VALUES_SEPARATOR,
                userCaseAccess.getCaseRole()))
            .label(String.format("%s (%s) - %s", userCaseAccess.getUserFullName(), userCaseAccess.getUserEmail(),
                userCaseAccess.getCaseRole()))
            .build();
    }
}
