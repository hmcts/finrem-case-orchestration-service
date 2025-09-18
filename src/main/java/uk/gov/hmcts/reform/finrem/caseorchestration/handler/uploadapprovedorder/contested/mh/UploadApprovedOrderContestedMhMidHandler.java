package uk.gov.hmcts.reform.finrem.caseorchestration.handler.uploadapprovedorder.contested.mh;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintDocumentService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class UploadApprovedOrderContestedMhMidHandler extends FinremCallbackHandler {

    private final BulkPrintDocumentService bulkPrintDocumentService;

    public UploadApprovedOrderContestedMhMidHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                    BulkPrintDocumentService bulkPrintDocumentService) {
        super(finremCaseDetailsMapper);
        this.bulkPrintDocumentService = bulkPrintDocumentService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.UPLOAD_APPROVED_ORDER_MH.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.midEvent(callbackRequest));

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        String caseId = String.valueOf(caseDetails.getId());

        FinremCaseData caseData = caseDetails.getData();
        List<String> errors = new ArrayList<>();

        FinremCaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
        FinremCaseData beforeData = caseDetailsBefore.getData();

        List<DirectionOrderCollection> uploadHearingOrders = caseData.getUploadHearingOrder();
        if (CollectionUtils.isEmpty(uploadHearingOrders)) {
            errors.add("No upload approved order found");
        } else {
            log.info("Latest no. of hearing orders {} for case Id {}", uploadHearingOrders.size(), caseId);
            List<DirectionOrderCollection> uploadHearingOrdersBefore = beforeData.getUploadHearingOrder();
            if (uploadHearingOrdersBefore != null && !uploadHearingOrdersBefore.isEmpty()) {
                log.info("Existing no. of hearing orders {} for case Id {}", uploadHearingOrdersBefore.size(), caseId);
                uploadHearingOrders.removeAll(uploadHearingOrdersBefore);
            }
            log.info("No. of hearing orders {} to check for error for case Id {}", uploadHearingOrders.size(), caseId);
            uploadHearingOrders.forEach(doc ->
                bulkPrintDocumentService.validateEncryptionOnUploadedDocument(doc.getValue().getUploadDraftDocument(),
                    caseId, errors, userAuthorisation)
            );
        }
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData).errors(errors).build();
    }
}
