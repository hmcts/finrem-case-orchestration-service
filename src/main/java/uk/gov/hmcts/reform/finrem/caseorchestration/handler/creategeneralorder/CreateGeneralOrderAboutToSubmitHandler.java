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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralOrderService;

import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.GENERAL_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.GENERAL_ORDER_CONSENT_IN_CONTESTED;

@Service
@Slf4j
public class CreateGeneralOrderAboutToSubmitHandler extends FinremCallbackHandler {

    private final GeneralOrderService generalOrderService;

    public CreateGeneralOrderAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                  GeneralOrderService generalOrderService) {
        super(finremCaseDetailsMapper);
        this.generalOrderService = generalOrderService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        if (!ABOUT_TO_SUBMIT.equals(callbackType)) {
            return false;
        }

        switch (caseType) {
            case CONTESTED -> {
                return GENERAL_ORDER.equals(eventType) || GENERAL_ORDER_CONSENT_IN_CONTESTED.equals(eventType);
            }
            case CONSENTED -> {
                return GENERAL_ORDER.equals(eventType);
            }
            default -> {
                return false;
            }
        }
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(
        FinremCallbackRequest callbackRequestWithFinremCaseDetails, String userAuthorisation) {

        log.info("Create General Order about to submit callback for case id: {}",
            callbackRequestWithFinremCaseDetails.getCaseDetails().getId());

        FinremCaseDetails caseDetails = callbackRequestWithFinremCaseDetails.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();
        if (isContestedCase(caseDetails)) {
            EventType eventId = callbackRequestWithFinremCaseDetails.getEventType();
            generalOrderService.addContestedGeneralOrderToCollection(caseData, eventId);
        } else {
            generalOrderService.addConsentedGeneralOrderToCollection(caseData);
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData)
            .build();
    }

    private boolean isContestedCase(FinremCaseDetails caseDetails) {
        return CaseType.CONTESTED.getCcdType().equals(caseDetails.getCaseType().getCcdType());
    }
}
