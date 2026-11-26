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

public abstract class AbstractSendNotApprovedConsentOrderFinremCallbackHandler extends FinremAboutToSubmitCallbackHandler {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final ConsentOrderPrintService consentOrderPrintService;
    private static final String MISSING_COURT_SELECTION_ERROR = "No FR court information is present on the case. "
        + "Please add this information using Update FR Court Info.";

    protected AbstractSendNotApprovedConsentOrderFinremCallbackHandler(
        FinremCaseDetailsMapper mapper, ConsentOrderPrintService consentOrderPrintService) {
        super(mapper);
        this.consentOrderPrintService = consentOrderPrintService;
    }

    /**
     * Sends the unapproved consent order to bulk print using the Send Order document selection rules.
     * SEND_ORDER is hardcoded. In this context, the event enum is used to conditionally choose documents to send.
     * The concrete classes that use this abstract class want documents chosen as if the SEND_ORDER event is being run.
     *
     * @param callbackRequest the callback containing the current and previous case details
     * @param userAuthorisation the authorisation token used for bulk printing
     * @return a callback response containing the current case data and, if court information is missing, an error.
     */
    protected GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> sendNotApprovedConsentOrderToBulkPrint(
        FinremCallbackRequest callbackRequest, String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToSubmit(callbackRequest));

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();

        try {
            consentOrderPrintService.sendConsentOrderToBulkPrint(caseDetails, caseDetailsBefore, SEND_ORDER,
                userAuthorisation);
            return response(caseDetails.getData());
        } catch (MissingCourtException e) {
            return responseWithoutWarnings(caseDetails.getData(), List.of(MISSING_COURT_SELECTION_ERROR));
        }
    }
}
