package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.ExpertEvidenceHandler;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.UploadCaseDocumentCollection;

import java.util.List;

@Component
public class ApplicantExpertEvidenceHandler extends ExpertEvidenceHandler {

    @Autowired
    public ApplicantExpertEvidenceHandler() {
        super(APPLICANT);
    }

    @Override
    protected List<UploadCaseDocumentCollection> getDocumentCollection(FinremCaseData caseData) {
        return caseData.getUploadCaseDocumentWrapper().getAppExpertEvidenceCollection();
    }

    @Override
    protected void setDocumentCollection(FinremCaseData caseData, List<UploadCaseDocumentCollection> docs) {
        caseData.getUploadCaseDocumentWrapper().setAppExpertEvidenceCollection(docs);
    }
}
