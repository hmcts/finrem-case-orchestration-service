package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import com.fasterxml.jackson.databind.ObjectMapper;

public class OtherDocumentsManager extends PartyDocumentManager {

    public OtherDocumentsManager(String collectionName, String party, ObjectMapper mapper) {
        super(collectionName, party, mapper);
    }

    @Override
    protected boolean isDocumentTypeValid(String caseDocumentType) {
        return caseDocumentType.equals("other")
            || caseDocumentType.equals("Form B")
            || caseDocumentType.equals("Form F")
            || caseDocumentType.equals("Care Plan")
            || caseDocumentType.equals("Pension Plan");
    }
}
