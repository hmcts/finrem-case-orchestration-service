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

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class SendOrderConsentForNotApprovedOrderAboutToSubmitHandler extends FinremCallbackHandler {
    private final ConsentOrderPrintService consentOrderPrintService;

    public SendOrderConsentForNotApprovedOrderAboutToSubmitHandler(FinremCaseDetailsMapper mapper,
                                                                   ConsentOrderPrintService consentOrderPrintService) {
        super(mapper);
        this.consentOrderPrintService = consentOrderPrintService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && EventType.SEND_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToSubmit(callbackRequest));

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();

        try {
            consentOrderPrintService.sendConsentOrderToBulkPrint(caseDetails, caseDetailsBefore, EventType.SEND_ORDER,
                userAuthorisation);
            return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
                .data(caseDetails.getData())
                .build();
        } catch (IllegalStateException e) {
            if ("There must be exactly one court selected in case data, current initialisedCourtField size is 0"
                .equals(e.getMessage())) {
                String userFriendlyMessage = "No FR court information is present on the case. "
                    + "Please add this information using Update FR Court Info.";
                return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
                    .errors(List.of(userFriendlyMessage))
                    .build();
            }
            throw e;
        }
    }
}
