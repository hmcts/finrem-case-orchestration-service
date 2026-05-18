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
    private static final String ERROR_NO_ORDERS = "There are no draft orders to be processed.";
    private static final String ERROR_NEW_DOCS = "You must upload a Microsoft Word file or PDF for new documents.";
    private static final String ERROR_FILE_FORMAT = "You must upload a Microsoft Word file or PDF for modifying an unprocessed document.";
    private static final String WARNING_ORDERS_HAVE_CHANGED = "Existing unprocessed hearing order(s) have been removed or amended. "
        + "This is not supported. Existing orders will remain unprocessed.";
    private static final String ERROR_ADDITIONAL_FILE_FORMAT = "All additional hearing documents must be Word or PDF files.";

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
        log.info(CallbackHandlerLogger.midEvent(callbackRequest));
        FinremCaseData caseData = callbackRequest.getFinremCaseData();

        if (processOrderService.hasNoApprovedOrdersToProcess(caseData)) {
            return response(caseData, null, List.of(ERROR_NO_ORDERS));
        }
        if (EventType.PROCESS_ORDER.equals(callbackRequest.getEventType())
            && validateHearingService.hasInvalidAdditionalHearingDocsForAddHearingChosen(caseData)) {
            return response(caseData, null, List.of(ERROR_ADDITIONAL_FILE_FORMAT));
        }

        if (showExistingOrdersHaveBeenAlteredWarning(caseData)) {
            return response(caseData, List.of(WARNING_ORDERS_HAVE_CHANGED), null);
        }

        FinremCaseData caseDataBefore = callbackRequest.getFinremCaseDataBefore();
        List<DirectionOrderCollection> uploadHearingOrders = filterNewItems(
            caseData.getUnprocessedUploadHearingDocuments(),
            caseDataBefore.getUnprocessedUploadHearingDocuments()
        );

        List<String> errors = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(uploadHearingOrders)) {
            uploadHearingOrders.forEach(doc ->
                bulkPrintDocumentService.validateEncryptionOnUploadedDocument(doc.getValue().getUploadDraftDocument(),
                    caseData.getCcdCaseId(), errors, userAuthorisation));
        }

        if (CollectionUtils.isNotEmpty(caseData.getHearingOrderOtherDocuments())) {
            List<DocumentCollectionItem> hearingOrderOtherDocuments = filterNewItems(
                caseData.getHearingOrderOtherDocuments(),
                caseDataBefore.getHearingOrderOtherDocuments()
            );
            if (CollectionUtils.isNotEmpty(hearingOrderOtherDocuments)) {
                hearingOrderOtherDocuments.forEach(doc ->
                    bulkPrintDocumentService.validateEncryptionOnUploadedDocument(doc.getValue(),
                        caseData.getCcdCaseId(), errors, userAuthorisation));
            }
        }

        processOrderService.populateUnprocessedApprovedDocuments(caseDataBefore);
        processOrderService.populateUnprocessedUploadHearingDocuments(caseDataBefore);
        if (!processOrderService.areAllNewOrdersWordOrPdfFiles(caseData)) {
            return response(caseData, null, List.of(ERROR_NEW_DOCS));
        }
        // Validate the modifying legacy approved orders
        if (!processOrderService.areAllLegacyApprovedOrdersWordOrPdf(caseData)) {
            return response(caseData, null, List.of(ERROR_FILE_FORMAT));
        }
        // Validate the modifying unprocessed approved orders are word documents (except PSA)
        if (!processOrderService.areAllModifyingUnprocessedOrdersWordOrPdfDocuments(caseData)) {
            return response(caseData, null, List.of(ERROR_FILE_FORMAT));
        }

        return response(caseData, null, errors);
    }

    private <T> List<T> filterNewItems(List<T> currentList, List<T> previousList) {
        List<T> safePreviousList = emptyIfNull(previousList);
        return emptyIfNull(currentList)
            .stream()
            .filter(item -> safePreviousList.stream().noneMatch(item::equals))
            .toList();
    }

    /*
     * Function to determine if a warning should be shown to the user to say that existing unprocessed hearing order(s)
     * have been altered.
     * @param caseData the current case data.
     * @return true if the user should see a warning.
     */
    private boolean showExistingOrdersHaveBeenAlteredWarning(FinremCaseData caseData) {
        List<DirectionOrderCollection> intendedUploadHearingOrderList = caseData.getUnprocessedUploadHearingDocuments();
        List<DirectionOrderCollection> currentStoredUploadHearingOrderList = caseData.getUploadHearingOrder();

        boolean onTheRightPage = onValidatingUnprocessedHearingOrdersPage(caseData);
        boolean existingOrdersHaveBeenAltered = processOrderService.uploadHearingOrderListAlteredOrRemoved(
            intendedUploadHearingOrderList,
            currentStoredUploadHearingOrderList);

        return onTheRightPage && existingOrdersHaveBeenAltered;
    }

    /*
     * Simple function to determine if this mid-event handler is currently validating the Unprocessed Hearing Orders page.
     * To do this, makes a simple check to see if the "Add Hearing" option has been selected, if so, User is on the
     * "Do you want to add a hearing?" page instead.  This mid-event only handles two pages, so this works.
     * @param caseData the current case data
     * @return true if the user is currently on the Unprocessed Hearing Orders page, false otherwise
     */
    private boolean onValidatingUnprocessedHearingOrdersPage(FinremCaseData caseData) {
        return caseData.getManageHearingsWrapper().getIsAddHearingChosen() == null;
    }
}
