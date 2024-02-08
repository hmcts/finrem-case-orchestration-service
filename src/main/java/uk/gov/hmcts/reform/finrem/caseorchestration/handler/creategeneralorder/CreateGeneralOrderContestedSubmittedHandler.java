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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.generalorder.FinremGeneralOrderRaisedCorresponder;

import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.GENERAL_ORDER;

@Service
@Slf4j
public class CreateGeneralOrderContestedSubmittedHandler extends FinremCallbackHandler {

    private final FinremGeneralOrderRaisedCorresponder generalOrderRaisedCorresponder;

    public CreateGeneralOrderContestedSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                       FinremGeneralOrderRaisedCorresponder generalOrderRaisedCorresponder) {
        super(finremCaseDetailsMapper);
        this.generalOrderRaisedCorresponder = generalOrderRaisedCorresponder;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return SUBMITTED.equals(callbackType) && CaseType.CONTESTED.equals(caseType) && GENERAL_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(
        FinremCallbackRequest callbackRequestWithFinremCaseDetails, String userAuthorisation) {
        log.info("Contested Create General Order submitted callback for case id: {}",
            callbackRequestWithFinremCaseDetails.getCaseDetails().getId());

        FinremCaseDetails caseDetails = callbackRequestWithFinremCaseDetails.getCaseDetails();
        generalOrderRaisedCorresponder.sendCorrespondence(caseDetails);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseDetails.getData())
            .build();
    }
}
