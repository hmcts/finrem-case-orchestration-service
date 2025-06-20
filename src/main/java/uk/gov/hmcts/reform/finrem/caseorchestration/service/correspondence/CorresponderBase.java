package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;

public abstract class CorresponderBase<D> {

    protected abstract boolean shouldSendApplicantSolicitorEmail(D caseDetails);

    protected abstract boolean shouldSendRespondentSolicitorEmail(D caseDetails);

    protected final ContactDetailsWrapper getContactDetailsWrapper(FinremCaseDetails caseDetails) {
        return caseDetails.getData().getContactDetailsWrapper();
    }

    protected final boolean isNotInternationalParty(YesOrNo resideOutsideUK) {
        return !YesOrNo.YES.equals(resideOutsideUK);
    }
}
