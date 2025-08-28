package uk.gov.hmcts.reform.finrem.caseorchestration.handler.uploadapprovedorder.contested;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadAdditionalDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintDocumentService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class UploadApprovedOrderContestedMidHandler extends FinremCallbackHandler {

    private final BulkPrintDocumentService service;

    public UploadApprovedOrderContestedMidHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                          BulkPrintDocumentService service) {
        super(finremCaseDetailsMapper);
        this.service = service;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && (EventType.UPLOAD_APPROVED_ORDER.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        String caseId = String.valueOf(caseDetails.getId());
        log.info("Invoking contested event {} mid callback for Case ID: {}",
            EventType.UPLOAD_APPROVED_ORDER, caseId);

        FinremCaseData caseData = caseDetails.getData();
        List<String> errors = new ArrayList<>();

        FinremCaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
        FinremCaseData beforeData = caseDetailsBefore.getData();

        List<DirectionOrderCollection> uploadHearingOrders = caseData.getUploadHearingOrder();
        if (uploadHearingOrders != null) {
            log.info("Latest no. of hearing orders {} for case Id {}", uploadHearingOrders.size(), caseId);
            List<DirectionOrderCollection> uploadHearingOrdersBefore = beforeData.getUploadHearingOrder();
            if (uploadHearingOrdersBefore != null && !uploadHearingOrdersBefore.isEmpty()) {
                log.info("Existing no. of hearing orders {} for case Id {}", uploadHearingOrdersBefore.size(), caseId);
                uploadHearingOrders.removeAll(uploadHearingOrdersBefore);
            }
            log.info("No. of hearing orders {} to check for error for case Id {}", uploadHearingOrders.size(), caseId);
            uploadHearingOrders.forEach(doc ->
                service.validateEncryptionOnUploadedDocument(doc.getValue().getUploadDraftDocument(),
                    caseId, errors, userAuthorisation)
            );
        }

        List<UploadAdditionalDocumentCollection> uploadAdditionalDocument = caseData.getUploadAdditionalDocument();
        if (uploadAdditionalDocument != null) {
            List<UploadAdditionalDocumentCollection> uploadAdditionalBefore = beforeData.getUploadAdditionalDocument();
            if (uploadAdditionalBefore != null && !uploadAdditionalBefore.isEmpty()) {
                uploadAdditionalDocument.removeAll(uploadAdditionalBefore);
            }
            uploadAdditionalDocument.forEach(doc ->
                service.validateEncryptionOnUploadedDocument(doc.getValue().getAdditionalDocuments(),
                    caseId, errors, userAuthorisation)
            );
        }
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData).errors(errors).build();
    }
}
