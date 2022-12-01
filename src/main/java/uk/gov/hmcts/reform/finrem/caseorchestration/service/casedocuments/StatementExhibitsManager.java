package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import com.fasterxml.jackson.databind.ObjectMapper;

public class StatementExhibitsManager extends PartyDocumentManager {

    public StatementExhibitsManager(String collectionName, String party, ObjectMapper mapper) {
        super(collectionName, party, mapper);
    }

    @Override
    protected boolean isDocumentTypeValid(String caseDocumentType) {
        return caseDocumentType.equals("Statement/Affidavit")
            || caseDocumentType.equals("Witness Statement/Affidavit");
    }
}
