package uk.gov.hmcts.reform.finrem.caseorchestration.service.processorder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.HasApprovable;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.PsaDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.StampType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.draftorders.HasApprovableCollectionReader;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus.APPROVED_BY_JUDGE;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProcessOrderService {

    private final HasApprovableCollectionReader hasApprovableCollectionReader;
    private final DocumentHelper documentHelper;
    private final GenericDocumentService genericDocumentService;

    private static <T> List<T> nullSafeList(List<T> t) {
        return ofNullable(t).orElse(List.of());
    }

    /**
     * Populates the `unprocessedApprovedDocuments` collection in the `DraftOrdersWrapper`
     * with approved draft orders and PSA documents that have not been processed yet.
     *
     * <p>The method collects documents marked as approved by the judge from the
     * `draftOrdersReviewCollection` within the `DraftOrdersWrapper`. It then converts
     * these documents into `DirectionOrderCollection` objects and sets them in the
     * `unprocessedApprovedDocuments` field.
     *
     * @param caseData the case data containing the draft orders and PSA documents
     *                 to process.
     */
    public void populateUnprocessedApprovedDocuments(FinremCaseData caseData) {
        DraftOrdersWrapper draftOrdersWrapper = caseData.getDraftOrdersWrapper();

        List<DraftOrderDocReviewCollection> draftOrderCollector = new ArrayList<>();
        hasApprovableCollectionReader.filterAndCollectDraftOrderDocs(draftOrdersWrapper.getDraftOrdersReviewCollection(),
            draftOrderCollector, APPROVED_BY_JUDGE::equals);
        List<PsaDocReviewCollection> psaCollector = new ArrayList<>();
        hasApprovableCollectionReader.filterAndCollectPsaDocs(draftOrdersWrapper.getDraftOrdersReviewCollection(),
            psaCollector, APPROVED_BY_JUDGE::equals);

        Function<HasApprovable, DirectionOrderCollection> directionOrderCollectionConvertor = d -> DirectionOrderCollection.builder()
            .value(DirectionOrder.builder()
                .isOrderStamped(YesOrNo.NO) // It's not stamped in the new draft order flow
                .orderDateTime(d.getValue().getApprovalDate())
                .uploadDraftDocument(d.getValue().getTargetDocument())
                .originalDocument(d.getValue().getTargetDocument())
                .build())
            .build();

        List<DirectionOrderCollection> result = new ArrayList<>(draftOrderCollector.stream()
            .map(directionOrderCollectionConvertor).toList());
        result.addAll(psaCollector.stream()
            .map(directionOrderCollectionConvertor).toList());
        caseData.getDraftOrdersWrapper().setUnprocessedApprovedDocuments(result);
    }

    /**
     * Determines if all legacy approved orders have been removed by comparing the state of
     * the "upload hearing order" field in the provided case data before and after an update.
     *
     * @param caseDataBefore the case data before the update, used to check if legacy orders existed
     * @param caseData       the case data after the update, used to check if legacy orders have been removed
     * @return {@code true} if the "upload hearing order" was not empty in {@code caseDataBefore}
     *         and is empty in {@code caseData}, otherwise {@code false}
     */
    public boolean areAllLegacyApprovedOrdersRemoved(FinremCaseData caseDataBefore, FinremCaseData caseData) {
        return !isUploadHearingOrderEmpty(caseDataBefore) && isUploadHearingOrderEmpty(caseData);
    }

    /**
     * Checks if all newly uploaded orders in the given case data are PDF documents.
     *
     * @param caseDataBefore the case data before the operation.
     * @param caseData       the case data after the operation.
     * @return true if all newly uploaded orders are PDF documents; false otherwise.
     */
    public boolean areAllNewOrdersPdfFiles(FinremCaseData caseDataBefore, FinremCaseData caseData) {
        return areAllNewDocumentsPdf(caseData.getDraftOrdersWrapper().getUnprocessedApprovedDocuments())
            && areAllNewDocumentsPdf(caseData.getUploadHearingOrder()
        );
    }

    /**
     * Checks if all legacy approved orders in the case data are PDF files.
     *
     * <p>This method verifies that each document in the {@code uploadHearingOrder} collection
     * has the specified file extension (in this case, "pdf").</p>
     *
     * @param caseData the {@link FinremCaseData} object containing the uploaded hearing orders
     * @return {@code true} if all documents in {@code uploadHearingOrder} have the "pdf" extension; {@code false} otherwise
     */
    public boolean areAllLegacyApprovedOrdersPdf(FinremCaseData caseData) {
        return areAllDocumentsWithExtensions(caseData.getUploadHearingOrder(), List.of("pdf"));
    }

    /**
     * Checks if all the documents in the unprocessed approved draft orders of the given case data
     * have filenames with extensions matching the specified Word document formats (.doc or .docx).
     *
     * @param caseData the FinremCaseData object containing the draft orders wrapper
     *                 with unprocessed approved documents.
     * @return {@code true} if all unprocessed approved documents have filenames ending with
     *         ".doc" or ".docx" (case-insensitive), {@code false} otherwise.
     */
    public boolean areAllModifyingUnprocessedOrdersWordDocuments(FinremCaseData caseData) {
        return areAllDocumentsWithExtensions(nullSafeList(caseData.getDraftOrdersWrapper().getUnprocessedApprovedDocuments())
            .stream().filter(doc -> doc.getValue().getOriginalDocument() != null).toList(), List.of("doc", "docx"));
    }

    private boolean areAllDocumentsWithExtensions(List<DirectionOrderCollection> list, List<String> fileExtensions) {
        return nullSafeList(list).stream()
            .allMatch(doc -> of(doc).map(DirectionOrderCollection::getValue)
                .map(DirectionOrder::getUploadDraftDocument)
                .map(CaseDocument::getDocumentFilename)
                .orElse("")
                .matches(String.format("(?i).*\\.(%s)$", String.join("|", fileExtensions))));
    }

    private boolean areAllNewDocumentsPdf(List<DirectionOrderCollection> afterList) {

        return areAllDocumentsWithExtensions(nullSafeList(afterList).stream()
                .filter(doc -> doc.getValue().getOriginalDocument() == null).toList(), List.of("pdf"));
    }

    private boolean isUploadHearingOrderEmpty(FinremCaseData caseData) {
        return nullSafeList(caseData.getUploadHearingOrder()).isEmpty();
    }

    public CaseDocument convertToPdfAndStampDocument(FinremCaseDetails caseDetails,
                                              CaseDocument document,
                                              String authorisationToken) {

        String caseId = String.valueOf(caseDetails.getId());
        FinremCaseData caseData = caseDetails.getData();
        StampType stampType = documentHelper.getStampType(caseData);

        CaseDocument stampedDoc = genericDocumentService.stampDocument(document,
            authorisationToken, stampType, caseId);

        stampedDoc.setCategoryId(
            DocumentCategory.APPROVED_ORDERS.getDocumentCategoryId());

        return stampedDoc;
    }
}
