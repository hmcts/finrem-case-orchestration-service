package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType;

public class ChronologiesStatementsCollectionService extends PartyDocumentCollectionService {

    public ChronologiesStatementsCollectionService(ManageCaseDocumentsCollectionType manageCaseDocumentsCollectionType,
                                                   CaseDocumentParty party) {
        super(manageCaseDocumentsCollectionType, party);
    }

    @Override
    protected boolean canProcessDocumentType(CaseDocumentType caseDocumentType) {
        return caseDocumentType.equals(CaseDocumentType.STATEMENT_OF_ISSUES)
            || caseDocumentType.equals(CaseDocumentType.CHRONOLOGY)
            || caseDocumentType.equals(CaseDocumentType.FORM_G);
    }
}
