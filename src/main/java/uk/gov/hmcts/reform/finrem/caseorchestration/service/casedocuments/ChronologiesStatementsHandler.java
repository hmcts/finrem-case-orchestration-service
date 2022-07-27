package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import uk.gov.hmcts.reform.finrem.ccd.domain.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.UploadCaseDocumentCollection;

import java.util.List;

public class ChronologiesStatementsHandler extends PartyDocumentHandler {

    public ChronologiesStatementsHandler(String party) {
        super(party);
    }

    @Override
    protected boolean isDocumentTypeValid(String caseDocumentType) {
        return caseDocumentType.equals("Statement of Issues")
            || caseDocumentType.equals("Chronology")
            || caseDocumentType.equals("Form G");
    }

    @Override
    protected boolean isDocumentTypeValid(CaseDocumentType caseDocumentType) {
        return CaseDocumentType.STATEMENT_OF_ISSUES.equals(caseDocumentType)
            || CaseDocumentType.CHRONOLOGY.equals(caseDocumentType)
            || CaseDocumentType.FORM_G.equals(caseDocumentType);
    }

    @Override
    protected List<UploadCaseDocumentCollection> getDocumentCollection(FinremCaseData caseData) {
        return null;
    }

    @Override
    protected void setDocumentCollection(FinremCaseData caseData, List<UploadCaseDocumentCollection> docs) {

    }
}
