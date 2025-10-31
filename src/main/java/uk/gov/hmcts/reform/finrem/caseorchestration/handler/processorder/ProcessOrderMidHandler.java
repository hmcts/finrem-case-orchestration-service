package uk.gov.hmcts.reform.finrem.caseorchestration.handler.processorder;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.processorder.ProcessOrderService;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;

@Slf4j
@Service
public class ProcessOrderMidHandler extends FinremCallbackHandler {

    private final BulkPrintDocumentService bulkPrintDocumentService;
    private final ProcessOrderService processOrderService;
    private final ValidateHearingService validateHearingService;

    public ProcessOrderMidHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                  BulkPrintDocumentService bulkPrintDocumentService, ProcessOrderService processOrderService,
                                  ValidateHearingService validateHearingService) {
        super(finremCaseDetailsMapper);
        this.bulkPrintDocumentService = bulkPrintDocumentService;
        this.processOrderService = processOrderService;
        this.validateHearingService = validateHearingService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.PROCESS_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info(CallbackHandlerLogger.midEvent(callbackRequest));
        FinremCaseData caseData = caseDetails.getData();

        List<String> errors = new ArrayList<>();

        FinremCaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
        FinremCaseData caseDataBefore = caseDetailsBefore.getData();

        if (processOrderService.hasNoApprovedOrdersToProcess(caseData)) {
            return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
                .data(caseData).errors(List.of("There are no draft orders to be processed.")).build();
        }
        if (EventType.PROCESS_ORDER.equals(callbackRequest.getEventType())
            && validateHearingService.hasInvalidAdditionalHearingDocsForAddHearingChosen(caseData)) {
            return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
                    .data(caseData)
                    .errors(List.of("All additional hearing documents must be Word or PDF files."))
                    .build();
        }

        List<DirectionOrderCollection> uploadHearingOrders = filterNewItems(
            caseData.getUploadHearingOrder(),
            caseDataBefore.getUploadHearingOrder()
        );

        if (CollectionUtils.isNotEmpty(uploadHearingOrders)) {
            uploadHearingOrders.forEach(doc ->
                bulkPrintDocumentService.validateEncryptionOnUploadedDocument(doc.getValue().getUploadDraftDocument(),
                    caseDetails.getCaseIdAsString(), errors, userAuthorisation));
        }

        if (CollectionUtils.isNotEmpty(caseData.getHearingOrderOtherDocuments())) {
            List<DocumentCollectionItem> hearingOrderOtherDocuments = filterNewItems(
                caseData.getHearingOrderOtherDocuments(),
                caseDataBefore.getHearingOrderOtherDocuments()
            );
            if (CollectionUtils.isNotEmpty(hearingOrderOtherDocuments)) {
                hearingOrderOtherDocuments.forEach(doc ->
                    bulkPrintDocumentService.validateEncryptionOnUploadedDocument(doc.getValue(),
                        caseDetails.getCaseIdAsString(), errors, userAuthorisation));
            }
        }

        processOrderService.populateUnprocessedApprovedDocuments(caseDataBefore);
        if (!processOrderService.areAllNewOrdersWordOrPdfFiles(caseData)) {
            return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
                .data(caseData).errors(List.of("You must upload a Microsoft Word file or PDF for new documents.")).build();
        }
        // Validate the modifying legacy approved orders
        if (!processOrderService.areAllLegacyApprovedOrdersPdf(caseData)) {
            return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
                .data(caseData).errors(List.of("You must upload a PDF file for modifying legacy approved documents.")).build();
        }
        // Validate the modifying unprocessed approved orders are word documents (except PSA)
        if (!processOrderService.areAllModifyingUnprocessedOrdersWordOrPdfDocuments(caseData)) {
            return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
                .data(caseData).errors(List.of("You must upload a Microsoft Word file or PDF for modifying an unprocessed approved documents."))
                .build();
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData).errors(errors).build();
    }

    private <T> List<T> filterNewItems(List<T> currentList, List<T> previousList) {
        List<T> safePreviousList = emptyIfNull(previousList);
        return emptyIfNull(currentList)
            .stream()
            .filter(item -> safePreviousList.stream().noneMatch(item::equals))
            .toList();
    }
}
