package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import com.fasterxml.jackson.databind.ObjectMapper;

public class CorrespondenceManager extends PartyDocumentManager {

    public CorrespondenceManager(String collectionName, String party, ObjectMapper mapper) {
        super(collectionName, party, mapper);
    }

    @Override
    protected boolean isDocumentTypeValid(String caseDocumentType) {
        return caseDocumentType.equals("Offers")
            || caseDocumentType.equals("Letter from Applicant");
    }
}
