package uk.gov.hmcts.reform.finrem.caseorchestration.handler.rejectorder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder.ConsentOrderNotApprovedCorresponder;

@Slf4j
@Service
public class RejectedConsentOrderSubmittedHandler extends FinremCallbackHandler {

    private final ConsentOrderNotApprovedCorresponder consentOrderNotApprovedCorresponder;

    public RejectedConsentOrderSubmittedHandler(FinremCaseDetailsMapper mapper,
                                                ConsentOrderNotApprovedCorresponder consentOrderNotApprovedCorresponder) {
        super(mapper);
        this.consentOrderNotApprovedCorresponder = consentOrderNotApprovedCorresponder;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && EventType.REJECT_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.submitted(callbackRequest));

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Sending consent order not approved email correspondence to applicant for Case ID: {}", caseDetails.getId());
        consentOrderNotApprovedCorresponder.sendCorrespondence(caseDetails);

        return submittedResponse();
    }
}
