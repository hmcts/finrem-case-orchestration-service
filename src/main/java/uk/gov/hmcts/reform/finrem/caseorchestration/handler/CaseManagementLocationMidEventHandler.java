package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseManagementLocationService;

import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.AMEND_CONTESTED_APP_DETAILS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.GIVE_ALLOCATION_DIRECTIONS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.LIST_FOR_HEARING;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.PAPER_CASE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.SOLICITOR_CREATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.UPDATE_FRC_INFORMATION;

@Service
@Slf4j
public class CaseManagementLocationMidEventHandler extends FinremCallbackHandler {

    private final CaseManagementLocationService caseManagementLocationService;

    public CaseManagementLocationMidEventHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                 CaseManagementLocationService caseManagementLocationService) {
        super(finremCaseDetailsMapper);
        this.caseManagementLocationService = caseManagementLocationService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && isValidEventType(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info("About to start handling case creation mid-event, setting caseManagement location for {}",
            callbackRequest.getCaseDetails().getId());

        return caseManagementLocationService.setCaseManagementLocation(callbackRequest);
    }

    private boolean isValidEventType(EventType eventType) {
        return getValidEventTypes().contains(eventType);
    }

    private List<EventType> getValidEventTypes() {
        return List.of(SOLICITOR_CREATE, PAPER_CASE, AMEND_CONTESTED_APP_DETAILS,
            UPDATE_FRC_INFORMATION, GIVE_ALLOCATION_DIRECTIONS, LIST_FOR_HEARING);
    }
}
