package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadedApprovedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadedApprovedOrderHolder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.UploadedDraftOrderCategoriser;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;

@Service
@Slf4j
@RequiredArgsConstructor
public class HearingOrderService {

    private enum ApprovedOrderUploader {
        CASEWORKER, JUDGE
    }

    private final GenericDocumentService genericDocumentService;
    private final DocumentHelper documentHelper;
    private final OrderDateService orderDateService;
    private final UploadedDraftOrderCategoriser uploadedDraftOrderCategoriser;

    /**
     * Converts caseworker-approved draft hearing orders to PDF if required,
     * stamps them with the appropriate court seal, and stores the results in the case data.
     *
     * <p>
     * Processing steps:
     * - Retrieves all caseworker-approved draft hearing orders from the case
     * - Ensures each order document is in PDF format
     * - Stamps the order document with the correct {@link StampType}
     * - Updates the latest draft hearing order in the case
     * - Appends the stamped order and any additional documents to the
     *   final orders and hearing orders collections
     *
     * @param finremCaseData     the financial remedy case data containing judge-approved orders
     * @param authorisationToken the service authorization token used for document conversion and stamping
     */
    public void stampAndStoreCwApprovedOrders(FinremCaseData finremCaseData, String authorisationToken) {
        synchroniseCreatedDateExistingApprovedOrder(finremCaseData, authorisationToken); // only CW
        convertAdditionalDocumentsToPdf(finremCaseData, authorisationToken);
        doStampAndStoreApprovedOrders(finremCaseData, authorisationToken, ApprovedOrderUploader.CASEWORKER);
    }

    /**
     * Converts judge-approved draft hearing orders to PDF if required,
     * stamps them with the appropriate court seal, and stores the results in the case data.
     *
     * <p>
     * Processing steps:
     * - Retrieves all judge-approved draft hearing orders from the case
     * - Ensures each order document is in PDF format
     * - Stamps the order document with the correct {@link StampType}
     * - Updates the latest draft hearing order in the case
     * - Appends the stamped order and any additional documents to the
     *   final orders and hearing orders collections
     *
     * @param finremCaseData     the financial remedy case data containing judge-approved orders
     * @param authorisationToken the service authorization token used for document conversion and stamping
     */
    public void stampAndStoreJudgeApprovedOrders(FinremCaseData finremCaseData, String authorisationToken) {
        convertAdditionalDocumentsToPdf(finremCaseData, authorisationToken); // only Judge first
        doStampAndStoreApprovedOrders(finremCaseData, authorisationToken, ApprovedOrderUploader.JUDGE);
    }

    public void appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrders(FinremCaseDetails caseDetails) {
        FinremCaseData caseData = caseDetails.getData();

        List<DraftDirectionOrderCollection> judgesAmendedDirectionOrders
            = ofNullable(caseData.getDraftDirectionWrapper().getJudgesAmendedOrderCollection()).orElse(new ArrayList<>());

        Optional<DraftDirectionOrder> latestDraftDirectionOrder
            = ofNullable(caseData.getDraftDirectionWrapper().getLatestDraftDirectionOrder());

        if (latestDraftDirectionOrder.isPresent()) {
            DraftDirectionOrder draftDirectionOrder = latestDraftDirectionOrder.get();
            DraftDirectionOrder directionOrder = DraftDirectionOrder.builder()
                .uploadDraftDocument(draftDirectionOrder.getUploadDraftDocument())
                .purposeOfDocument(draftDirectionOrder.getPurposeOfDocument())
                .build();
            DraftDirectionOrderCollection directionOrderCollection = DraftDirectionOrderCollection.builder().value(directionOrder).build();
            judgesAmendedDirectionOrders.add(directionOrderCollection);
            caseData.getDraftDirectionWrapper().setJudgesAmendedOrderCollection(judgesAmendedDirectionOrders);
            uploadedDraftOrderCategoriser.categorise(caseData);
        }
    }

    private List<DraftDirectionOrder> convertApprovedOrdersToPdfIfNeeded(FinremCaseData caseData, ApprovedOrderUploader approvedOrderUploader,
                                                                         String authorisationToken) {
        final String caseId = caseData.getCcdCaseId();
        List<? extends UploadedApprovedOrderHolder> orders = ApprovedOrderUploader.CASEWORKER == approvedOrderUploader
            ? caseData.getDraftDirectionWrapper().getCwApprovedOrderCollection()
            : caseData.getDraftDirectionWrapper().getJudgeApprovedOrderCollection();
        return emptyIfNull(orders)
            .stream()
            .map(orderCollection -> {
                UploadedApprovedOrder approvedOrder = orderCollection.getValue();
                return DraftDirectionOrder.builder()
                    .uploadDraftDocument(
                        genericDocumentService.convertDocumentIfNotPdfAlready(
                            approvedOrder.getApprovedOrder(),
                            authorisationToken,
                            caseId))
                    .additionalDocuments(approvedOrder.getAdditionalDocuments())
                    .build();
            })
            .toList();
    }

