package uk.gov.hmcts.reform.finrem.caseorchestration.service.sendorder;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApproveOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApproveOrdersHolder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrderConsolidateCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public abstract class SendOrderPartyDocumentHandler {
    private final String caseRoleCode;

    protected SendOrderPartyDocumentHandler(String caseRoleCode) {
        this.caseRoleCode = caseRoleCode;
    }

    public void setUpOrderDocumentsOnCase(FinremCaseDetails finremCaseDetails, List<String> partyList,
                                          List<CaseDocument> orderDocumentPack) {
        if (partyList.contains(caseRoleCode)) {
            final Long caseId = finremCaseDetails.getId();
            FinremCaseData caseData = finremCaseDetails.getData();
            log.info("Received request to send hearing pack to {} for case {}:", caseRoleCode,  caseId);
            List<ApprovedOrderCollection> orderColl = Optional.ofNullable(getOrderCollectionForParty(caseData)).orElse(new ArrayList<>());
            if (orderColl.isEmpty()) {
                addAdditionalOrderDocumentToPartyCollection(caseData, orderColl);
            }
            orderDocumentPack.forEach(document -> orderColl.add(getApprovedOrderCollection(document)));
            addOrdersToPartyCollection(caseData, orderColl);
        }
    }

    public void setUpOrderDocumentsOnPartiesTab(FinremCaseDetails finremCaseDetails, List<String> partyList) {
        if (partyList.contains(caseRoleCode)) {
            final Long caseId = finremCaseDetails.getId();
            FinremCaseData caseData = finremCaseDetails.getData();
            log.info("Received request to set consolidate document for {} for case {}:", caseRoleCode,  caseId);
            List<ApprovedOrderCollection> orderColl = Optional.ofNullable(getOrderCollectionForParty(caseData)).orElse(new ArrayList<>());
            setConsolidateCollection(caseData, orderColl);
        }
    }

    private ApprovedOrderCollection getApprovedOrderCollection(CaseDocument generalOrder) {
        return ApprovedOrderCollection.builder()
            .value(ApproveOrder.builder().caseDocument(generalOrder)
                .orderReceivedAt(LocalDateTime.now()).build()).build();
    }

    private void addAdditionalOrderDocumentToPartyCollection(FinremCaseData caseData, List<ApprovedOrderCollection> approvedOrderCollections) {
        CaseDocument additionalDocument = caseData.getAdditionalDocument();
        if (additionalDocument != null) {
            approvedOrderCollections.add(getApprovedOrderCollection(additionalDocument));
        }
    }

    protected abstract List<ApprovedOrderCollection> getOrderCollectionForParty(FinremCaseData caseData);

    protected abstract void addOrdersToPartyCollection(FinremCaseData caseData, List<ApprovedOrderCollection> orderColl);

    protected abstract void setConsolidateCollection(FinremCaseData caseData, List<ApprovedOrderCollection> orderColl);

    protected ApprovedOrderConsolidateCollection getConsolidateCollection(List<ApprovedOrderCollection> orderCollection) {
        return ApprovedOrderConsolidateCollection.builder().value(ApproveOrdersHolder.builder()
            .approveOrders(orderCollection).orderReceivedAt(LocalDateTime.now()).build()).build();
    }
}
