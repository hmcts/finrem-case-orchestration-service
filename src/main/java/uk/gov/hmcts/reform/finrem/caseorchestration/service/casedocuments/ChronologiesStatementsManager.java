package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;

public class ChronologiesStatementsManager extends PartyDocumentManager {

    public ChronologiesStatementsManager(String collectionName, String party, ObjectMapper mapper) {
        super(collectionName, party, mapper);
    }

    @Override
    protected boolean isDocumentTypeValid(CaseDocumentType caseDocumentType) {
        return caseDocumentType.equals(CaseDocumentType.STATEMENT_OF_ISSUES)
            || caseDocumentType.equals(CaseDocumentType.CHRONOLOGY)
            || caseDocumentType.equals(CaseDocumentType.FORM_G);
    }
}
