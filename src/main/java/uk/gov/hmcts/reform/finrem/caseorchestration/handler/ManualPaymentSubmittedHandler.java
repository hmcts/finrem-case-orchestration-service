package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ManualPaymentDocumentService;

import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;

@Slf4j
@Service
public class ManualPaymentSubmittedHandler extends FinremCallbackHandler {

    private final ManualPaymentDocumentService service;
    private final BulkPrintService printService;

    public ManualPaymentSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                         ManualPaymentDocumentService service,
                                         BulkPrintService printService) {
        super(finremCaseDetailsMapper);
        this.service = service;
        this.printService = printService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && (EventType.MANUAL_PAYMENT.equals(eventType));
    }


    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Invoking contested event {} about to start callback for case id: {}",
            EventType.MANUAL_PAYMENT, caseDetails.getId());
        FinremCaseData caseData = caseDetails.getData();

        if (caseData.isContestedPaperApplication()) {
            log.info("Sending letter correspondence to applicant for case: {}", caseDetails.getId());
            CaseDocument caseDocument =
                service.generateManualPaymentLetter(caseDetails, userAuthorisation, APPLICANT);
            printService.sendDocumentForPrint(caseDocument, caseDetails);
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData).build();
    }
}