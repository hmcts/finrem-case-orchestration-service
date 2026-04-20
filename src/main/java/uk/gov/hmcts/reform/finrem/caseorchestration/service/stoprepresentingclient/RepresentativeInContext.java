package uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;

public record RepresentativeInContext(
    String userId,
    boolean isApplicantRepresentative,
    boolean isRespondentRepresentative,
    IntervenerType intervenerType,          // null if not an intervener
    IntervenerRole intervenerRole     // null if not an intervener
) {

    public boolean isIntervenerRepresentative() {
        return intervenerType != null;
    }

    public boolean isIntervenerBarrister() {
        return intervenerRole == IntervenerRole.BARRISTER;
    }
}