    private void appendStampedDocumentToUploadHearingOrder(FinremCaseData finremCaseData, CaseDocument stampedOrder,
                                                           List<DocumentCollectionItem> additionalDocs) {
        List<DirectionOrderCollection> directionOrders = ofNullable(finremCaseData.getUploadHearingOrder()).orElse(new ArrayList<>());
        directionOrders.add(
            DirectionOrderCollection.builder()
                .value(DirectionOrder.builder()
                    .uploadDraftDocument(stampedOrder)
                    .additionalDocuments(additionalDocs)
                    .build())
                .build()
        );
        finremCaseData.setUploadHearingOrder(directionOrders);
    }

    private void appendStampedOrderToFinalOrderCollection(FinremCaseData finremCaseData,
                                                          CaseDocument stampedOrder,
                                                          List<DocumentCollectionItem> additionalDocs) {
        List<DirectionOrderCollection> finalOrderCollection = finremCaseData.getFinalOrderCollection();
        DirectionOrderCollection latestOrder = DirectionOrderCollection.builder()
            .value(DirectionOrder.builder()
                .uploadDraftDocument(stampedOrder)
                .additionalDocuments(additionalDocs)
                .orderDateTime(LocalDateTime.now())
                .isOrderStamped(YesOrNo.YES)
                .build())
            .build();
        finalOrderCollection.add(latestOrder);
        finremCaseData.setFinalOrderCollection(finalOrderCollection);
    }

    private void synchroniseCreatedDateExistingApprovedOrder(FinremCaseData caseData, String authorisationToken) {
        List<DirectionOrderCollection> uploadHearingOrder = caseData.getUploadHearingOrder();
        caseData.setUploadHearingOrder(
            orderDateService.syncCreatedDateAndMarkDocumentNotStamped(uploadHearingOrder, authorisationToken)
        );
    }

    private void convertAdditionalDocumentsToPdf(FinremCaseData caseData, String authorisation) {
        List<DraftDirectionOrderCollection> judgeApprovedOrderCollection = caseData.getDraftDirectionWrapper().getJudgeApprovedOrderCollection();

        emptyIfNull(judgeApprovedOrderCollection).stream()
            .map(DraftDirectionOrderCollection::getValue)
            .map(DraftDirectionOrder::getAdditionalDocuments)
            .filter(CollectionUtils::isNotEmpty)
            .flatMap(List::stream)
            .forEach(additionalDoc -> {
                CaseDocument documentPdf = genericDocumentService.convertDocumentIfNotPdfAlready(
                    additionalDoc.getValue(), authorisation, String.valueOf(caseData.getCcdCaseId()));
                additionalDoc.setValue(documentPdf);
            });
    }

    private void doStampAndStoreApprovedOrders(FinremCaseData finremCaseData, String authorisationToken,
                                               ApprovedOrderUploader uploader) {
        String caseId = finremCaseData.getCcdCaseId();
        StampType stampType = documentHelper.getStampType(finremCaseData);

        finremCaseData.setFinalOrderCollection(orderDateService.syncCreatedDateAndMarkDocumentStamped(
            finremCaseData.getFinalOrderCollection(), authorisationToken));

        convertApprovedOrdersToPdfIfNeeded(finremCaseData, uploader, authorisationToken)
            .forEach(order -> handleApprovedOrder(finremCaseData, order, authorisationToken, stampType, caseId));
    }

    private void handleApprovedOrder(FinremCaseData finremCaseData, UploadedApprovedOrder order,
                                     String authorisationToken, StampType stampType, String caseId) {
        CaseDocument stampedDocument = genericDocumentService.stampDocument(
            order.getApprovedOrder(), authorisationToken, stampType, caseId);

        // Store "Latest draft hearing order"
        setLatestDraftHearingOrder(finremCaseData, stampedDocument);

        List<DocumentCollectionItem> additionalDocs = order.getAdditionalDocuments();

        // make the uploaded approved orders available in Order tab
        appendStampedOrderToFinalOrderCollection(finremCaseData, stampedDocument, additionalDocs);

        // make the uploaded approved orders available in Process Order event
        appendStampedDocumentToUploadHearingOrder(finremCaseData, stampedDocument, additionalDocs);
    }

    private void setLatestDraftHearingOrder(FinremCaseData finremCaseData, CaseDocument stampedOrder) {
        finremCaseData.setLatestDraftHearingOrder(stampedOrder);
    }
}
