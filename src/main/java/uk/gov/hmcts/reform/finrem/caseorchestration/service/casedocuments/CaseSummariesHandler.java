package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import com.fasterxml.jackson.databind.ObjectMapper;

public class CaseSummariesHandler extends PartyDocumentHandler {

    public CaseSummariesHandler(String collectionName, String party, ObjectMapper mapper) {
        super(collectionName, party, mapper);
    }

    @Override
    protected boolean isDocumentTypeValid(String caseDocumentType) {
        return caseDocumentType.equals("Position Statement")
            || caseDocumentType.equals("Skeleton Argument")
            || caseDocumentType.equals("Case Summary");
    }
}
