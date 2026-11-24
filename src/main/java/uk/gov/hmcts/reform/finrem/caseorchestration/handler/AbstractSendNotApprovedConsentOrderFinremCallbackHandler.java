package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.MissingCourtException;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderPrintService;

import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.SEND_ORDER;

public abstract class AbstractSendNotApprovedConsentOrderFinremCallbackHandler extends FinremCallbackHandler {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final ConsentOrderPrintService consentOrderPrintService;

    protected AbstractSendNotApprovedConsentOrderFinremCallbackHandler(
        FinremCaseDetailsMapper mapper, ConsentOrderPrintService consentOrderPrintService) {
        super(mapper);
        this.consentOrderPrintService = consentOrderPrintService;
    }

    protected GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> sendNotApprovedConsentOrderToBulkPrint(
        FinremCallbackRequest callbackRequest, String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToSubmit(callbackRequest));

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();

        try {
            consentOrderPrintService.sendConsentOrderToBulkPrint(caseDetails, caseDetailsBefore, SEND_ORDER,
                userAuthorisation);
            return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
                .data(caseDetails.getData())
                .build();
        } catch (MissingCourtException e) {
            return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
                .errors(List.of("No FR court information is present on the case. "
                    + "Please add this information using Update FR Court Info."))
                .build();
        }
    }
}
