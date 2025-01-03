package uk.gov.hmcts.reform.finrem.caseorchestration.handler.processorders;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.DirectionUploadOrderMidHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetail;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetailCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.processorder.ProcessOrderService;

import java.util.List;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.MID_EVENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.PROCESS_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@Slf4j
@Service
public class ProcessOrdersMidHandler extends DirectionUploadOrderMidHandler {

    private final ProcessOrderService processOrderService;

    public ProcessOrdersMidHandler(FinremCaseDetailsMapper finremCaseDetailsMapper, BulkPrintDocumentService service,
                                   ProcessOrderService processOrderService) {
        super(finremCaseDetailsMapper, service);
        this.processOrderService = processOrderService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return MID_EVENT.equals(callbackType) && CONTESTED.equals(caseType) && PROCESS_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest, String userAuthorisation) {
        FinremCaseData caseDataBefore = callbackRequest.getCaseDetailsBefore().getData();
        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();

        processOrderService.populateUnprocessedApprovedDocuments(caseDataBefore);

        if (processOrderService.areAllLegacyApprovedOrdersRemoved(caseDataBefore, caseData)) {
            return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
                .data(caseData).errors(List.of("Upload Approved Order is required.")).build();
        }
        if (!processOrderService.areAllNewOrdersPdfFiles(caseDataBefore, caseData)) {
            return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
                .data(caseData).errors(List.of("You must upload a PDF file for new documents.")).build();
        }
        // Validate the modifying legacy approved orders
        if (!processOrderService.areAllLegacyApprovedOrdersPdf(caseData)) {
            return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
                .data(caseData).errors(List.of("You must upload a PDF file for modifying legacy approved documents.")).build();
        }
        // Validate the modifying unprocessed approved orders are word documents
        if (!processOrderService.areAllModifyingUnprocessedOrdersWordDocuments(caseData)) {
            return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
                .data(caseData).errors(List.of("You must upload a Microsoft Word file for modifying an unprocessed approved documents."))
                .build();
        }

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> res = super.handle(callbackRequest, userAuthorisation);

        // Create an empty entry if it is empty to save a click on add new button
        if (ofNullable(caseData.getDirectionDetailsCollection()).orElse(List.of()).isEmpty()) {
            caseData.setDirectionDetailsCollection(List.of(
                DirectionDetailCollection.builder().value(DirectionDetail.builder().build()).build()
            ));
        }
        return res;
    }
}
