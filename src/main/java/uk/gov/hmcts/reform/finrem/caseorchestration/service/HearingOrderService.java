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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DRAFT_DIRECTION_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FINAL_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.JUDGES_AMENDED_DIRECTION_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LATEST_DRAFT_DIRECTION_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LATEST_DRAFT_HEARING_ORDER;

@Service
@Slf4j
@RequiredArgsConstructor
public class HearingOrderService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentHelper documentHelper;
    private final ObjectMapper objectMapper;
    private final OrderDateService orderDateService;

    public void convertToPdfAndStampAndStoreLatestDraftHearingOrder(CaseDetails caseDetails, String authorisationToken) {
        Map<String, Object> caseData = caseDetails.getData();

        Optional<DraftDirectionOrder> judgeApprovedHearingOrder = getJudgeApprovedHearingOrder(caseDetails, authorisationToken);

        if (judgeApprovedHearingOrder.isPresent()) {
            String caseId = caseDetails.getId().toString();
            CaseDocument latestDraftDirectionOrderDocument = genericDocumentService.convertDocumentIfNotPdfAlready(
                judgeApprovedHearingOrder.get().getUploadDraftDocument(),
                authorisationToken, caseId);
            CaseDocument stampedHearingOrder = genericDocumentService.stampDocument(latestDraftDirectionOrderDocument,
                authorisationToken, documentHelper.getStampType(caseDetails.getData()), caseId);
            updateCaseDataForLatestDraftHearingOrder(caseData, stampedHearingOrder);
            updateCaseDataForLatestHearingOrderCollection(caseData, stampedHearingOrder, authorisationToken);
            appendDocumentToHearingOrderCollection(caseDetails, stampedHearingOrder);
        } else {
            throw new InvalidCaseDataException(BAD_REQUEST.value(), "Missing data from callbackRequest.");
        }
    }

    public boolean latestDraftDirectionOrderOverridesSolicitorCollection(CaseDetails caseDetails, String authorisationToken) {
        DraftDirectionOrder draftDirectionOrderCollectionTail = draftDirectionOrderCollectionTail(caseDetails, authorisationToken)
            .orElseThrow(IllegalArgumentException::new);

        Optional<DraftDirectionOrder> latestDraftDirectionOrder = Optional.ofNullable(caseDetails.getData().get(LATEST_DRAFT_DIRECTION_ORDER))
            .map(this::convertToDraftDirectionOrder);

        return latestDraftDirectionOrder.isPresent() && !latestDraftDirectionOrder.get().equals(draftDirectionOrderCollectionTail);
    }


    public void appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrders(FinremCaseDetails caseDetails) {
        FinremCaseData caseData = caseDetails.getData();

        List<DraftDirectionOrderCollection> judgesAmendedDirectionOrders
            = Optional.ofNullable(caseData.getDraftDirectionWrapper().getJudgesAmendedOrderCollection()).orElse(new ArrayList<>());

        Optional<DraftDirectionOrder> latestDraftDirectionOrder
            = Optional.ofNullable(caseData.getDraftDirectionWrapper().getLatestDraftDirectionOrder());

        if (latestDraftDirectionOrder.isPresent()) {
            DraftDirectionOrder draftDirectionOrder = latestDraftDirectionOrder.get();
            DraftDirectionOrder directionOrder = DraftDirectionOrder.builder()
                .uploadDraftDocument(draftDirectionOrder.getUploadDraftDocument())
                .purposeOfDocument(draftDirectionOrder.getPurposeOfDocument())
                .build();
            DraftDirectionOrderCollection directionOrderCollection = DraftDirectionOrderCollection.builder().value(directionOrder).build();
            judgesAmendedDirectionOrders.add(directionOrderCollection);
            caseData.getDraftDirectionWrapper().setJudgesAmendedOrderCollection(judgesAmendedDirectionOrders);
        }
    }

    public void appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrders(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();

        List<CollectionElement<DraftDirectionOrder>> judgesAmendedDirectionOrders = Optional.ofNullable(caseData.get(
                JUDGES_AMENDED_DIRECTION_ORDER_COLLECTION))
            .map(this::convertToListOfDraftDirectionOrder)
            .orElse(new ArrayList<>());

        Optional<DraftDirectionOrder> latestDraftDirectionOrder = Optional.ofNullable(caseData.get(LATEST_DRAFT_DIRECTION_ORDER))
            .map(this::convertToDraftDirectionOrder);

        if (latestDraftDirectionOrder.isPresent()) {
            judgesAmendedDirectionOrders.add(CollectionElement.<DraftDirectionOrder>builder()
                .value(latestDraftDirectionOrder.get())
                .build());
            caseData.put(JUDGES_AMENDED_DIRECTION_ORDER_COLLECTION, judgesAmendedDirectionOrders);
        }
    }

    public Optional<DraftDirectionOrder> draftDirectionOrderCollectionTail(CaseDetails caseDetails, String authorisationToken) {
        List<CollectionElement<DraftDirectionOrder>> draftDirectionOrders = Optional.ofNullable(caseDetails.getData()
                .get(DRAFT_DIRECTION_ORDER_COLLECTION))
            .map(this::convertToListOfDraftDirectionOrder)
            .orElse(emptyList());

        Optional<DraftDirectionOrder> draftDirectionOrder = draftDirectionOrders.isEmpty()
            ? Optional.empty()
            : Optional.of(draftDirectionOrders.get(draftDirectionOrders.size() - 1).getValue());

        if (draftDirectionOrder.isPresent()) {
            DraftDirectionOrder draftOrder = draftDirectionOrder.get();
            String caseId = caseDetails.getId().toString();
            return Optional.of(DraftDirectionOrder.builder().purposeOfDocument(draftOrder.getPurposeOfDocument())
                .uploadDraftDocument(
                    genericDocumentService.convertDocumentIfNotPdfAlready(
                        draftOrder.getUploadDraftDocument(),
                        authorisationToken, caseId))
                .build());

        }
        return Optional.empty();
    }

    @SuppressWarnings("java:S3358")
    private Optional<DraftDirectionOrder> getJudgeApprovedHearingOrder(CaseDetails caseDetails, String authorisationToken) {
        Optional<DraftDirectionOrder> draftDirectionOrderCollectionTail = draftDirectionOrderCollectionTail(caseDetails, authorisationToken);

        return draftDirectionOrderCollectionTail.isEmpty()
            ? Optional.empty()
            : latestDraftDirectionOrderOverridesSolicitorCollection(caseDetails, authorisationToken)
            ? Optional.ofNullable(caseDetails.getData().get(LATEST_DRAFT_DIRECTION_ORDER)).map(this::convertToDraftDirectionOrder)
            : draftDirectionOrderCollectionTail;
    }

    private void appendDocumentToHearingOrderCollection(CaseDetails caseDetails, CaseDocument document) {
        Map<String, Object> caseData = caseDetails.getData();

        List<CollectionElement<DirectionOrder>> directionOrders = Optional.ofNullable(caseData.get(HEARING_ORDER_COLLECTION))
            .map(this::convertToListOfDirectionOrder)
            .orElse(new ArrayList<>());

        DirectionOrder newDirectionOrder = DirectionOrder.builder().uploadDraftDocument(document).build();
        directionOrders.add(CollectionElement.<DirectionOrder>builder().value(newDirectionOrder).build());

        caseData.put(HEARING_ORDER_COLLECTION, directionOrders);
    }

    private void updateCaseDataForLatestDraftHearingOrder(Map<String, Object> caseData, CaseDocument stampedHearingOrder) {
        caseData.put(LATEST_DRAFT_HEARING_ORDER, stampedHearingOrder);
    }

    public void updateCaseDataForLatestHearingOrderCollection(Map<String, Object> caseData,
                                                              CaseDocument stampedHearingOrder,
                                                              String authorisationToken) {
        List<DirectionOrderCollection> finalOrderCollection = documentHelper.getFinalOrderCollection(caseData);
        List<DirectionOrderCollection> finalDatedCollection = orderDateService.addCreatedDateInFinalOrder(finalOrderCollection, authorisationToken);
        if (!documentHelper.checkIfOrderAlreadyInFinalOrderCollection(finalDatedCollection, stampedHearingOrder)) {
            DirectionOrderCollection latestOrder = DirectionOrderCollection.builder()
                .value(DirectionOrder.builder()
                    .uploadDraftDocument(stampedHearingOrder)
                    .orderDateTime(LocalDateTime.now())
                    .isOrderStamped(YesOrNo.YES)
                    .build())
                .build();
            finalDatedCollection.add(latestOrder);
        }
        caseData.put(FINAL_ORDER_COLLECTION, finalDatedCollection);
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
