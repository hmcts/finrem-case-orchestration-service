package uk.gov.hmcts.reform.finrem.caseorchestration.handler.casesubmission;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PBAValidationService;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class CaseSubmissionPbaValidateMidEventHandler extends FinremCallbackHandler {

    private final PBAValidationService pbaValidationService;

    public CaseSubmissionPbaValidateMidEventHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                    PBAValidationService pbaValidationService) {
        super(finremCaseDetailsMapper);
        this.pbaValidationService = pbaValidationService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType)
            && EventType.APPLICATION_PAYMENT_SUBMISSION.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(
        FinremCallbackRequest callbackRequestWithFinremCaseDetails, String userAuthorisation) {

        FinremCaseDetails caseDetails = callbackRequestWithFinremCaseDetails.getCaseDetails();

        log.info("Received request to validate PBA number for Case ID: {}",
            caseDetails.getId());

        validateCaseData(callbackRequestWithFinremCaseDetails);

        FinremCaseData caseData = caseDetails.getData();
        if (YesOrNo.NO.equals(caseData.getHelpWithFeesQuestion())) {
            String pbaNumber = Objects.toString(caseData.getPbaNumber());
            log.info("Validating PBA Number: {} for Case ID: {}", pbaNumber, caseDetails.getId());
            if (!pbaValidationService.isValidPBA(userAuthorisation, pbaNumber)) {
                log.info("PBA number is invalid for Case ID: {}", caseDetails.getId());
                return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
                    .errors(List.of("PBA Account Number is not valid, please enter a valid one."))
                    .build();
            }
        }
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseDetails.getData()).build();
    }

}