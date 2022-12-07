package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public abstract class PartyDocumentCollectionService extends DocumentCollectionService {
    private final CaseDocumentParty party;

    public PartyDocumentCollectionService(ManageCaseDocumentsCollectionType manageCaseDocumentsCollectionType,
                                          EvidenceManagementDeleteService evidenceManagementDeleteService,
                                          CaseDocumentParty party) {
        super(manageCaseDocumentsCollectionType, evidenceManagementDeleteService);
        this.party = party;
    }

    protected abstract boolean canProcessDocumentType(CaseDocumentType caseDocumentType);

    protected List<UploadCaseDocumentCollection> getDocumentForCollectionServiceType(
        List<UploadCaseDocumentCollection> eventScreenDocumentCollections) {

        return eventScreenDocumentCollections.stream()
            .filter(d -> {
                UploadCaseDocument uploadedCaseDocument = d.getUploadCaseDocument();
                return uploadedCaseDocument.getCaseDocuments() != null
                    && uploadedCaseDocument.getCaseDocumentParty() != null
                    && uploadedCaseDocument.getCaseDocumentParty().equals(party);
            })
            .filter(d -> d.getUploadCaseDocument().getCaseDocumentType() != null
                && canProcessDocumentType(d.getUploadCaseDocument().getCaseDocumentType()))
            .collect(Collectors.toList());
    }
}
