package uk.gov.hmcts.reform.finrem.caseorchestration.handler.creategeneralorder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralOrderService;

import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.GENERAL_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.GENERAL_ORDER_CONSENT_IN_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@Service
@Slf4j
public class CreateGeneralOrderContestedAboutToSubmitHandler extends FinremCallbackHandler {

    private final CaseDataService caseDataService;
    private final GeneralOrderService generalOrderService;

    protected CreateGeneralOrderContestedAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                           CaseDataService caseDataService,
                                                           GeneralOrderService generalOrderService) {
        super(finremCaseDetailsMapper);
        this.caseDataService = caseDataService;
        this.generalOrderService = generalOrderService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return ABOUT_TO_SUBMIT.equals(callbackType) && CONTESTED.equals(caseType)
            && (GENERAL_ORDER.equals(eventType) || GENERAL_ORDER_CONSENT_IN_CONTESTED.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(
        FinremCallbackRequest callbackRequestWithFinremCaseDetails, String userAuthorisation) {

        log.info("Contested Create General Order about to submit callback for case id: {}",
            callbackRequestWithFinremCaseDetails.getCaseDetails().getId());

        FinremCaseDetails caseDetails = callbackRequestWithFinremCaseDetails.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();
        EventType eventId = callbackRequestWithFinremCaseDetails.getEventType();

        if (GENERAL_ORDER_CONSENT_IN_CONTESTED.equals(eventId) && caseDataService.hasConsentOrder(caseData)) {
            generalOrderService.addConsentedInContestedGeneralOrderToCollection(caseData);
        } else {
            generalOrderService.addContestedGeneralOrderToCollection(caseData);
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData)
            .build();
    }
}
