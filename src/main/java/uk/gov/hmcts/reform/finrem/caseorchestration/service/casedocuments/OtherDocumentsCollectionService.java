package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;

public class OtherDocumentsCollectionService extends PartyDocumentCollectionService {

    public OtherDocumentsCollectionService(ManageCaseDocumentsCollectionType manageCaseDocumentsCollectionType,
                                           EvidenceManagementDeleteService evidenceManagementDeleteService,
                                           CaseDocumentParty party) {
        super(manageCaseDocumentsCollectionType, evidenceManagementDeleteService, party);
    }

    @Override
    protected boolean canProcessDocumentType(CaseDocumentType caseDocumentType) {
        return caseDocumentType.equals(CaseDocumentType.OTHER)
            || caseDocumentType.equals(CaseDocumentType.FORM_B)
            || caseDocumentType.equals(CaseDocumentType.FORM_F)
            || caseDocumentType.equals(CaseDocumentType.CARE_PLAN)
            || caseDocumentType.equals(CaseDocumentType.PENSION_PLAN);
    }
}
