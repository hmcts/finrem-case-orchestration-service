package uk.gov.hmcts.reform.finrem.caseorchestration.handler.managebarrister;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.BarristerChange;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseRoleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.BarristerChangeCaseAccessUpdater;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.ManageBarristerService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ManageBarristerAboutToSubmitHandler extends FinremCallbackHandler {

    private final CaseRoleService caseRoleService;
    private final ManageBarristerService manageBarristerService;
    private final BarristerChangeCaseAccessUpdater barristerChangeCaseAccessUpdater;

    public ManageBarristerAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                               CaseRoleService caseRoleService,
                                               ManageBarristerService manageBarristerService,
                                               BarristerChangeCaseAccessUpdater barristerChangeCaseAccessUpdater) {
        super(finremCaseDetailsMapper);
        this.caseRoleService = caseRoleService;
        this.manageBarristerService = manageBarristerService;
        this.barristerChangeCaseAccessUpdater = barristerChangeCaseAccessUpdater;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.MANAGE_BARRISTER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToSubmit(callbackRequest));

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();

        CaseRole userCaseRole = caseRoleService.getUserOrCaseworkerCaseRole(caseDetails.getCaseIdAsString(),
            userAuthorisation);
        BarristerParty barristerParty = manageBarristerService.getManageBarristerParty(caseDetails, userCaseRole);
        List<String> errors = new ArrayList<>();
        if (barristerParty == null) {
            errors.add("Select which party's barrister you want to manage");
        } else {
            updateCase(callbackRequest, userAuthorisation, barristerParty, userCaseRole);
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseDetails.getData())
            .errors(errors)
            .build();
    }

    private void updateCase(FinremCallbackRequest callbackRequest, String authToken, BarristerParty barristerParty,
                            CaseRole userCaseRole) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        List<BarristerCollectionItem> eventBarristers = manageBarristerService.getEventBarristers(caseDetails.getData(),
            barristerParty);

        // The UI only captures the email address of added barristers so we need to look up the userId here.
        // Storing the userId in the case data means we can use it in the future to remove barrister access
        // to the case if required
        manageBarristerService.addUserIdToBarristerData(eventBarristers);

        // Add or remove barrister case access to reflect the changes made in the event
        BarristerChange barristerChange = manageBarristerService.getBarristerChange(caseDetails,
            callbackRequest.getCaseDetailsBefore().getData(), userCaseRole);
        barristerChangeCaseAccessUpdater.update(caseDetails, authToken, barristerChange);
    }
}
