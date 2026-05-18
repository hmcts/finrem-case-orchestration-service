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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UnapproveOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UnapprovedOrderCollection;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

            //This gets the party's current document collection e.g. appOrderCollection
            List<ApprovedOrderCollection> orderColl = Optional.ofNullable(getOrderCollectionForParty(caseData)).orElse(new ArrayList<>());

            orderDocumentPack.forEach(document -> {
                if (!documentAlreadyExistsInCurrentOrderCollection(document, orderColl)
                    && !additionalHearingDocumentAlreadyExists(document, getExistingConsolidateCollection(caseData))) {
                    orderColl.add(getApprovedOrderCollection(document));
                }
            });

            //Adds to each party collection e.g. applicant, respondent, intervener etc
            addOrdersToPartyCollection(caseData, orderColl);
        }
    }

    public void setUpConsentOrderApprovedDocumentsOnCase(FinremCaseDetails caseDetails, List<String> partyList,
                                                         List<ConsentOrderCollection> approvedConsentOrders,
                                                         List<DocumentCollectionItem> additionalDocuments) {
        if (partyList.contains(caseRoleCode)) {
            FinremCaseData caseData = caseDetails.getData();
            List<ConsentInContestedApprovedOrderCollection> orderColl = Optional.ofNullable(getConsentOrderCollectionForParty(caseData))
                    .orElse(new ArrayList<>());
            approvedConsentOrders.forEach(order -> orderColl.add(getConsentApprovedOrderCollection(order)));
            orderColl.getLast().getApprovedOrder().setAdditionalConsentDocuments(additionalDocuments);
            addApprovedConsentOrdersToPartyCollection(caseData, orderColl);
        }
    }

    public void setUpConsentOrderUnapprovedDocumentsOnCase(FinremCaseDetails caseDetails, List<String> partyList,
                                                           CaseDocument latestOrderDocument,
                                                           List<DocumentCollectionItem> additionalDocuments) {
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

    private UnapprovedOrderCollection getUnapprovedOrderCollection(CaseDocument consentOrder, List<DocumentCollectionItem> additionalDocuments) {
        return UnapprovedOrderCollection.builder()
            .value(UnapproveOrder.builder().caseDocument(consentOrder).additionalConsentDocuments(additionalDocuments)
                .orderReceivedAt(LocalDateTime.now()).build()).build();
    }

    private boolean documentAlreadyExistsInCurrentOrderCollection(CaseDocument document, List<ApprovedOrderCollection> orderCollection) {
        if (document == null) {
            return false;
        }

        return Optional.ofNullable(orderCollection).orElse(List.of()).stream()
            .map(ApprovedOrderCollection::getValue)
            .filter(Objects::nonNull)
            .map(ApproveOrder::getCaseDocument)
            .filter(Objects::nonNull)
            .map(CaseDocument::getDocumentUrl)
            .anyMatch(existingUrl -> Objects.equals(existingUrl, document.getDocumentUrl()));
    }

    private boolean additionalHearingDocumentAlreadyExists(
        CaseDocument document,
        List<ApprovedOrderConsolidateCollection> existingConsolidatedCollection
    ) {
        if (!isAdditionalHearingDocument(document)) {
            return false;
        }

        return Optional.ofNullable(existingConsolidatedCollection)
            .orElse(List.of())
            .stream()
            .map(ApprovedOrderConsolidateCollection::getValue)
            .filter(Objects::nonNull)
            .map(ApproveOrdersHolder::getApproveOrders)
            .filter(Objects::nonNull)
            .flatMap(List::stream)
            .map(ApprovedOrderCollection::getValue)
            .filter(Objects::nonNull)
            .map(ApproveOrder::getCaseDocument)
            .filter(Objects::nonNull)
            .anyMatch(existingDocument ->
                Objects.equals(existingDocument.getDocumentUrl(), document.getDocumentUrl())
            );
    }

    private boolean isAdditionalHearingDocument(CaseDocument document) {
        return document != null
            && ADDITIONAL_HEARING_FILE_NAME.equals(document.getDocumentFilename());
    }

    protected List<ApprovedOrderConsolidateCollection> getPartyConsolidateCollection(List<ApprovedOrderConsolidateCollection> list) {
        List<ApprovedOrderConsolidateCollection> approvedOrderConsolidateCollections = Optional.ofNullable(list).orElse(new ArrayList<>());
        List<ApprovedOrderConsolidateCollection> returnList = new ArrayList<>();

        approvedOrderConsolidateCollections.forEach(orders -> {
            ApproveOrdersHolder value = orders.getValue();
            List<ApprovedOrderCollection> approveOrders = value.getApproveOrders();
            List<ApprovedOrderCollection> orderCollections = new ArrayList<>();
            if (approveOrders != null) {
                orderCollections
                    = approveOrders.stream().filter(doc -> doc.getValue().getCaseDocument() != null).toList();
            }

            if (!orderCollections.isEmpty()) {
                returnList.add(getConsolidateCollection(value, orderCollections));
            }
        });
        return returnList;
    }

    private ApprovedOrderConsolidateCollection getConsolidateCollection(ApproveOrdersHolder value,
                                                                          List<ApprovedOrderCollection> orderCollection) {
        return ApprovedOrderConsolidateCollection.builder().value(ApproveOrdersHolder.builder()
            .approveOrders(orderCollection).orderReceivedAt(value.getOrderReceivedAt()).build()).build();
    }

    protected ApprovedOrderConsolidateCollection getConsolidateCollection(
        List<ApprovedOrderCollection> orderCollection,
        List<DocumentCollectionItem> supportingDocuments
    ) {
        return ApprovedOrderConsolidateCollection.builder()
            .value(ApproveOrdersHolder.builder()
                .approveOrders(orderCollection)
                .supportingDocuments(supportingDocuments)
                .orderReceivedAt(LocalDateTime.now())
                .build())
            .build();
    }

    protected List<DocumentCollectionItem> getCategorisedSupportingDocuments(FinremCaseData caseData) {
        return Optional.ofNullable(caseData.getSendOrderWrapper().getAdditionalDocuments())
            .orElse(List.of())
            .stream()
            .filter(Objects::nonNull)
            .map(document -> DocumentCollectionItem.builder()
                .value(copyDocumentWithCategory(document.getValue(), getSupportingDocumentsCategoryId()))
                .build())
            .toList();
    }

    private CaseDocument copyDocumentWithCategory(CaseDocument document, String categoryId) {
        return CaseDocument.builder()
            .documentUrl(document.getDocumentUrl())
            .documentFilename(document.getDocumentFilename())
            .documentBinaryUrl(document.getDocumentBinaryUrl())
            .categoryId(categoryId)
            .build();
    }

    protected abstract String getSupportingDocumentsCategoryId();

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
