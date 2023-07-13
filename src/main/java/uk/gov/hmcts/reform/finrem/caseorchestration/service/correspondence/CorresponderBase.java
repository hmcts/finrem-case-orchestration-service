package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;

public abstract class CorresponderBase<D> {

    protected abstract boolean shouldSendApplicantSolicitorEmail(D caseDetails);

    protected abstract boolean shouldSendRespondentSolicitorEmail(D caseDetails);

    protected abstract boolean shouldSendIntervenerSolicitorEmail(IntervenerWrapper intervenerWrapper, D caseDetails);

}
