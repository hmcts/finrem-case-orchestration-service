package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ExpertEvidenceHandler extends PartyDocumentHandler {

    public ExpertEvidenceHandler(String collectionName, String party, ObjectMapper mapper) {
        super(collectionName, party, mapper);
    }

    @Override
    protected boolean isDocumentTypeValid(String caseDocumentType) {
        return caseDocumentType.equals("Valuation Report")
            || caseDocumentType.equals("Expert Evidence");
    }
}
