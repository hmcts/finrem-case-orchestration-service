package uk.gov.hmcts.reform.finrem.caseorchestration.service.sendorder;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApproveOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApproveOrdersHolder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrderConsolidateCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentInContestedApprovedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentInContestedApprovedOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UnapproveOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UnapprovedOrderCollection;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public abstract class SendOrderPartyDocumentHandler {
    private final String caseRoleCode;
    protected static final String ADDITIONAL_HEARING_FILE_NAME = "AdditionalHearingDocument.pdf";

    protected SendOrderPartyDocumentHandler(String caseRoleCode) {
        this.caseRoleCode = caseRoleCode;
    }

    public void setUpOrderDocumentsOnCase(FinremCaseDetails finremCaseDetails, List<String> partyList,
                                          List<CaseDocument> orderDocumentPack) {
        if (partyList.contains(caseRoleCode)) {
            final Long caseId = finremCaseDetails.getId();
            FinremCaseData caseData = finremCaseDetails.getData();
            log.info("Received request to send hearing pack to {} for Case ID: {}:", caseRoleCode,  caseId);
            List<ApprovedOrderCollection> orderColl = Optional.ofNullable(getOrderCollectionForParty(caseData)).orElse(new ArrayList<>());
            if (orderColl.isEmpty()) {
                addAdditionalOrderDocumentToPartyCollection(caseData, orderColl);
            }
            orderDocumentPack.forEach(document -> {
                if (shouldAddDocumentToOrderColl(document, getExistingConsolidateCollection(caseData))) {
                    orderColl.add(getApprovedOrderCollection(document));
                }
            });
            addOrdersToPartyCollection(caseData, orderColl);
        }
    }

    public void setUpConsentOrderApprovedDocumentsOnCase(FinremCaseDetails caseDetails, List<String> partyList,
                                                         List<ConsentOrderCollection> approvedConsentOrders,
                                                         List<DocumentCollection> additionalDocuments) {
        if (partyList.contains(caseRoleCode)) {
            FinremCaseData caseData = caseDetails.getData();
            List<ConsentInContestedApprovedOrderCollection> orderColl = Optional.ofNullable(getConsentOrderCollectionForParty(caseData))
                    .orElse(new ArrayList<>());
            approvedConsentOrders.forEach(order -> orderColl.add(getConsentApprovedOrderCollection(order)));
            orderColl.get(orderColl.size() - 1).getApprovedOrder().setAdditionalConsentDocuments(additionalDocuments);
            addApprovedConsentOrdersToPartyCollection(caseData, orderColl);
        }
    }

    public void setUpConsentOrderUnapprovedDocumentsOnCase(FinremCaseDetails caseDetails, List<String> partyList,
                                                           CaseDocument latestOrderDocument,
                                                           List<DocumentCollection> additionalDocuments) {
        if (partyList.contains(caseRoleCode)) {
            FinremCaseData caseData = caseDetails.getData();
            List<UnapprovedOrderCollection> orderColl = Optional.ofNullable(getUnapprovedOrderCollectionForParty(caseData))
                .orElse(new ArrayList<>());
            orderColl.add(getUnapprovedOrderCollection(latestOrderDocument, additionalDocuments));
            addUnapprovedOrdersToPartyCollection(caseData, orderColl);
        }
    }

    public void setUpCoverSheetOnCase(FinremCaseDetails caseDetails, List<String> partyList, String authToken) {
        if (partyList.contains(caseRoleCode)) {
            CaseDocument coverSheet = getPartyCoverSheet(caseDetails, authToken);
            addCoverSheetToPartyField(caseDetails, coverSheet);
        }
    }

    public void setUpOrderDocumentsOnPartiesTab(FinremCaseDetails finremCaseDetails, List<String> partyList) {
        log.info("in send order party doc handler");
        if (partyList.contains(caseRoleCode)) {
            final Long caseId = finremCaseDetails.getId();
            FinremCaseData caseData = finremCaseDetails.getData();
            log.info("Received request to set consolidate document for {} for Case ID: {}:", caseRoleCode,  caseId);
            List<ApprovedOrderCollection> orderColl = Optional.ofNullable(getOrderCollectionForParty(caseData)).orElse(new ArrayList<>());
            setConsolidateCollection(caseData, orderColl);
        }
    }

    private ApprovedOrderCollection getApprovedOrderCollection(CaseDocument generalOrder) {
        return ApprovedOrderCollection.builder()
            .value(ApproveOrder.builder().caseDocument(generalOrder)
                .orderReceivedAt(LocalDateTime.now()).build()).build();
    }

    private ConsentInContestedApprovedOrderCollection getConsentApprovedOrderCollection(ConsentOrderCollection approvedOrder) {
        return ConsentInContestedApprovedOrderCollection.builder().approvedOrder(ConsentInContestedApprovedOrder.builder()
                .consentOrder(approvedOrder.getApprovedOrder().getConsentOrder())
                .orderLetter(approvedOrder.getApprovedOrder().getOrderLetter())
                .pensionDocuments(approvedOrder.getApprovedOrder().getPensionDocuments())
                .orderReceivedAt(LocalDateTime.now()).build()
            ).build();
    }

    private UnapprovedOrderCollection getUnapprovedOrderCollection(CaseDocument consentOrder, List<DocumentCollection> additionalDocuments) {
        return UnapprovedOrderCollection.builder()
            .value(UnapproveOrder.builder().caseDocument(consentOrder).additionalConsentDocuments(additionalDocuments)
                .orderReceivedAt(LocalDateTime.now()).build()).build();
    }

    private void addAdditionalOrderDocumentToPartyCollection(FinremCaseData caseData, List<ApprovedOrderCollection> approvedOrderCollections) {
        CaseDocument additionalDocument = caseData.getAdditionalDocument();
        if (additionalDocument != null) {
            approvedOrderCollections.add(getApprovedOrderCollection(additionalDocument));
        }
    }

    protected boolean shouldAddDocumentToOrderColl(CaseDocument document,
                                                   List<ApprovedOrderConsolidateCollection> orderCollForRole) {
        List<ApprovedOrderConsolidateCollection> existingCollection = Optional.ofNullable(orderCollForRole)
                .orElse(new ArrayList<>());
        if (existingCollection.isEmpty()) {
            return true;
        }
        return existingCollection.stream().noneMatch(doc -> doc.getValue().getApproveOrders().stream().anyMatch(order ->
                order.getValue().getCaseDocument().getDocumentFilename().equals(ADDITIONAL_HEARING_FILE_NAME)
                        && order.getValue().getCaseDocument().getDocumentUrl().equals(document.getDocumentUrl())
        ));
    }

    protected ApprovedOrderConsolidateCollection getConsolidateCollection(List<ApprovedOrderCollection> orderCollection) {
        return ApprovedOrderConsolidateCollection.builder().value(ApproveOrdersHolder.builder()
                .approveOrders(orderCollection).orderReceivedAt(LocalDateTime.now()).build()).build();
    }

    protected abstract List<ApprovedOrderCollection> getOrderCollectionForParty(FinremCaseData caseData);

    protected abstract List<ConsentInContestedApprovedOrderCollection> getConsentOrderCollectionForParty(FinremCaseData caseData);

    protected abstract List<UnapprovedOrderCollection> getUnapprovedOrderCollectionForParty(FinremCaseData caseData);

    protected abstract void addApprovedConsentOrdersToPartyCollection(FinremCaseData caseData,
                                                                      List<ConsentInContestedApprovedOrderCollection> orderColl);

    protected abstract void addUnapprovedOrdersToPartyCollection(FinremCaseData caseData, List<UnapprovedOrderCollection> orderColl);

    protected abstract void addCoverSheetToPartyField(FinremCaseDetails caseDetails, CaseDocument coverSheet);

    protected abstract CaseDocument getPartyCoverSheet(FinremCaseDetails caseDetails, String authToken);

    protected abstract void addOrdersToPartyCollection(FinremCaseData caseData, List<ApprovedOrderCollection> orderColl);

    protected abstract void setConsolidateCollection(FinremCaseData caseData, List<ApprovedOrderCollection> orderColl);

    protected abstract List<ApprovedOrderConsolidateCollection> getExistingConsolidateCollection(FinremCaseData caseData);
}
