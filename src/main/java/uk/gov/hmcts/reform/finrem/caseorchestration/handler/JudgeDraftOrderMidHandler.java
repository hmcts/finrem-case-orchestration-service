package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftDirectionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintDocumentService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class JudgeDraftOrderMidHandler extends FinremCallbackHandler {

    private final BulkPrintDocumentService service;

    private static final String NO_ORDERS_IN_COLLECTION
        = "No orders have been uploaded. Please upload an order.";

    public JudgeDraftOrderMidHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                     BulkPrintDocumentService service) {
        super(finremCaseDetailsMapper);
        this.service = service;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.JUDGE_DRAFT_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.midEvent(callbackRequest));
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();
        DraftDirectionWrapper draftDirectionWrapper = caseData.getDraftDirectionWrapper();
        List<DraftDirectionOrderCollection> judgeApprovedOrderCollection = draftDirectionWrapper.getJudgeApprovedOrderCollection();

        List<String> errors = new ArrayList<>();
        if (CollectionUtils.isEmpty(judgeApprovedOrderCollection)) {
            errors.add(NO_ORDERS_IN_COLLECTION);
            return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
                .data(caseData).errors(errors).build();
        }

        // validate the newly uploaded orders only
        // It does not need to verify caseDetailsBefore later as it will not show the previous uploaded orders anymore.
        FinremCaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
        FinremCaseData beforeCaseData = caseDetailsBefore.getData();
        DraftDirectionWrapper draftDirectionWrapperBefore = beforeCaseData.getDraftDirectionWrapper();

        if (draftDirectionWrapperBefore != null) {
            List<DraftDirectionOrderCollection> draftDirectionOrderCollectionBefore
                = draftDirectionWrapperBefore.getDraftDirectionOrderCollection();
            if (draftDirectionOrderCollectionBefore != null && !draftDirectionOrderCollectionBefore.isEmpty()) {
                judgeApprovedOrderCollection.removeAll(draftDirectionOrderCollectionBefore);
            }
        }

        judgeApprovedOrderCollection.forEach(doc ->
            service.validateEncryptionOnUploadedDocument(doc.getValue().getUploadDraftDocument(),
                caseData.getCcdCaseId(), errors, userAuthorisation)
        );

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).errors(errors).build();
    }
}
