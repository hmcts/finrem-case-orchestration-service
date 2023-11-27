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

import java.util.List;

import static java.util.Objects.isNull;


@Slf4j
@Service
public class SendOrderConsentedApproveOrderAboutToSubmitHandler extends FinremCallbackHandler {
    public SendOrderConsentedApproveOrderAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper) {
        super(finremCaseDetailsMapper);
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && EventType.CONSENTED_SEND_ORDER_FOR_APPROVED.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        String caseId = String.valueOf(caseDetails.getId());
        log.info("Invoking consented event {}, callback {} callback for case id: {}",
            EventType.CONSENTED_SEND_ORDER_FOR_APPROVED, CallbackType.ABOUT_TO_SUBMIT, caseId);

        FinremCaseData finremCaseData = caseDetails.getData();
        if (isNull(finremCaseData.getLatestConsentOrder())) {
            log.info("Failed validation for caseId{} as latest Consent Order field was null", caseId);
            return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
                .data(caseDetails.getData()).errors(List.of("Latest Consent Order" +
                    " Field is empty. Please use the Upload Consent Order Event instead of Send Order")).build();
        }

        log.info("Successfully validated {} as latest Consent Order field was present", caseId);
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseDetails.getData()).build();
    }
}
