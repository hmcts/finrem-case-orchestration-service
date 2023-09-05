package uk.gov.hmcts.reform.finrem.caseorchestration.service.sendorder;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApproveOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
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

    protected SendOrderPartyDocumentHandler(String caseRoleCode) {
        this.caseRoleCode = caseRoleCode;
    }

    public void setUpOrderDocumentsOnCase(FinremCaseDetails finremCaseDetails, List<String> partyList,
                                          List<CaseDocument> orderDocumentPack) {
        if (partyList.contains(caseRoleCode)) {
            final Long caseId = finremCaseDetails.getId();
            FinremCaseData caseData = finremCaseDetails.getData();
            log.info("Received request to send hearing pack to {} for case {}:", caseRoleCode,  caseId);
            List<ApprovedOrderCollection> orderColl = Optional.ofNullable(getOrderCollectionForParty(caseData))
                .orElse(new ArrayList<>());
            orderDocumentPack.forEach(document -> orderColl.add(getApprovedOrderCollection(document)));
            addAdditionalOrderDocumentToPartyCollection(caseData, orderColl);
            addOrdersToPartyCollection(caseData, orderColl);
        }
    }

    public void setUpConsentOrderApprovedDocumentsOnCase(FinremCaseDetails caseDetails, List<String> partyList,
                                                         List<CaseDocument> approvedOrderDocumentPack) {
        if (partyList.contains(caseRoleCode)) {
            FinremCaseData caseData = caseDetails.getData();
            List<ApprovedOrderCollection> orderColl = Optional.ofNullable(getOrderCollectionForParty(caseData))
                    .orElse(new ArrayList<>());
            approvedOrderDocumentPack.forEach(document -> orderColl.add(getApprovedOrderCollection(document)));
            addOrdersToPartyCollection(caseData, orderColl);
        }
    }

    public void setUpConsentOrderUnapprovedDocumentsOnCase(FinremCaseDetails caseDetails, List<String> partyList,
                                                           List<CaseDocument> approvedOrderDocumentPack) {
        if (partyList.contains(caseRoleCode)) {
            FinremCaseData caseData = caseDetails.getData();
            List<UnapprovedOrderCollection> orderColl = Optional.ofNullable(getUnapprovedOrderCollectionForParty(caseData))
                .orElse(new ArrayList<>());
            approvedOrderDocumentPack.forEach(document -> orderColl.add(getUnapprovedOrderCollection(document)));
            addUnapprovedOrdersToPartyCollection(caseData, orderColl);
        }
    }

    public void setUpCoverSheetOnCase(FinremCaseDetails caseDetails, List<String> partyList, String authToken) {
        if (partyList.contains(caseRoleCode)) {
            CaseDocument coverSheet = getPartyCoverSheet(caseDetails, authToken);
            addCoverSheetToPartyField(caseDetails, coverSheet);
        }
    }

    private ApprovedOrderCollection getApprovedOrderCollection(CaseDocument generalOrder) {
        return ApprovedOrderCollection.builder()
            .value(ApproveOrder.builder().caseDocument(generalOrder)
                .orderReceivedAt(LocalDateTime.now()).build()).build();
    }

    private UnapprovedOrderCollection getUnapprovedOrderCollection(CaseDocument consentOrder) {
        return UnapprovedOrderCollection.builder()
            .value(UnapproveOrder.builder().caseDocument(consentOrder)
                .orderReceivedAt(LocalDateTime.now()).build()).build();
    }

    private void addAdditionalOrderDocumentToPartyCollection(FinremCaseData caseData, List<ApprovedOrderCollection> approvedOrderCollections) {
        CaseDocument additionalDocument = caseData.getAdditionalDocument();
        if (additionalDocument != null) {
            approvedOrderCollections.add(getApprovedOrderCollection(additionalDocument));
        }
    }

    protected abstract List<ApprovedOrderCollection> getOrderCollectionForParty(FinremCaseData caseData);

    protected abstract List<UnapprovedOrderCollection> getUnapprovedOrderCollectionForParty(FinremCaseData caseData);

    protected abstract void addOrdersToPartyCollection(FinremCaseData caseData, List<ApprovedOrderCollection> orderColl);

    protected abstract void addUnapprovedOrdersToPartyCollection(FinremCaseData caseData, List<UnapprovedOrderCollection> orderColl);

    protected abstract void addCoverSheetToPartyField(FinremCaseDetails caseDetails, CaseDocument coverSheet);

    protected abstract CaseDocument getPartyCoverSheet(FinremCaseDetails caseDetails, String authToken);

}
