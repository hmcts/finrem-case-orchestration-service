package uk.gov.hmcts.reform.finrem.caseorchestration.handler.managebarrister;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseRoleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.BarristerValidationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.ManageBarristerService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ManageBarristerMidEventHandler extends FinremCallbackHandler {

    private final ManageBarristerService manageBarristerService;
    private final BarristerValidationService barristerValidationService;
    private final SystemUserService systemUserService;
    private final CaseRoleService caseRoleService;

    public ManageBarristerMidEventHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                          ManageBarristerService manageBarristerService,
                                          BarristerValidationService barristerValidationService,
                                          SystemUserService systemUserService, CaseRoleService caseRoleService) {
        super(finremCaseDetailsMapper);
        this.manageBarristerService = manageBarristerService;
        this.barristerValidationService = barristerValidationService;
        this.systemUserService = systemUserService;
        this.caseRoleService = caseRoleService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.MANAGE_BARRISTER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.midEvent(callbackRequest));

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();

        CaseRole userCaseRole = caseRoleService.getUserOrCaseworkerCaseRole(String.valueOf(caseDetails.getId()), userAuthorisation);

        List<String> errors = new ArrayList<>();
        BarristerParty barristerParty = manageBarristerService.getManageBarristerParty(caseDetails, userCaseRole);
        if (barristerParty == null) {
            errors.add("Select which party's barrister you want to manage");
        } else {
            errors.addAll(getValidationErrors(caseDetails, barristerParty, userAuthorisation));
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseDetails.getData())
            .errors(errors)
            .build();
    }

    private List<String> getValidationErrors(FinremCaseDetails caseDetails, BarristerParty barristerParty, String authToken) {
        List<BarristerData> barristers = manageBarristerService.getEventBarristers(caseDetails.getData(), barristerParty)
            .stream()
            .map(item -> BarristerData.builder().barrister(item.getValue()).build())
            .toList();

        String authTokenToUse = getAuthTokenToUse(caseDetails, authToken);

        CaseRole barristerCaseRole = manageBarristerService.getBarristerCaseRole(barristerParty);

        return barristerValidationService.validateBarristerEmails(barristers,
            authTokenToUse, caseDetails.getCaseIdAsString(), barristerCaseRole);
    }

    /**
     * If the barrister party has been set in the case event then we know that the user is a caseworker.
     * In this scenario we need to use the system user token to validate the emails as the caseworker
     * does not have a role required to access the organisation service.
     *
     * @param caseDetails case details
     * @param authToken   user auth token
     * @return the appropriate auth token to use
     */
    private String getAuthTokenToUse(FinremCaseDetails caseDetails, String authToken) {
        if (caseDetails.getData().getBarristerParty() != null) {
            return systemUserService.getSysUserToken();
        } else {
            return authToken;
        }
    }
}
