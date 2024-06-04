package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnStartDefaultValueService;

@Slf4j
@Service
public class AmendApplicationContestedAboutToStartHandler extends FinremCallbackHandler  {

    private final OnStartDefaultValueService onStartDefaultValueService;

    public AmendApplicationContestedAboutToStartHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                        OnStartDefaultValueService onStartDefaultValueService) {
        super(finremCaseDetailsMapper);
        this.onStartDefaultValueService = onStartDefaultValueService;
    }


    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && (EventType.AMEND_CONTESTED_APP_DETAILS.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info("Amend Application Details contested for Case ID : {}", callbackRequest.getCaseDetails().getId());

        onStartDefaultValueService.defaultCivilPartnershipField(callbackRequest);
        onStartDefaultValueService.defaultTypeOfApplication(callbackRequest);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(callbackRequest.getCaseDetails().getData())
            .build();
    }
}
