package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetail;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetailCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.processorder.ProcessOrderService;

import java.util.ArrayList;
import java.util.List;

import static java.util.Optional.ofNullable;

@Slf4j
@Service
public class DirectionUploadOrderMidHandler extends FinremCallbackHandler {

    private final BulkPrintDocumentService bulkPrintDocumentService;

    private final ProcessOrderService processOrderService;

    public DirectionUploadOrderMidHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                          BulkPrintDocumentService bulkPrintDocumentService, ProcessOrderService processOrderService) {
        super(finremCaseDetailsMapper);
        this.bulkPrintDocumentService = bulkPrintDocumentService;
        this.processOrderService = processOrderService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.DIRECTION_UPLOAD_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        String caseId = String.valueOf(caseDetails.getId());
        log.info("Invoking contested event {} mid callback for Case ID: {}", callbackRequest.getEventType(), caseId);
        FinremCaseData caseData = caseDetails.getData();

        List<String> errors = new ArrayList<>();

        FinremCaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
        FinremCaseData caseDataBefore = caseDetailsBefore.getData();

        if (processOrderService.areAllLegacyApprovedOrdersRemoved(caseDataBefore, caseData)) {
            return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
                .data(caseData).errors(List.of("Upload Approved Order is required.")).build();
        }

        List<DirectionOrderCollection> uploadHearingOrders = filterNewItems(
            caseData.getUploadHearingOrder(),
            caseDataBefore.getUploadHearingOrder()
        );
        if (CollectionUtils.isNotEmpty(uploadHearingOrders)) {
            uploadHearingOrders.forEach(doc ->
                bulkPrintDocumentService.validateEncryptionOnUploadedDocument(doc.getValue().getUploadDraftDocument(),
                    caseId, errors, userAuthorisation));
        }

        if (CollectionUtils.isNotEmpty(caseData.getHearingOrderOtherDocuments())) {
            List<DocumentCollection> hearingOrderOtherDocuments = filterNewItems(
                caseData.getHearingOrderOtherDocuments(),
                caseDataBefore.getHearingOrderOtherDocuments()
            );
            if (CollectionUtils.isNotEmpty(hearingOrderOtherDocuments)) {
                hearingOrderOtherDocuments.forEach(doc ->
                    bulkPrintDocumentService.validateEncryptionOnUploadedDocument(doc.getValue(),
                        caseId, errors, userAuthorisation));
            }
        }

        processOrderService.populateUnprocessedApprovedDocuments(caseDataBefore);
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

        // Create an empty entry if it is empty to save a click on add new button
        if (ofNullable(caseData.getDirectionDetailsCollection()).orElse(List.of()).isEmpty()) {
            caseData.setDirectionDetailsCollection(List.of(
                DirectionDetailCollection.builder().value(DirectionDetail.builder().build()).build()
            ));
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData).errors(errors).build();
    }

    private <T> List<T> filterNewItems(List<T> currentList, List<T> previousList) {
        List<T> safePreviousList = ListUtils.emptyIfNull(previousList);
        return ListUtils.emptyIfNull(currentList)
            .stream()
            .filter(item -> safePreviousList.stream().noneMatch(item::equals))
            .toList();
    }
}