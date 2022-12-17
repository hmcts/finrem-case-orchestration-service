package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType;

public class ExpertEvidenceCollectionService extends PartyDocumentCollectionService {

    public ExpertEvidenceCollectionService(ManageCaseDocumentsCollectionType manageCaseDocumentsCollectionType,
                                           CaseDocumentParty party) {
        super(manageCaseDocumentsCollectionType, party);
    }

    @Override
    protected boolean canProcessDocumentType(CaseDocumentType caseDocumentType) {
        return caseDocumentType.equals(CaseDocumentType.VALUATION_REPORT)
            || caseDocumentType.equals(CaseDocumentType.EXPERT_EVIDENCE);
    }
}
