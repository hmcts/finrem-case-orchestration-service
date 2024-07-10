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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;

@Slf4j
@Service
public class AmendApplicationContestedMidHandler extends FinremCallbackHandler {

    private final InternationalPostalService postalService;

    public AmendApplicationContestedMidHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                               InternationalPostalService postalService) {
        super(finremCaseDetailsMapper);
        this.postalService = postalService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.AMEND_CONTESTED_APP_DETAILS.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequestWithFinremCaseDetails,
                                                                              String userAuthorisation) {
        throw new UnsupportedOperationException("never reach this line");
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequestWithFinremCaseDetails,
                                                                              String userAuthorisation, CallbackContext context) {
        FinremCaseDetails caseDetails = callbackRequestWithFinremCaseDetails.getCaseDetails();
        log.info("Invoking contested event {} mid event callback (PageId:{}) for Case ID: {}",
            EventType.AMEND_CONTESTED_APP_DETAILS, context.getPageId(), caseDetails.getId());

        FinremCaseData caseData = caseDetails.getData();
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData).errors(postalService.validate(caseData)).build();
    }
}
