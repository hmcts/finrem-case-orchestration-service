package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDataConsented;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderService;

@Slf4j
@Service
public class RespondToOrderAboutToSubmitHandler extends FinremCallbackHandler<FinremCaseDataConsented> {
    private final ConsentOrderService consentOrderService;

    public RespondToOrderAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                              ConsentOrderService consentOrderService) {
        super(finremCaseDetailsMapper);
        this.consentOrderService = consentOrderService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && EventType.RESPOND_TO_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseDataConsented> handle(
        FinremCallbackRequest<FinremCaseDataConsented> callbackRequest, String userAuthorisation) {
        FinremCaseDetails<FinremCaseDataConsented> caseDetails = callbackRequest.getCaseDetails();
        log.info("Invoking contested event {} about to start callback for case id: {}",
            EventType.RESPOND_TO_ORDER, caseDetails.getId());
        FinremCaseDataConsented caseData = caseDetails.getData();

        CaseDocument caseDocument = consentOrderService.getLatestConsentOrderData(callbackRequest);
        caseData.setLatestConsentOrder(caseDocument);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseDataConsented>builder()
            .data(caseData).build();
    }

}


