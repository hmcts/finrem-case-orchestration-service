package uk.gov.hmcts.reform.finrem.caseorchestration.service.processorder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.draftorders.HasApprovableCollectionReader;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProcessOrderService {

    private final HasApprovableCollectionReader hasApprovableCollectionReader;

    private static <T> List<T> nullSafeList(List<T> t) {
        return ofNullable(t).orElse(List.of());
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
            .allMatch(doc -> {
                String url = urlExtractor.apply(doc);
                return url != null && url.toLowerCase().matches(".*\\.(pdf)$");
            });
    }

    private boolean isUploadHearingOrderEmpty(FinremCaseData caseData) {
        return nullSafeList(caseData.getUploadHearingOrder()).isEmpty();
    }

}
