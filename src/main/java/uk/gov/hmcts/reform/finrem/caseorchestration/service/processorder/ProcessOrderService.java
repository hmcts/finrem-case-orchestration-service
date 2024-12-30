package uk.gov.hmcts.reform.finrem.caseorchestration.service.processorder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.HasApprovable;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.PsaDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.draftorders.HasApprovableCollectionReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus.APPROVED_BY_JUDGE;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProcessOrderService {

    private final HasApprovableCollectionReader hasApprovableCollectionReader;

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
    public boolean isAllLegacyApprovedOrdersRemoved(FinremCaseData caseDataBefore, FinremCaseData caseData) {
        return !isUploadHearingOrderEmpty(caseDataBefore) && isUploadHearingOrderEmpty(caseData);
    }

    /**
     * Checks if all newly uploaded orders in the given case data are PDF documents.
     *
     * @param caseDataBefore the case data before the operation.
     * @param caseData       the case data after the operation.
     * @return true if all newly uploaded orders are PDF documents; false otherwise.
     */
    public boolean areAllNewUploadedOrdersPdfDocumentsPresent(FinremCaseData caseDataBefore, FinremCaseData caseData) {
        return areAllNewDocumentsPdf(caseDataBefore.getDraftOrdersWrapper().getUnprocessedApprovedDocuments(),
                caseData.getDraftOrdersWrapper().getUnprocessedApprovedDocuments(),
                doc -> ofNullable(doc)
                    .map(DirectionOrderCollection::getValue)
                    .map(DirectionOrder::getOriginalDocument)
                    .map(CaseDocument::getDocumentUrl)
                    .orElse(""))
            && areAllNewDocumentsPdf(caseDataBefore.getUploadHearingOrder(), caseData.getUploadHearingOrder(),
                doc -> ofNullable(doc)
                    .map(DirectionOrderCollection::getValue)
                    .map(DirectionOrder::getUploadDraftDocument)
                    .map(CaseDocument::getDocumentUrl)
                    .orElse(""));
    }

    private boolean areAllNewDocumentsPdf(List<DirectionOrderCollection> beforeList,
                                          List<DirectionOrderCollection> afterList,
                                          Function<DirectionOrderCollection, String> urlExtractor) {
        Set<String> beforeUrls = nullSafeList(beforeList).stream()
            .map(urlExtractor)
            .collect(Collectors.toSet());

        return nullSafeList(afterList).stream()
            .filter(doc -> !beforeUrls.contains(doc.getValue().getUploadDraftDocument().getDocumentUrl()))
            .allMatch(doc -> of(doc).map(DirectionOrderCollection::getValue).map(DirectionOrder::getUploadDraftDocument)
                .map(CaseDocument::getDocumentFilename).orElse("").matches("(?i).*\\.(pdf)$"));
    }

    private boolean isUploadHearingOrderEmpty(FinremCaseData caseData) {
        return nullSafeList(caseData.getUploadHearingOrder()).isEmpty();
    }

}
