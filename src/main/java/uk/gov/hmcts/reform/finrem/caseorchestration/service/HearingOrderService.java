package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.InvalidCaseDataException;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CollectionElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.UploadedDraftOrderCategoriser;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.JUDGES_AMENDED_DIRECTION_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LATEST_DRAFT_DIRECTION_ORDER;

@Service
@Slf4j
@RequiredArgsConstructor
public class HearingOrderService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentHelper documentHelper;
    private final ObjectMapper objectMapper;
    private final OrderDateService orderDateService;
    private final UploadedDraftOrderCategoriser uploadedDraftOrderCategoriser;

    // Mirroring logic of convertToPdfAndStampAndStoreLatestDraftHearingOrder(CaseDetails, ...) for FinremCaseData
    public void convertLastJudgeApprovedOrderToPdfAndStampAndStoreLatestDraftHearingOrder(FinremCaseData finremCaseData, String authorisationToken) {
        Optional<DraftDirectionOrder> judgeApprovedHearingOrder = getJudgeApprovedLastHearingOrder(finremCaseData, authorisationToken);

        String caseId = finremCaseData.getCcdCaseId();
        if (judgeApprovedHearingOrder.isPresent()) {
            DraftDirectionOrder order = judgeApprovedHearingOrder.get();
            CaseDocument latestDraftDirectionOrderDocument = genericDocumentService.convertDocumentIfNotPdfAlready(
                order.getUploadDraftDocument(),
                authorisationToken, caseId);
            CaseDocument stampedHearingOrder = genericDocumentService.stampDocument(latestDraftDirectionOrderDocument,
                authorisationToken, documentHelper.getStampType(finremCaseData), caseId);
            updateCaseDataForLatestDraftHearingOrder(finremCaseData, stampedHearingOrder);
            List<DocumentCollectionItem> additionalDocs = order.getAdditionalDocuments();
            updateCaseDataForLatestHearingOrderCollection(finremCaseData, stampedHearingOrder, authorisationToken, additionalDocs);
            appendDocumentToHearingOrderCollection(finremCaseData, stampedHearingOrder, additionalDocs);
        } else {
            throw new InvalidCaseDataException(BAD_REQUEST.value(),
                "Missing data from callbackRequest for Case ID: " + caseId);
        }
    }

    // Mirroring logic of latestDraftDirectionOrderOverridesSolicitorCollection(CaseDetails, ...) for FinremCaseData
    public boolean latestDraftDirectionOrderOverridesSolicitorCollection(FinremCaseData finremCaeData, String authorisationToken) {
        DraftDirectionOrder draftDirectionOrderCollectionTail = draftDirectionOrderCollectionTail(finremCaeData, authorisationToken)
            .orElseThrow(IllegalArgumentException::new);

        Optional<DraftDirectionOrder> latestDraftDirectionOrder = ofNullable(finremCaeData.getDraftDirectionWrapper().getLatestDraftDirectionOrder())
            .map(this::convertToDraftDirectionOrder);

        return latestDraftDirectionOrder.isPresent() && !latestDraftDirectionOrder.get().equals(draftDirectionOrderCollectionTail);
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

    public void appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrders(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();

        List<CollectionElement<DraftDirectionOrder>> judgesAmendedDirectionOrders = ofNullable(caseData.get(
                JUDGES_AMENDED_DIRECTION_ORDER_COLLECTION))
            .map(this::convertToListOfDraftDirectionOrder)
            .orElse(new ArrayList<>());

        Optional<DraftDirectionOrder> latestDraftDirectionOrder = ofNullable(caseData.get(LATEST_DRAFT_DIRECTION_ORDER))
            .map(this::convertToDraftDirectionOrder);

        if (latestDraftDirectionOrder.isPresent()) {
            judgesAmendedDirectionOrders.add(CollectionElement.<DraftDirectionOrder>builder()
                .value(latestDraftDirectionOrder.get())
                .build());
            caseData.put(JUDGES_AMENDED_DIRECTION_ORDER_COLLECTION, judgesAmendedDirectionOrders);
        }
    }

    // Mirroring logic of draftDirectionOrderCollectionTail(CaseDetails, ...) for FinremCaseData
    // The other uploaded orders should be processed which will be covered by DFR-3655
    public Optional<DraftDirectionOrder> draftDirectionOrderCollectionTail(FinremCaseData finremCaseData, String authorisationToken) {
        List<DraftDirectionOrderCollection> draftDirectionOrders
            = emptyIfNull(finremCaseData.getDraftDirectionWrapper().getJudgeApprovedOrderCollection());

        Optional<DraftDirectionOrder> draftDirectionOrder = draftDirectionOrders.isEmpty()
            ? Optional.empty()
            : Optional.of(draftDirectionOrders.getLast().getValue());

        if (draftDirectionOrder.isPresent()) {
            DraftDirectionOrder draftOrder = draftDirectionOrder.get();
            String caseId = finremCaseData.getCcdCaseId();
            return Optional.of(DraftDirectionOrder.builder().purposeOfDocument(draftOrder.getPurposeOfDocument())
                .uploadDraftDocument(
                    genericDocumentService.convertDocumentIfNotPdfAlready(
                        draftOrder.getUploadDraftDocument(),
                        authorisationToken, caseId))
                .additionalDocuments(draftOrder.getAdditionalDocuments())
                .build());
        }
        return Optional.empty();
    }

    // Mirroring logic of getJudgeApprovedHearingOrder(CaseDetails, ...) for FinremCaseData
    private Optional<DraftDirectionOrder> getJudgeApprovedLastHearingOrder(FinremCaseData finremCaseData, String authorisationToken) {
        Optional<DraftDirectionOrder> draftDirectionOrderCollectionTail = draftDirectionOrderCollectionTail(finremCaseData, authorisationToken);

        return draftDirectionOrderCollectionTail.isEmpty()
            ? Optional.empty()
            : latestDraftDirectionOrderOverridesSolicitorCollection(finremCaseData, authorisationToken)
            ? ofNullable(finremCaseData.getDraftDirectionWrapper().getLatestDraftDirectionOrder()).map(this::convertToDraftDirectionOrder)
            : draftDirectionOrderCollectionTail;
    }

    // Mirroring logic of appendDocumentToHearingOrderCollection(CaseDetails, ...) for FinremCaseData
    private void appendDocumentToHearingOrderCollection(FinremCaseData finremCaseData, CaseDocument document, List<DocumentCollectionItem> additionalDocs) {
        List<DirectionOrderCollection> directionOrders = ofNullable(finremCaseData.getUploadHearingOrder()).orElse(new ArrayList<>());

        DirectionOrder newDirectionOrder = DirectionOrder.builder().uploadDraftDocument(document).additionalDocuments(additionalDocs).build();
        directionOrders.add(DirectionOrderCollection.builder().value(newDirectionOrder).build());

        finremCaseData.setUploadHearingOrder(directionOrders);
    }

    private void updateCaseDataForLatestDraftHearingOrder(FinremCaseData finremCaseData, CaseDocument stampedHearingOrder) {
        finremCaseData.setLatestDraftHearingOrder(stampedHearingOrder);
    }

    public void updateCaseDataForLatestHearingOrderCollection(FinremCaseData finremCaseData,
                                                              CaseDocument stampedHearingOrder,
                                                              String authorisationToken,
                                                              List<DocumentCollectionItem> additionalDocs) {
        List<DirectionOrderCollection> finalOrderCollection = finremCaseData.getFinalOrderCollection();
        List<DirectionOrderCollection> finalDatedCollection = orderDateService.syncCreatedDateAndMarkDocumentStamped(finalOrderCollection, authorisationToken);
        if (!documentHelper.checkIfOrderAlreadyInFinalOrderCollection(finalDatedCollection, stampedHearingOrder)) {
            DirectionOrderCollection latestOrder = DirectionOrderCollection.builder()
                .value(DirectionOrder.builder()
                    .uploadDraftDocument(stampedHearingOrder)
                    .additionalDocuments(additionalDocs)
                    .orderDateTime(LocalDateTime.now())
                    .isOrderStamped(YesOrNo.YES)
                    .build())
                .build();
            finalDatedCollection.add(latestOrder);
        }
        finremCaseData.setFinalOrderCollection(finalDatedCollection);
    }

    private DraftDirectionOrder convertToDraftDirectionOrder(Object value) {
        return objectMapper.convertValue(value, new TypeReference<>() {
        });
    }

    private List<CollectionElement<DraftDirectionOrder>> convertToListOfDraftDirectionOrder(Object value) {
        return objectMapper.convertValue(value, new TypeReference<>() {
        });
    }

    private List<CollectionElement<DirectionOrder>> convertToListOfDirectionOrder(Object value) {
        return objectMapper.convertValue(value, new TypeReference<>() {
        });
    }
}
