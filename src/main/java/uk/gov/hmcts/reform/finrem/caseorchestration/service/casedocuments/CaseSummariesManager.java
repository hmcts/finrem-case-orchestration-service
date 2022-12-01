package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;

public class CaseSummariesManager extends PartyDocumentManager {

    public CaseSummariesManager(String collectionName, String party, ObjectMapper mapper) {
        super(collectionName, party, mapper);
    }

    @Override
    protected boolean isDocumentTypeValid(CaseDocumentType caseDocumentType) {
        return caseDocumentType.equals(CaseDocumentType.POSITION_STATEMENT)
            || caseDocumentType.equals(CaseDocumentType.SKELETON_ARGUMENT)
            || caseDocumentType.equals(CaseDocumentType.CASE_SUMMARY);
    }
}
