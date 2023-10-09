package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;

import java.util.List;

public abstract class MultiLetterOrEmailAllPartiesCorresponder<D> extends EmailAndLettersCorresponderBase<D> {

    @Override
    public void sendCorrespondence(D caseDetails, String authorisationToken) {
        sendApplicantCorrespondence(authorisationToken, caseDetails);
        sendRespondentCorrespondence(authorisationToken, caseDetails);
        sendIntervenerCorrespondence(authorisationToken, caseDetails);
    }

    protected abstract void sendApplicantCorrespondence(String authorisationToken, D caseDetails);

    protected abstract void sendRespondentCorrespondence(String authorisationToken, D caseDetails);

    protected abstract void sendIntervenerCorrespondence(String authorisationToken, D caseDetails);

    protected abstract void emailApplicantSolicitor(D caseDetails);

    protected abstract void emailRespondentSolicitor(D caseDetails);

    protected abstract void emailIntervenerSolicitor(IntervenerWrapper intervenerWrapper, D caseDetails);

    public abstract List<CaseDocument> getCaseDocuments(D caseDetails);
}
