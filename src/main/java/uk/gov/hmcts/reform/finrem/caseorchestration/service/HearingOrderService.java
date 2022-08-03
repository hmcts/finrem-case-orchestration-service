package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.google.common.collect.Iterables;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.InvalidCaseDataException;
import uk.gov.hmcts.reform.finrem.ccd.domain.DirectionOrder;
import uk.gov.hmcts.reform.finrem.ccd.domain.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.DraftDirectionOrder;
import uk.gov.hmcts.reform.finrem.ccd.domain.DraftDirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
@Slf4j
@RequiredArgsConstructor
public class HearingOrderService {

    private final GenericDocumentService genericDocumentService;

    public void convertToPdfAndStampAndStoreLatestDraftHearingOrder(FinremCaseDetails caseDetails, String authorisationToken) {
        FinremCaseData caseData = caseDetails.getCaseData();

        Optional<DraftDirectionOrder> judgeApprovedHearingOrder = getJudgeApprovedHearingOrder(caseDetails);

        if (judgeApprovedHearingOrder.isPresent()) {
            Document latestDraftDirectionOrderDocument = genericDocumentService.convertDocumentIfNotPdfAlready(
                judgeApprovedHearingOrder.get().getUploadDraftDocument(),
                authorisationToken);
            Document stampedHearingOrder = genericDocumentService.stampDocument(latestDraftDirectionOrderDocument, authorisationToken);
            updateCaseDataForLatestDraftHearingOrder(caseData, stampedHearingOrder);
            updateCaseDataForLatestHearingOrderCollection(caseData, stampedHearingOrder);
            appendDocumentToHearingOrderCollection(caseDetails, stampedHearingOrder);
        } else {
            throw new InvalidCaseDataException(BAD_REQUEST.value(), "Missing data from callbackRequest.");
        }
    }

    public boolean latestDraftDirectionOrderOverridesSolicitorCollection(FinremCaseDetails caseDetails) {
        DraftDirectionOrder draftDirectionOrderCollectionTail = draftDirectionOrderCollectionTail(caseDetails)
            .orElseThrow(IllegalArgumentException::new);

        Optional<DraftDirectionOrder> latestDraftDirectionOrder =
            Optional.ofNullable(caseDetails.getCaseData().getDraftDirectionWrapper().getLatestDraftDirectionOrder());

        return latestDraftDirectionOrder.isPresent() && !latestDraftDirectionOrder.get().equals(draftDirectionOrderCollectionTail);
    }

    public void appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrders(FinremCaseDetails caseDetails) {
        FinremCaseData caseData = caseDetails.getCaseData();

        List<DraftDirectionOrderCollection> judgesAmendedDirectionOrders = Optional.ofNullable(
            caseData.getDraftDirectionWrapper().getJudgesAmendedOrderCollection()).orElse(new ArrayList<>());

        Optional<DraftDirectionOrder> latestDraftDirectionOrder =
            Optional.ofNullable(caseData.getDraftDirectionWrapper().getLatestDraftDirectionOrder());

        if (latestDraftDirectionOrder.isPresent()) {
            judgesAmendedDirectionOrders.add(DraftDirectionOrderCollection.builder()
                .value(latestDraftDirectionOrder.get())
                .build());
            caseData.getDraftDirectionWrapper().setJudgesAmendedOrderCollection(judgesAmendedDirectionOrders);
        }
    }

    public Optional<DraftDirectionOrder> draftDirectionOrderCollectionTail(FinremCaseDetails caseDetails) {
        List<DraftDirectionOrderCollection> draftDirectionOrders =
            Optional.ofNullable(caseDetails.getCaseData().getDraftDirectionWrapper().getDraftDirectionOrderCollection())
                .orElse(emptyList());

        return draftDirectionOrders.isEmpty()
            ? Optional.empty()
            : Optional.of(Iterables.getLast(draftDirectionOrders).getValue());
    }

    private Optional<DraftDirectionOrder> getJudgeApprovedHearingOrder(FinremCaseDetails caseDetails) {
        Optional<DraftDirectionOrder> draftDirectionOrderCollectionTail = draftDirectionOrderCollectionTail(caseDetails);

        return draftDirectionOrderCollectionTail.isEmpty()
            ? Optional.empty()
            : latestDraftDirectionOrderOverridesSolicitorCollection(caseDetails)
            ? Optional.ofNullable(caseDetails.getCaseData().getDraftDirectionWrapper().getLatestDraftDirectionOrder())
            : draftDirectionOrderCollectionTail;
    }

    private void appendDocumentToHearingOrderCollection(FinremCaseDetails caseDetails, Document document) {
        FinremCaseData caseData = caseDetails.getCaseData();

        List<DirectionOrderCollection> directionOrders =
            Optional.ofNullable(caseData.getUploadHearingOrder()).orElse(new ArrayList<>());

        DirectionOrder newDirectionOrder = DirectionOrder.builder().uploadDraftDocument(document).build();
        directionOrders.add(DirectionOrderCollection.builder().value(newDirectionOrder).build());

        caseData.setUploadHearingOrder(directionOrders);
    }

    private void updateCaseDataForLatestDraftHearingOrder(FinremCaseData caseData, Document stampedHearingOrder) {
        caseData.setLatestDraftHearingOrder(stampedHearingOrder);
    }

    public void updateCaseDataForLatestHearingOrderCollection(FinremCaseData caseData, Document stampedHearingOrder) {

        List<DirectionOrderCollection> finalOrderCollection = Optional.ofNullable(caseData.getFinalOrderCollection())
            .orElse(new ArrayList<>());

        finalOrderCollection.add(DirectionOrderCollection.builder()
            .value(DirectionOrder.builder()
                .uploadDraftDocument(stampedHearingOrder)
                .build())
            .build());

        caseData.setFinalOrderCollection(finalOrderCollection);
    }
}
