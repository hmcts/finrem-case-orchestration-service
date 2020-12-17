package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.EvidenceManagementClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CollectionElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedConsentOrderData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.evidence.FileUploadResponse;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPROVED_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_CONSENT_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_CONSENT_ORDER_NOT_APPROVED_COLLECTION;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentOrderingService {

    private final EvidenceManagementClient evidenceManagementClient;
    private final DocumentHelper documentHelper;
    private final ObjectMapper objectMapper;

    /**
     * Returns true if document A was modified later than document B, false otherwise.
     */
    public boolean isDocumentModifiedLater(CaseDocument documentA, CaseDocument documentB, String authorisationToken) {
        List<FileUploadResponse> auditResponse = evidenceManagementClient.auditFileUrls(authorisationToken, asList(
            documentA.getDocumentUrl(),
            documentB.getDocumentUrl()));

        if (auditResponse.size() != 2) {
            throw new IllegalStateException();
        }

        return  auditResponse.get(0).getModifiedOn().after(
            auditResponse.get(1).getModifiedOn());
    }

    public boolean isOrderApprovedCollectionModifiedLaterThanNotApprovedCollection(CaseDetails caseDetails, String authorisationToken) {
        Map<String, Object> caseData = caseDetails.getData();
        List<ContestedConsentOrderData> orderNotApprovedOrders = documentHelper.convertToContestedConsentOrderData(
            caseData.get(CONTESTED_CONSENT_ORDER_NOT_APPROVED_COLLECTION));
        CaseDocument latestOrderNotApproved = orderNotApprovedOrders.get(orderNotApprovedOrders.size() - 1).getConsentOrder().getConsentOrder();

        List<CollectionElement<ApprovedOrder>> approvedOrders = getApprovedOrderCollection(caseDetails);
        CaseDocument latestOrderApproved = approvedOrders.get(approvedOrders.size() - 1).getValue().getConsentOrder();

        return isDocumentModifiedLater(latestOrderApproved, latestOrderNotApproved, authorisationToken);
    }

    private List<CollectionElement<ApprovedOrder>> getApprovedOrderCollection(CaseDetails caseDetails) {
        String approvedOrderCollectionFieldName = CommonFunction.isConsentedInContestedCase(caseDetails)
            ? CONTESTED_CONSENT_ORDER_COLLECTION : APPROVED_ORDER_COLLECTION;

        return objectMapper.convertValue(caseDetails.getData().get(approvedOrderCollectionFieldName), new TypeReference<>() {});
    }
}
