package uk.gov.hmcts.reform.finrem.caseorchestration.handler.creategeneralorder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralOrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import java.time.Clock;
import java.time.ZoneId;

import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.GENERAL_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.GENERAL_ORDER_CONSENT_IN_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@Service
@Slf4j
public class ContestedCreateGeneralOrderAboutToStartHandler extends CreateGeneralOrderAboutToStartHandler {

    @Autowired
    public ContestedCreateGeneralOrderAboutToStartHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                          IdamService idamService) {
        this(finremCaseDetailsMapper, idamService, Clock.system(ZoneId.of("Europe/London")));
    }

    public ContestedCreateGeneralOrderAboutToStartHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                          IdamService idamService, Clock clock) {
        super(finremCaseDetailsMapper, idamService, clock);
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return ABOUT_TO_START.equals(callbackType) && CONTESTED.equals(caseType)
            && (GENERAL_ORDER.equals(eventType) || GENERAL_ORDER_CONSENT_IN_CONTESTED.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(
        FinremCallbackRequest callbackRequestWithFinremCaseDetails, String userAuthorisation) {

        log.info("Contested Create General Order about to start callback for case id: {}",
            callbackRequestWithFinremCaseDetails.getCaseDetails().getId());

        FinremCaseData caseData = callbackRequestWithFinremCaseDetails.getCaseDetails().getData();
        GeneralOrderWrapper generalOrderWrapper = caseData.getGeneralOrderWrapper();
        initGeneralOrder(generalOrderWrapper, userAuthorisation);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).build();
    }
}
