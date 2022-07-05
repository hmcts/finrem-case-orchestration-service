package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.StatementExhibitsHandler;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.UploadCaseDocumentCollection;

import java.util.List;

@Component
public class ApplicantStatementsExhibitsHandler extends StatementExhibitsHandler {

    @Autowired
    public ApplicantStatementsExhibitsHandler() {
        super(APPLICANT);
    }

    @Override
    protected List<UploadCaseDocumentCollection> getDocumentCollection(FinremCaseData caseData) {
        return caseData.getUploadCaseDocumentWrapper().getAppStatementsExhibitsCollection();
    }

    @Override
    protected void setDocumentCollection(FinremCaseData caseData, List<UploadCaseDocumentCollection> docs) {
        caseData.getUploadCaseDocumentWrapper().setAppStatementsExhibitsCollection(docs);
    }
}
