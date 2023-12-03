package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintDocumentService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class DirectionUploadOrderMidHandler extends FinremCallbackHandler {

    private final BulkPrintDocumentService service;

    public DirectionUploadOrderMidHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                          BulkPrintDocumentService service) {
        super(finremCaseDetailsMapper);
        this.service = service;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && (EventType.DIRECTION_UPLOAD_ORDER.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        String caseId = String.valueOf(caseDetails.getId());
        log.info("Invoking contested event {} mid callback for case id: {}",
            EventType.DIRECTION_UPLOAD_ORDER, caseId);
        FinremCaseData caseData = caseDetails.getData();

        List<String> errors = new ArrayList<>();

        FinremCaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
        FinremCaseData caseDataBefore = caseDetailsBefore.getData();

        List<DirectionOrderCollection> uploadHearingOrders = caseData.getUploadHearingOrder();
        if (uploadHearingOrders != null) {
            List<DirectionOrderCollection> uploadHearingOrdersBefore = caseDataBefore.getUploadHearingOrder();
            if (uploadHearingOrdersBefore != null && !uploadHearingOrdersBefore.isEmpty()) {
                uploadHearingOrders.removeAll(uploadHearingOrdersBefore);
            }
            uploadHearingOrders.forEach(doc ->
                service.validateEncryptionOnUploadedDocument(doc.getValue().getUploadDraftDocument(),
                    caseId, errors, userAuthorisation)
            );
        }
        List<DocumentCollection> hearingOrderOtherDocuments = caseData.getHearingOrderOtherDocuments();
        if (hearingOrderOtherDocuments != null) {
            List<DocumentCollection> hearingOrderOtherDocumentsBefore = caseDataBefore.getHearingOrderOtherDocuments();
            if (hearingOrderOtherDocumentsBefore != null && !hearingOrderOtherDocumentsBefore.isEmpty()) {
                hearingOrderOtherDocuments.removeAll(hearingOrderOtherDocumentsBefore);
            }
            hearingOrderOtherDocuments.forEach(doc ->
                service.validateEncryptionOnUploadedDocument(doc.getValue(),
                    caseId, errors, userAuthorisation)
            );
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData).errors(errors).build();
    }
}