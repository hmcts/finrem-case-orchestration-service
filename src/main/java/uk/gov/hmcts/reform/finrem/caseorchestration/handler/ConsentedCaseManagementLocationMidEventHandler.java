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

@Service
@Slf4j
public class ConsentedCaseManagementLocationMidEventHandler extends FinremCallbackHandler {

    private final CaseManagementLocationService caseManagementLocationService;

    public ConsentedCaseManagementLocationMidEventHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                          CaseManagementLocationService caseManagementLocationService) {
        super(finremCaseDetailsMapper);
        this.caseManagementLocationService = caseManagementLocationService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && EventType.CONSENTED_UPDATE_COURT_INFO.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info("About to start handling case creation mid-event, setting caseManagement location for ");

        return caseManagementLocationService.setCaseManagementLocation(callbackRequest);
    }
}
