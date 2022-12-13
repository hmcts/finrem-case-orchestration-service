package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Order(2)
public class FdrDocumentsCollectionService extends DocumentCollectionService {


    public FdrDocumentsCollectionService(EvidenceManagementDeleteService evidenceManagementDeleteService) {
        super(ManageCaseDocumentsCollectionType.CONTESTED_FDR_CASE_DOCUMENT_COLLECTION,
            evidenceManagementDeleteService);
    }

    protected List<UploadCaseDocumentCollection> getServiceCollectionType(
        List<UploadCaseDocumentCollection> eventScreenDocumentCollections) {

        return eventScreenDocumentCollections.stream()
            .filter(d -> {
                UploadCaseDocument uploadedCaseDocument = d.getUploadCaseDocument();
                return uploadedCaseDocument.getCaseDocuments() != null
                    && uploadedCaseDocument.getCaseDocumentType() != null
                    && uploadedCaseDocument.getCaseDocumentFdr() != null
                    && uploadedCaseDocument.getCaseDocumentFdr().equals(YesOrNo.YES);
            }).collect(Collectors.toList());
    }
}
