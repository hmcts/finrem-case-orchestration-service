package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.evidence.FileUploadResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementAuditService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderDateService {
    private final EvidenceManagementAuditService evidenceManagementAuditService;

    public List<DirectionOrderCollection> addCreatedDateInFinalOrder(List<DirectionOrderCollection> orderCollections,
                                                                     String authorisationToken) {
        List<DirectionOrderCollection> directionOrderCollections = Optional.ofNullable(orderCollections)
            .orElse(new ArrayList<>());
        return addCreatedDateInOrder(directionOrderCollections, authorisationToken, YesOrNo.YES);
    }

    public List<DirectionOrderCollection> addCreatedDateInUploadedOrder(List<DirectionOrderCollection> orderCollections,
                                                                        String authorisationToken) {
        List<DirectionOrderCollection> directionOrderCollections = Optional.ofNullable(orderCollections)
            .orElse(new ArrayList<>());
        return addCreatedDateInOrder(directionOrderCollections, authorisationToken, YesOrNo.NO);
    }

    private List<DirectionOrderCollection> addCreatedDateInOrder(List<DirectionOrderCollection> orderCollections,
                                                                 String authorisationToken,
                                                                 YesOrNo isStamped) {
        List<DirectionOrderCollection> returnCollection = new ArrayList<>();
        if (!orderCollections.isEmpty()) {
            List<String> documentUrls = new ArrayList<>();
            orderCollections.forEach(order -> documentUrls.add(order.getValue().getUploadDraftDocument().getDocumentUrl()));
            List<FileUploadResponse> auditResponse = evidenceManagementAuditService.audit(documentUrls, authorisationToken);
            orderCollections.forEach(order -> addCreatedDateAndUpdateStampedInformation(isStamped, returnCollection, auditResponse, order));
        }
        return returnCollection;
    }

    private void addCreatedDateAndUpdateStampedInformation(YesOrNo isStamped,
                                                           List<DirectionOrderCollection> returnCollection,
                                                           List<FileUploadResponse> auditResponse,
                                                           DirectionOrderCollection order) {
        if (isOrderNotStamped(order)) {
            String filename = order.getValue().getUploadDraftDocument().getDocumentFilename();
            for (FileUploadResponse fileUploadResponse : auditResponse) {
                if (filename.equals(fileUploadResponse.getFileName())) {
                    LocalDateTime createdOn = fileUploadResponse.getCreatedOn() != null ? fileUploadResponse.getCreatedOn() : LocalDateTime.now();
                    log.info("auditResponse filename {} original create on {} if null created on {}",
                        fileUploadResponse.getFileName(), fileUploadResponse.getCreatedOn(), createdOn);
                    returnCollection.add(getDirectionOrderCollectionObj(order.getValue(), order.getValue().getUploadDraftDocument(), createdOn,
                        isStamped));
                    break;
                }
            }
        } else {
            returnCollection.add(order);
        }
    }

    private DirectionOrderCollection getDirectionOrderCollectionObj(DirectionOrder directionOrder, CaseDocument caseDocument,
                                                                    LocalDateTime createdOn,
                                                                    YesOrNo isStamped) {
        return DirectionOrderCollection.builder().value(directionOrder.toBuilder()
            .uploadDraftDocument(caseDocument)
            .orderDateTime(createdOn)
            .isOrderStamped(isStamped)
            .build()).build();
    }

    private boolean isOrderNotStamped(DirectionOrderCollection order) {
        return !YesOrNo.YES.equals(order.getValue().getIsOrderStamped());
    }

}

