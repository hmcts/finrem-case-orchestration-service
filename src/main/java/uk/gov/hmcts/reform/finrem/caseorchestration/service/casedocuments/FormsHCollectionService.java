package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;

public class FormsHCollectionService extends PartyDocumentCollectionService {

    public FormsHCollectionService(ManageCaseDocumentsCollectionType manageCaseDocumentsCollectionType,
                                   EvidenceManagementDeleteService evidenceManagementDeleteService,
                                   CaseDocumentParty party) {
        super(manageCaseDocumentsCollectionType, evidenceManagementDeleteService, party);
    }

    @Override
    protected boolean canProcessDocumentType(CaseDocumentType caseDocumentType) {
        return caseDocumentType.equals(CaseDocumentType.FORM_H);
    }
}
