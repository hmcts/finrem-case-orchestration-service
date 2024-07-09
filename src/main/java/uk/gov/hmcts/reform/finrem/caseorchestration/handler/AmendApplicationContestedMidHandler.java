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

import java.util.List;

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
        return List.of(CallbackType.MID_EVENT, CallbackType.MID_EVENT_1, CallbackType.MID_EVENT_2, CallbackType.MID_EVENT_3).contains(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.AMEND_CONTESTED_APP_DETAILS.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        throw new UnsupportedOperationException("MESSAGE");
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              CallbackType callbackType,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Invoking contested event {} mid event callback for Case ID: {}",
            EventType.AMEND_CONTESTED_APP_DETAILS, caseDetails.getId());

        FinremCaseData caseData = caseDetails.getData();
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData).errors(postalService.validate(caseData)).build();
    }
}
