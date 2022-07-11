package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.EvidenceManagementClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.evidence.FileUploadResponse;
import uk.gov.hmcts.reform.finrem.ccd.domain.ConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;

import java.util.List;

import static java.util.Arrays.asList;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentOrderingService {

    private final EvidenceManagementClient evidenceManagementClient;

    /**
     * Returns true if document A was modified later than document B, false otherwise.
     */
    public boolean isDocumentModifiedLater(Document documentA, Document documentB, String authorisationToken) {
        List<FileUploadResponse> auditResponse = evidenceManagementClient.auditFileUrls(authorisationToken, asList(
            documentA.getUrl(),
            documentB.getUrl()));

        if (auditResponse.size() != 2) {
            throw new IllegalStateException();
        }

        return auditResponse.get(0).getModifiedOn().after(
            auditResponse.get(1).getModifiedOn());
    }

    public boolean isOrderApprovedCollectionModifiedLaterThanNotApprovedCollection(FinremCaseDetails caseDetails, String authorisationToken) {
        FinremCaseData caseData = caseDetails.getCaseData();
        List<ConsentOrderCollection> orderNotApprovedOrders = caseData.getConsentOrderWrapper().getConsentedNotApprovedOrders();
        Document latestOrderNotApproved = orderNotApprovedOrders.get(orderNotApprovedOrders.size() - 1).getValue().getConsentOrder();

        List<ConsentOrderCollection> approvedOrders = getApprovedOrders(caseDetails.getCaseData());
        Document latestOrderApproved = approvedOrders.get(approvedOrders.size() - 1).getValue().getConsentOrder();

        return isDocumentModifiedLater(latestOrderApproved, latestOrderNotApproved, authorisationToken);
    }

    private List<ConsentOrderCollection> getApprovedOrders(FinremCaseData caseData) {
        return caseData.isConsentedInContestedCase()
            ? caseData.getContestedConsentedApprovedOrders()
            : caseData.getApprovedOrderCollection();
    }
}
