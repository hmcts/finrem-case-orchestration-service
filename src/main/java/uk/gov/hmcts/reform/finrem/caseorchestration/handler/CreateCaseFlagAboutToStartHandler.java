package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseFlagsService;

@Slf4j
@Service
public class CreateCaseFlagAboutToStartHandler extends FinremCallbackHandler {

    private final CaseFlagsService caseFlagsService;

    public CreateCaseFlagAboutToStartHandler(FinremCaseDetailsMapper mapper,
                                             CaseFlagsService caseFlagsService) {
        super(mapper);
        this.caseFlagsService = caseFlagsService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return (CallbackType.ABOUT_TO_START.equals(callbackType) || CallbackType.ABOUT_TO_SUBMIT.equals(callbackType))
            && (CaseType.CONSENTED.equals(caseType) || CaseType.CONTESTED.equals(caseType))
            && (EventType.CREATE_CASE_FLAG.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info("Handling create case flag about to start callback for case ID: {}", callbackRequest.getCaseDetails().getId());
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();
        caseFlagsService.setCaseFlagInformation(caseDetails);
        log.info("Create case flags are added for case ID: {}", caseDetails.getId());
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).build();
    }
}
