package uk.gov.hmcts.reform.finrem.caseorchestration.service.processorder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.draftorders.HasApprovableCollectionReader;

import java.util.List;
import java.util.Set;
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
     * Checks if all newly uploaded orders have PDF document extensions.
     *
     * <p>This method compares the list of previously uploaded approved documents with the current ones and filters out the new documents.
     * It then verifies if all these new documents have a PDF extension (case-insensitive).</p>
     *
     * @param caseDataBefore The case data before any new documents were uploaded.
     * @param caseData The current case data containing newly uploaded documents.
     * @return {@code true} if all new documents have PDF extensions; {@code false} otherwise.
     */
    public boolean isAllNewUploadedOrdersArePdfDocuments(FinremCaseData caseDataBefore, FinremCaseData caseData) {
        List<DirectionOrderCollection> before = nullSafeList(caseDataBefore.getDraftOrdersWrapper().getUnprocessedApprovedDocuments());
        List<DirectionOrderCollection> after = nullSafeList(caseData.getDraftOrdersWrapper().getUnprocessedApprovedDocuments());

        Set<String> beforeUrls = before.stream()
            .map(doc -> doc.getValue().getOriginalDocument().getDocumentUrl())
            .collect(Collectors.toSet());

        // Filter "after" list for new documents
        List<DirectionOrderCollection> newOrders = after.stream()
            .filter(doc -> !beforeUrls.contains(doc.getValue().getOriginalDocument().getDocumentUrl()))
            .toList();

        // Check if all new documents have the required extensions
        return newOrders.stream()
            .allMatch(doc -> {
                String url = doc.getValue().getOriginalDocument().getDocumentUrl();
                return url != null && url.toLowerCase().matches(".*\\.(pdf)$");
            });
    }

    private boolean isUploadHearingOrderEmpty(FinremCaseData caseData) {
        return nullSafeList(caseData.getUploadHearingOrder()).isEmpty();
    }

}
