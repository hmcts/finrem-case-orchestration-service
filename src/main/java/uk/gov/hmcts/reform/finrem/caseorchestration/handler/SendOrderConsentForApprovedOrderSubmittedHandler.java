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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder.FinremConsentOrderAvailableCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

@Slf4j
@Service
public class SendOrderConsentForApprovedOrderSubmittedHandler extends FinremSubmittedCallbackHandler {

    private final ConsentOrderPrintService consentOrderPrintService;

    private final FinremConsentOrderAvailableCorresponder consentOrderAvailableCorresponder;

    public SendOrderConsentForApprovedOrderSubmittedHandler(FinremCaseDetailsMapper mapper,
                                                            EvidenceManagementDeleteService evidenceManagementDeleteService,
                                                            RetryExecutor retryExecutor,
                                                            ConsentOrderPrintService consentOrderPrintService,
                                                            FinremConsentOrderAvailableCorresponder consentOrderAvailableCorresponder) {

        super(mapper, evidenceManagementDeleteService, retryExecutor);
        this.consentOrderPrintService = consentOrderPrintService;
        this.consentOrderAvailableCorresponder = consentOrderAvailableCorresponder;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && EventType.CONSENT_SEND_ORDER_FOR_APPROVED_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest, String userAuthorisation) {

        log.info(CallbackHandlerLogger.submitted(callbackRequest));
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();

        consentOrderPrintService.sendConsentOrderToBulkPrintWithStoredCoverSheets(caseDetails, caseDetailsBefore,
            EventType.CONSENT_SEND_ORDER_FOR_APPROVED_ORDER, userAuthorisation);

        consentOrderAvailableCorresponder.sendCorrespondence(caseDetails);

        return submittedResponse();
    }
}
