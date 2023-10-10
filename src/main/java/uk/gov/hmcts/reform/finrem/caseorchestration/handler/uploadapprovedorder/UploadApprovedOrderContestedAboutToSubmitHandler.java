package uk.gov.hmcts.reform.finrem.caseorchestration.handler.uploadapprovedorder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UploadApprovedOrderService;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadApprovedOrderContestedAboutToSubmitHandler
    implements CallbackHandler<Map<String, Object>> {

    private final UploadApprovedOrderService uploadApprovedOrderService;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.UPLOAD_APPROVED_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> handle(
        CallbackRequest callbackRequest, String userAuthorisation) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();

        return uploadApprovedOrderService.handleUploadApprovedOrderAboutToSubmit(caseDetails, caseDetailsBefore, userAuthorisation);
    }
}
