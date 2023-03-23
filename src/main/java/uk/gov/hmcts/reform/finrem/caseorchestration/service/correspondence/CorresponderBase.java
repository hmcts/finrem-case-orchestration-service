package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence;

public abstract class CorresponderBase<D> {

    protected abstract boolean shouldSendApplicantSolicitorEmail(D caseDetails);

    protected abstract boolean shouldSendRespondentSolicitorEmail(D caseDetails);
}
