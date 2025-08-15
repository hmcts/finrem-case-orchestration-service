package uk.gov.hmcts.reform.finrem.caseorchestration.handler.uploadapprovedorder.contested.mh;

import lombok.extern.slf4j.Slf4j;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UploadApprovedOrderService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class UploadApprovedOrderContestedMhAboutToSubmitHandler extends FinremCallbackHandler {

    private final UploadApprovedOrderService service;

    public UploadApprovedOrderContestedMhAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                              UploadApprovedOrderService service) {
        super(finremCaseDetailsMapper);
        this.service = service;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.UPLOAD_APPROVED_ORDER_MH.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        CallbackHandlerLogger.aboutToSubmit(callbackRequest);

        List<String> errors = new ArrayList<>();
        service.processApprovedOrders(callbackRequest, errors, userAuthorisation);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(callbackRequest.getCaseDetails().getData()).errors(errors).build();
    }
}
