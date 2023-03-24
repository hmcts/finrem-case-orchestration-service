package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;

import java.util.List;

public abstract class MultiLetterOrEmailAllPartiesCorresponder<D> extends EmailAndLettersCorresponderBase<D> {

    public void sendCorrespondence(D caseDetails, String authorisationToken) {
        sendApplicantCorrespondence(authorisationToken, caseDetails);
        sendRespondentCorrespondence(authorisationToken, caseDetails);
    }

    protected abstract void sendApplicantCorrespondence(String authorisationToken, D caseDetails);

    protected abstract void sendRespondentCorrespondence(String authorisationToken, D caseDetails);

    protected abstract void emailApplicantSolicitor(D caseDetails);

    protected abstract void emailRespondentSolicitor(D caseDetails);

    public abstract List<BulkPrintDocument> getDocumentsToPrint(D caseDetails);
}
