package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedConsentOrderData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.evidence.FileUploadResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementAuditService;

import java.util.List;

import static java.util.Arrays.asList;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentOrderingService {

    private final EvidenceManagementAuditService evidenceManagementAuditService;
    private final DocumentHelper documentHelper;
    private final CaseDataService caseDataService;
    private final ObjectMapper objectMapper;

    /**
     * Returns true if document A was modified later than document B, false otherwise.
     */
    public boolean isDocumentModifiedLater(CaseDocument documentA, CaseDocument documentB, String authorisationToken) {
        List<FileUploadResponse> auditResponse = evidenceManagementAuditService.audit(asList(
            documentA.getDocumentUrl(),
            documentB.getDocumentUrl()), authorisationToken);

        if (auditResponse.size() != 2) {
            throw new IllegalStateException();
        }

        return auditResponse.get(0).getModifiedOn().isAfter(
            auditResponse.get(1).getModifiedOn());
    }

    public boolean isGeneralOrderRecentThanApprovedOrder(CaseDocument generalOrder,
                                                     List<ConsentOrderCollection> orderCollection,
                                                     String authorisationToken) {

        if (orderCollection != null && !orderCollection.isEmpty() && generalOrder != null) {
            ApprovedOrder order = orderCollection.get(0).getApprovedOrder();
            CaseDocument consentOrder = order.getConsentOrder();
            List<FileUploadResponse> auditResponse = evidenceManagementAuditService.audit(asList(
                generalOrder.getDocumentUrl(),
                consentOrder.getDocumentUrl()), authorisationToken);

            if (auditResponse.size() != 2) {
                throw new IllegalStateException();
            }

            return auditResponse.get(0).getCreatedOn().isAfter(
                auditResponse.get(1).getCreatedOn());
        }
        return false;
    }

    public boolean isOrderApprovedCollectionModifiedLaterThanNotApprovedCollection(FinremCaseDetails caseDetails, String authorisationToken) {
        FinremCaseData caseData = caseDetails.getData();
        List<ContestedConsentOrderData> orderNotApprovedOrders = documentHelper.convertToContestedConsentOrderData(
            caseData.getConsentOrderWrapper().getConsentedNotApprovedOrders());
        CaseDocument latestOrderNotApproved = orderNotApprovedOrders.get(orderNotApprovedOrders.size() - 1).getConsentOrder().getConsentOrder();

        List<ConsentOrderCollection> approvedOrders = getApprovedOrderCollection(caseDetails);

        CaseDocument latestOrderApproved = approvedOrders.get(approvedOrders.size() - 1).getApprovedOrder().getConsentOrder();

        return isDocumentModifiedLater(latestOrderApproved, latestOrderNotApproved, authorisationToken);
    }

    private List<ConsentOrderCollection> getApprovedOrderCollection(FinremCaseDetails caseDetails) {
        if (caseDataService.isConsentedInContestedCase(caseDetails)) {
            return caseDetails.getData().getConsentOrderWrapper().getContestedConsentedApprovedOrders();
        } else {
            return caseDetails.getData().getApprovedOrderCollection();
        }
    }
}
