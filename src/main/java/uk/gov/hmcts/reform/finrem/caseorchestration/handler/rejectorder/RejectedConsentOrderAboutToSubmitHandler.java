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

    // PT todo.  Javadoc.
    // Refer to tech debt ticket to 1) move to submitted handler with audit pattern, having 2) validated that a court exists for the coversheet.
    // Bulk Print Service will prevent duplicate documents being posted on CCD retries.
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

    // pt todo javadoc expaineding that data comes from callback request set in start
    private boolean refusedAsHearingNeeded(FinremCaseData caseData) {
        return caseData.getOrderRefusalOnScreen()
            .getOrderRefusal()
            .contains(OrderRefusalOption.HEARING_FIXED_FOR_FIRST_AVAILABLE_DATE);
    }

    // pt todo javadoc expaineding that data comes from callback request set in start
    // specify free format text not checkbox.
    private boolean refusedForOtherReasons(FinremCaseData caseData) {
        return Optional.ofNullable(caseData.getOrderRefusalOnScreen().getOrderRefusalOther())
            .filter(otherReasonFreeFormatText -> !otherReasonFreeFormatText.isBlank())
            .isPresent();
    }
}
