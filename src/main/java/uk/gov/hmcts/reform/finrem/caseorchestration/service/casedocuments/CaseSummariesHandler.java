package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import uk.gov.hmcts.reform.finrem.ccd.domain.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.UploadCaseDocumentCollection;

import java.util.List;

public class CaseSummariesHandler extends PartyDocumentHandler {

    public CaseSummariesHandler(String party) {
        super(party);
    }

    @Override
    protected boolean isDocumentTypeValid(String caseDocumentType) {
        return caseDocumentType.equals("Position Statement")
            || caseDocumentType.equals("Skeleton Argument")
            || caseDocumentType.equals("Case Summary");
    }

    @Override
    protected boolean isDocumentTypeValid(CaseDocumentType caseDocumentType) {
        return CaseDocumentType.POSITION_STATEMENT.equals(caseDocumentType)
            || CaseDocumentType.SKELETON_ARGUMENT.equals(caseDocumentType)
            || CaseDocumentType.CASE_SUMMARY.equals(caseDocumentType);
    }

    @Override
    protected List<UploadCaseDocumentCollection> getDocumentCollection(FinremCaseData caseData) {
        return null;
    }

    @Override
    protected void setDocumentCollection(FinremCaseData caseData, List<UploadCaseDocumentCollection> docs) {

    }
}
