package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PaymentDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PaymentDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import java.util.List;

@Slf4j
@Service
public class ManualPaymentAboutToSubmitHandler extends FinremCallbackHandler {

    private final GenericDocumentService service;

    public ManualPaymentAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                         GenericDocumentService service) {
        super(finremCaseDetailsMapper);
        this.service = service;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && (EventType.MANUAL_PAYMENT.equals(eventType));
    }


    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info("Invoking contested event {} about to start callback for case id: {}",
            EventType.MANUAL_PAYMENT, callbackRequest.getCaseDetails().getId());
        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();

        List<PaymentDocumentCollection> paymentDocuments = caseData.getCopyOfPaperFormA();
        List<PaymentDocumentCollection> paymentList
            = paymentDocuments.stream().map(payment -> covertToPdf(payment.getValue(), userAuthorisation)).toList();
        caseData.setCopyOfPaperFormA(paymentList);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData).build();
    }

    private PaymentDocumentCollection covertToPdf(PaymentDocument paymentDocument, String userAuthorisation) {

        return PaymentDocumentCollection.builder()
            .value(PaymentDocument.builder().typeOfDocument(paymentDocument.getTypeOfDocument())
                .uploadedDocument(service.convertDocumentIfNotPdfAlready(paymentDocument
                    .getUploadedDocument(),userAuthorisation)).build())
            .build();
    }
}