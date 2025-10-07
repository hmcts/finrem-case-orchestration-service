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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseRoleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.BarristerChangeNotifier;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.ManageBarristerService;

@Slf4j
@Service
public class ManageBarristerSubmittedHandler extends FinremCallbackHandler {

    private final CaseRoleService caseRoleService;
    private final ManageBarristerService manageBarristerService;
    private final BarristerChangeNotifier barristerChangeNotifier;

    public ManageBarristerSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                           CaseRoleService caseRoleService,
                                           ManageBarristerService manageBarristerService,
                                           BarristerChangeNotifier barristerChangeNotifier) {
        super(finremCaseDetailsMapper);
        this.caseRoleService = caseRoleService;
        this.manageBarristerService = manageBarristerService;
        this.barristerChangeNotifier = barristerChangeNotifier;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.MANAGE_BARRISTER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.submitted(callbackRequest));

        CaseRole userCaseRole = caseRoleService.getUserOrCaseworkerCaseRole(
            callbackRequest.getCaseDetails().getCaseIdAsString(), userAuthorisation);
        BarristerChange barristerChange = manageBarristerService.getBarristerChange(callbackRequest.getCaseDetails(),
            callbackRequest.getCaseDetailsBefore().getData(), userCaseRole);
        barristerChangeNotifier.notify(new BarristerChangeNotifier.NotifierRequest(callbackRequest.getCaseDetails(),
            userAuthorisation, barristerChange));

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(callbackRequest.getCaseDetails().getData())
            .build();
    }
}
