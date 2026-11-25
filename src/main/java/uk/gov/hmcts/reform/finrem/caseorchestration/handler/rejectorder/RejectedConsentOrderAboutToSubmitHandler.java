package uk.gov.hmcts.reform.finrem.caseorchestration.handler.rejectorder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.AbstractSendNotApprovedConsentOrderFinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrderRefusalOption;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.RefusalOrderDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.RefusedConsentOrderDocumentCategoriser;

import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State.AWAITING_RESPONSE;

@Slf4j
@Service
public class RejectedConsentOrderAboutToSubmitHandler extends AbstractSendNotApprovedConsentOrderFinremCallbackHandler {

    private final RefusalOrderDocumentService refusalOrderDocumentService;
    private final RefusedConsentOrderDocumentCategoriser categoriser;

    @Autowired
    public RejectedConsentOrderAboutToSubmitHandler(FinremCaseDetailsMapper mapper,
                                                    RefusalOrderDocumentService refusalOrderDocumentService,
                                                    RefusedConsentOrderDocumentCategoriser categoriser,
                                                    ConsentOrderPrintService consentOrderPrintService) {
        super(mapper, consentOrderPrintService);
        this.refusalOrderDocumentService = refusalOrderDocumentService;
        this.categoriser = categoriser;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && EventType.REJECT_ORDER.equals(eventType);
    }

    /**
     * Processes and categorises the consent order refusal. If no hearing is required and no
     * free-text reason is provided, sets the case data to Awaiting Response and sends the order
     * to bulk print.
     * Bulk Print should be idempotent and prevent duplicate documents being posted on CCD retries, provided
     * that case version is not increased between retry attempts.
     *
     * @param callbackRequest the callback containing the case details and on-screen refusal reasons
     * @param userAuthorisation the authorisation token used for document processing and bulk printing
     * @return the updated case data or a callback error if court information is missing for bulk printing
     */
    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToSubmit(callbackRequest));
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();

        boolean useConsentOrderNotApprovedState =
            refusedAsHearingNeeded(callbackRequest.getFinremCaseData())
                || refusedForOtherReasons(callbackRequest.getFinremCaseData());

        FinremCaseData caseData = refusalOrderDocumentService.processConsentOrderNotApproved(caseDetails, userAuthorisation);
        categoriser.categorise(caseData);

        if (useConsentOrderNotApprovedState) {
            return response(caseData);
        } else {
            caseData.setState(AWAITING_RESPONSE.getStateId());
            return sendNotApprovedConsentOrderToBulkPrint(callbackRequest, userAuthorisation);
        }
    }

    /**
     * Checks for a HEARING_FIXED_FOR_FIRST_AVAILABLE_DATE in the on-screen refusal reasons.
     * EXUI validates for requires a refusal reason before submitting the event, hence no null check.
     *
     * @param caseData the callback case data with the refusal holder is initialised in about-to-start
     * @return true if a HEARING_FIXED_FOR_FIRST_AVAILABLE_DATE option was selected
     */
    private boolean refusedAsHearingNeeded(FinremCaseData caseData) {
        return caseData.getOrderRefusalOnScreen()
            .getOrderRefusal()
            .contains(OrderRefusalOption.HEARING_FIXED_FOR_FIRST_AVAILABLE_DATE);
    }

    /**
     * Checks the free-text refusal reason on the callback's on-screen refusal holder, which is
     * initialised in the about-to-start handler. A missing or blank reason returns false.
     *
     * The Other checkbox is intentionally not checked, just the free-text reason.
     *
     * @param caseData the callback case data containing the on-screen refusal holder
     * @return whether the free-text reason contains non-whitespace characters
     */
    private boolean refusedForOtherReasons(FinremCaseData caseData) {
        return Optional.ofNullable(caseData.getOrderRefusalOnScreen().getOrderRefusalOther())
            .filter(otherReasonFreeFormatText -> !otherReasonFreeFormatText.isBlank())
            .isPresent();
    }
}
