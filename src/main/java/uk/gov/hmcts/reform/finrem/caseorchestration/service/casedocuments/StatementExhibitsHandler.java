package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType;

public class StatementExhibitsHandler extends PartyDocumentHandler {

    public StatementExhibitsHandler(ManageCaseDocumentsCollectionType manageCaseDocumentsCollectionType,
                                    CaseDocumentParty party) {
        super(manageCaseDocumentsCollectionType, party);
    }

    @Override
    protected boolean canProcessDocumentType(CaseDocumentType caseDocumentType) {

        return caseDocumentType.equals(CaseDocumentType.STATEMENT_AFFIDAVIT)
            || caseDocumentType.equals(CaseDocumentType.WITNESS_STATEMENT_AFFIDAVIT);
    }
}
