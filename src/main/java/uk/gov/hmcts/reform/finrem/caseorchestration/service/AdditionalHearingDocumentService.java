package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetail;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetailCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdditionalHearingDocumentService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentHelper documentHelper;
    private final OrderDateService orderDateService;

    public void sortDirectionDetailsCollection(FinremCaseData caseData) {
        List<DirectionDetailCollection> directionDetailsCollection
            = Optional.ofNullable(caseData.getDirectionDetailsCollection()).orElse(new ArrayList<>());
        if (!directionDetailsCollection.isEmpty()) {
            directionDetailsCollection.sort(Comparator.comparing(
                DirectionDetailCollection::getValue, Comparator.comparing(
                    DirectionDetail::getDateOfHearing, Comparator.nullsLast(
                        Comparator.reverseOrder()
                    )
                )
            ));
        }
    }

    private DirectionOrderCollection getDirectionOrderCollection(DirectionOrder originalDirectionOrder,
                                                                 CaseDocument caseDocument, LocalDateTime orderDateTime) {
        return DirectionOrderCollection.builder().value(originalDirectionOrder.toBuilder()
            .uploadDraftDocument(caseDocument)
            .orderDateTime(orderDateTime)
            .isOrderStamped(YesOrNo.YES)
            .build()).build();
    }

    /**
     * Stamps and collects orders from unprocessedUploadHearingDocuments, updating uploadHearingOrder and finalOrderCollection.
     *
     * @param caseDetails the case details containing the orders
     * @param authorisationToken the authorisation token for document stamping
     */
    public void stampAndUpdateOrderCollections(FinremCaseDetails caseDetails, String authorisationToken) {
        String caseId = caseDetails.getId().toString();
        log.info("Processing unprocessed upload hearing documents for Case ID: {}", caseId);
        FinremCaseData caseData = caseDetails.getData();

        List<DirectionOrderCollection> finalOrderCollection = orderDateService
            .syncCreatedDateAndMarkDocumentStamped(caseData.getFinalOrderCollection(), authorisationToken);
        List<DirectionOrderCollection> newFinalOrderCollection = new ArrayList<>(emptyIfNull(caseData.getFinalOrderCollection()));

        List<DirectionOrderCollection> unprocessedOrders = Optional.ofNullable(caseData.getUnprocessedUploadHearingDocuments())
            .orElse(List.of());

        if (!unprocessedOrders.isEmpty()) {
            List<DirectionOrderCollection> updatedUploadHearingOrder = new ArrayList<>(emptyIfNull(caseData.getUploadHearingOrder()));

            for (DirectionOrderCollection unprocessedOrder : unprocessedOrders) {
                CaseDocument uploadDraftDocument = unprocessedOrder.getValue().getUploadDraftDocument();
                LocalDateTime orderDateTime = unprocessedOrder.getValue().getOrderDateTime();

                CaseDocument stampedDocument = getStampedDocs(authorisationToken, caseData, caseDetails.getCaseType(), uploadDraftDocument);
                log.info("Stamped document {} for Case ID: {}", stampedDocument.getDocumentFilename(), caseId);

                // Replace or add the stamped order in uploadHearingOrder
                replaceOrAddOrder(updatedUploadHearingOrder, uploadDraftDocument, stampedDocument, orderDateTime);

                // Add to finalOrderCollection if not already present
                if (!documentHelper.checkIfOrderAlreadyInFinalOrderCollection(finalOrderCollection, uploadDraftDocument)) {
                    newFinalOrderCollection.add(documentHelper.prepareFinalOrder(stampedDocument,
                        unprocessedOrder.getValue().getAdditionalDocuments()));
                }
            }

            caseData.setUploadHearingOrder(updatedUploadHearingOrder);
            caseData.setFinalOrderCollection(newFinalOrderCollection);
            if (!updatedUploadHearingOrder.isEmpty()) {
                caseData.setLatestDraftHearingOrder(updatedUploadHearingOrder.getLast().getValue().getUploadDraftDocument());
            }
        }
    }

    /**
     * Replaces an order in the collection if it exists, otherwise adds it.
     *
     * @param orders the list of orders to update
     * @param originalDoc the original document to match
     * @param stampedDoc the new stamped document
     * @param orderDateTime the order date time
     */
    private void replaceOrAddOrder(List<DirectionOrderCollection> orders, CaseDocument originalDoc, CaseDocument stampedDoc,
                                   LocalDateTime orderDateTime) {
        for (int i = 0; i < orders.size(); i++) {
            if (orders.get(i).getValue().getUploadDraftDocument().getDocumentUrl().equals(originalDoc.getDocumentUrl())) {
                orders.set(i, getDirectionOrderCollection(orders.get(i).getValue(), stampedDoc, orderDateTime));
                return;
            }
        }
        // If not found, add as a new stamped order
        orders.add(DirectionOrderCollection.builder()
            .value(DirectionOrder.builder()
                .uploadDraftDocument(stampedDoc)
                .orderDateTime(orderDateTime)
                .isOrderStamped(YesOrNo.YES)
                .build())
            .build());
    }

    private CaseDocument getStampedDocs(String authorisationToken, FinremCaseData caseData, CaseType caseType, CaseDocument uploadDraftDocument) {
        CaseDocument caseDocument = genericDocumentService.convertDocumentIfNotPdfAlready(uploadDraftDocument, authorisationToken, caseType);
        StampType stampType = documentHelper.getStampType(caseData);
        return genericDocumentService.stampDocument(caseDocument, authorisationToken, stampType, caseType);
    }
}
