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
        convertAdditionalDocumentsToPdf(finremCaseData, authorisationToken, ApprovedOrderUploader.CASEWORKER);
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
        convertAdditionalDocumentsToPdf(finremCaseData, authorisationToken, ApprovedOrderUploader.JUDGE); // only Judge first
        doStampAndStoreApprovedOrders(finremCaseData, authorisationToken, ApprovedOrderUploader.JUDGE);
    }

    /**
     * DFR-4185 - This method is deprecated and should no longer be used.
     *
     * <p>
     * Previously, it appended the latest draft direction order to the judge's amended direction
     * orders collection in {@link FinremCaseData}. However, the
     * {@code judgesAmendedOrderCollection} field is no longer used or displayed anywhere in the system,
     * so this method no longer has any functional effect.
     * It remains in the codebase temporarily for backward compatibility and will be removed
     * in a future release.
     *
     * @param caseDetails the {@link FinremCaseDetails} containing case data
     */
    @Deprecated
    public void appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrders(FinremCaseDetails caseDetails) {
        // DFR-4185 to deprecate it
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

    private List<DraftDirectionOrder> convertCaseWorkerApprovedOrdersToPdfIfNeeded(FinremCaseData caseData,
                                                                                   String authorisationToken) {
        String caseId = caseData.getCcdCaseId();
        List<? extends UploadedApprovedOrderHolder> caseworkerOrders =
            emptyIfNull(caseData.getDraftDirectionWrapper().getCwApprovedOrderCollection());

        return caseworkerOrders.stream()
            .map(orderHolder -> {
                UploadedApprovedOrder approvedOrder = orderHolder.getValue();
                CaseDocument pdfDocument = genericDocumentService.convertDocumentIfNotPdfAlready(
                    approvedOrder.getApprovedOrder(), authorisationToken, caseId);
                return DraftDirectionOrder.builder()
                    .uploadDraftDocument(pdfDocument)
                    .additionalDocuments(approvedOrder.getAdditionalDocuments())
                    .build();
            })
            .toList();
    }

    private void appendDocumentToUploadHearingOrder(FinremCaseData finremCaseData, CaseDocument order,
                                                    List<DocumentCollectionItem> additionalDocs, YesOrNo isOrderStamped) {
        List<DirectionOrderCollection> directionOrders = ofNullable(finremCaseData.getUploadHearingOrder()).orElse(new ArrayList<>());
        directionOrders.add(
            DirectionOrderCollection.builder()
                .value(DirectionOrder.builder()
                    .uploadDraftDocument(order)
                    .additionalDocuments(additionalDocs)
                    .isOrderStamped(isOrderStamped)
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
        /*
            TODO (DFR-4184): Seems like it's a bug that it marks the existing documents to be not stamped.
                  Have to study how we use `isOrderStamped` field.
                  For the original logic, please see commit 628fe5dd.
                  Below logic was copied from AdditionalHearingDocumentService and is called by
                  - UploadApprovedOrderService.processApprovedOrders
                  - UploadApprovedOrderService.processApprovedOrdersMh
        */
        caseData.setUploadHearingOrder(
            orderDateService.syncCreatedDateAndMarkDocumentNotStamped(caseData.getUploadHearingOrder(), authorisationToken)
        );
    }

    private void convertAdditionalDocumentsToPdf(FinremCaseData caseData, String authorisation,
                                                 ApprovedOrderUploader uploader) {
        List<? extends UploadedApprovedOrderHolder> orders = ApprovedOrderUploader.CASEWORKER == uploader
            ? caseData.getDraftDirectionWrapper().getCwApprovedOrderCollection()
            : caseData.getDraftDirectionWrapper().getJudgeApprovedOrderCollection();

        emptyIfNull(orders).stream()
            .map(UploadedApprovedOrderHolder::getValue)
            .map(UploadedApprovedOrder::getAdditionalDocuments)
            .filter(CollectionUtils::isNotEmpty)
            .flatMap(List::stream)
            .forEach(additionalDoc -> {
                CaseDocument documentPdf = genericDocumentService.convertDocumentIfNotPdfAlready(
                    additionalDoc.getValue(), authorisation, caseData.getCcdCaseId());
                additionalDoc.setValue(documentPdf);
            });
    }

    private void doStampAndStoreApprovedOrders(FinremCaseData finremCaseData, String authorisationToken,
                                               ApprovedOrderUploader uploader) {
        String caseId = finremCaseData.getCcdCaseId();
        StampType stampType = documentHelper.getStampType(finremCaseData);

        finremCaseData.setFinalOrderCollection(orderDateService.syncCreatedDateAndMarkDocumentStamped(
            finremCaseData.getFinalOrderCollection(), authorisationToken));

        if (ApprovedOrderUploader.CASEWORKER == uploader) {
            convertCaseWorkerApprovedOrdersToPdfIfNeeded(finremCaseData, authorisationToken)
                .forEach(order -> handleApprovedOrder(finremCaseData, order, authorisationToken, stampType, caseId, uploader));
        } else {
            List<? extends UploadedApprovedOrderHolder> judgeOrders = emptyIfNull(
                finremCaseData.getDraftDirectionWrapper().getJudgeApprovedOrderCollection());
            judgeOrders.stream()
                .map(UploadedApprovedOrderHolder::getValue)
                .forEach(order -> handleApprovedOrder(finremCaseData, order, authorisationToken, stampType, caseId, uploader));
        }
    }

    private void handleApprovedOrder(FinremCaseData finremCaseData, UploadedApprovedOrder order,
                                     String authorisationToken, StampType stampType, String caseId, ApprovedOrderUploader uploader) {

        List<DocumentCollectionItem> additionalDocs = order.getAdditionalDocuments();
        YesOrNo isOrderStamped;
        if (ApprovedOrderUploader.CASEWORKER == uploader) {
            CaseDocument stampedDocument = genericDocumentService.stampDocument(
                order.getApprovedOrder(), authorisationToken, stampType, caseId);
            isOrderStamped = YesOrNo.YES;
            appendDocumentToUploadHearingOrder(finremCaseData, stampedDocument, additionalDocs, isOrderStamped);
            appendStampedOrderToFinalOrderCollection(finremCaseData, stampedDocument, additionalDocs);
            setLatestDraftHearingOrder(finremCaseData, stampedDocument);
        } else {
            isOrderStamped = YesOrNo.NO;
            // make the uploaded approved orders available for judge uploaded orders in Process Order event
            appendDocumentToUploadHearingOrder(finremCaseData, order.getApprovedOrder(), additionalDocs, isOrderStamped);
        }
    }

    private void setLatestDraftHearingOrder(FinremCaseData finremCaseData, CaseDocument stampedOrder) {
        finremCaseData.setLatestDraftHearingOrder(stampedOrder);
    }
}
