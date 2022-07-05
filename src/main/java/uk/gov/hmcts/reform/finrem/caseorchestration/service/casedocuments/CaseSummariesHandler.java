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
        return caseDocumentType.equals(CaseDocumentType.POSITION_STATEMENT)
            || caseDocumentType.equals(CaseDocumentType.SKELETON_ARGUMENT)
            || caseDocumentType.equals(CaseDocumentType.CASE_SUMMARY);
    }

    @Override
    protected List<UploadCaseDocumentCollection> getDocumentCollection(FinremCaseData caseData) {
        return null;
    }

    @Override
    protected void setDocumentCollection(FinremCaseData caseData, List<UploadCaseDocumentCollection> docs) {

    }
}
