package uk.gov.hmcts.reform.finrem.caseorchestration.handler.casesubmission;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.fee.FeeCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.payment.PaymentResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.PaymentDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeeService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.adapters.PBAPaymentServiceAdapter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ConsentedStatus.AWAITING_HWF_DECISION;

@Service
@Slf4j
public class CaseSubmissionAboutToSubmitHandler extends FinremCallbackHandler {

    private final FeeService feeService;
    private final PBAPaymentServiceAdapter pbaPaymentService;

    public CaseSubmissionAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                              FeeService feeService, PBAPaymentServiceAdapter pbaPaymentService) {
        super(finremCaseDetailsMapper);
        this.feeService = feeService;
        this.pbaPaymentService = pbaPaymentService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && EventType.APPLICATION_PAYMENT_SUBMISSION.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToSubmit(callbackRequest));
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();

        addPaymentDetails(caseDetails);

        boolean isPbaPaymentRequired = true;
        FinremCaseData caseData = caseDetails.getData();
        if (isHelpWithFeesCase(caseData)) {
            caseData.setState(AWAITING_HWF_DECISION.getId());
            isPbaPaymentRequired = false;
        } else if (isPaymentReferenceExists(caseData)) {
            log.info("Case ID: {} - Payment reference already exists", caseDetails.getId());
            isPbaPaymentRequired = false;
        }

        List<String> errors = new ArrayList<>();
        if (isPbaPaymentRequired) {
            PaymentResponse paymentResponse = pbaPaymentService.makePayment(userAuthorisation, caseDetails);
            if (paymentResponse.isPaymentSuccess()) {
                log.info("Case ID: {} - Payment successful", caseDetails.getId());
                caseData.getPaymentDetailsWrapper().setPbaPaymentReference(paymentResponse.getReference());
            } else if (paymentResponse.isDuplicatePayment()) {
                log.info("Case ID: {} - Duplicate payment", caseDetails.getId());
                errors.add(paymentResponse.getMessage());
            } else {
                log.info("Case ID: {} - Payment failed", caseDetails.getId());
                errors.add(paymentResponse.getMessage());
            }
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData)
            .errors(errors)
            .build();
    }

    private void addPaymentDetails(FinremCaseDetails caseDetails) {
        FeeCaseData feeCaseData = feeService.getApplicationFeeCaseData(caseDetails);
        PaymentDetailsWrapper paymentDetailsWrapper = caseDetails.getData().getPaymentDetailsWrapper();
        paymentDetailsWrapper.setAmountToPay(new BigDecimal(feeCaseData.getAmountToPay()));
        paymentDetailsWrapper.setOrderSummary(feeCaseData.getOrderSummary());
    }

    private boolean isHelpWithFeesCase(FinremCaseData caseData) {
        return YesOrNo.YES.equals(caseData.getPaymentDetailsWrapper().getHelpWithFeesQuestion());
    }

    private boolean isPaymentReferenceExists(FinremCaseData caseData) {
        return StringUtils.isNotEmpty(caseData.getPaymentDetailsWrapper().getPbaPaymentReference());
    }
}
