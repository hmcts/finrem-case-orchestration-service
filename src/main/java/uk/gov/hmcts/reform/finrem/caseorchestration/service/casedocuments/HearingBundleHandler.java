package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import uk.gov.hmcts.reform.finrem.ccd.domain.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.UploadCaseDocumentCollection;

import java.util.List;

public class HearingBundleHandler extends PartyDocumentHandler {

    public HearingBundleHandler(String party) {
        super(party);
    }

    @Override
    protected boolean isDocumentTypeValid(String caseDocumentType) {
        return caseDocumentType.equals("Trial Bundle");
    }

    @Override
    protected boolean isDocumentTypeValid(CaseDocumentType caseDocumentType) {
        return caseDocumentType.equals(CaseDocumentType.TRIAL_BUNDLE);
    }

    @Override
    protected List<UploadCaseDocumentCollection> getDocumentCollection(FinremCaseData caseData) {
        return null;
    }

    @Override
    protected void setDocumentCollection(FinremCaseData caseData, List<UploadCaseDocumentCollection> docs) {

    }
}
