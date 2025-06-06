package uk.gov.hmcts.reform.finrem.caseorchestration.handler.managehearings;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ManageHearingsMidHandler extends FinremCallbackHandler {

    private final  ValidateHearingService validateHearingService;

    public ManageHearingsMidHandler(FinremCaseDetailsMapper finremCaseDetailsMapper, ValidateHearingService validateHearingService) {
        super(finremCaseDetailsMapper);
        this.validateHearingService = validateHearingService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.MANAGE_HEARINGS.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.midEvent(callbackRequest));
        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();

        FinremCaseData finremCaseData = finremCaseDetails.getData();

        ManageHearingsAction actionSelection = finremCaseData.getManageHearingsWrapper().getManageHearingsActionSelection();
        ManageHearingsWrapper manageHearingsWrapper = finremCaseData.getManageHearingsWrapper();

        List<String> warnings = new ArrayList<>();

        if (ManageHearingsAction.ADD_HEARING.equals(actionSelection)) {
            warnings = validateHearingService.validateManageHearingWarnings(finremCaseDetails.getData(),
                manageHearingsWrapper.getWorkingHearing().getHearingType());
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(finremCaseData)
            .warnings(warnings)
            .build();
    }
}
