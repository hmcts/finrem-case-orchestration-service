package uk.gov.hmcts.reform.finrem.caseorchestration.handler.uploadapprovedorder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.helper.DocumentWarningsHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UploadApprovedOrderService;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;

@Slf4j
@Service
public class UploadApprovedOrderContestedAboutToSubmitHandler extends FinremCallbackHandler {

    private final DocumentWarningsHelper documentWarningsHelper;

    private final UploadApprovedOrderService uploadApprovedOrderService;

    public UploadApprovedOrderContestedAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                            UploadApprovedOrderService uploadApprovedOrderService,
                                                            DocumentWarningsHelper documentWarningsHelper) {
        super(finremCaseDetailsMapper);
        this.uploadApprovedOrderService = uploadApprovedOrderService;
        this.documentWarningsHelper = documentWarningsHelper;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.UPLOAD_APPROVED_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToSubmit(callbackRequest));

        List<String> errors = new ArrayList<>();
        uploadApprovedOrderService.processApprovedOrders(callbackRequest, errors, userAuthorisation);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(callbackRequest.getCaseDetails().getData())
            .warnings(documentWarningsHelper.getDocumentWarnings(callbackRequest, data ->
                emptyIfNull(data.getUploadHearingOrder()).stream()
                    .map(DirectionOrderCollection::getValue).toList(), userAuthorisation))
            .errors(errors).build();
    }
}
