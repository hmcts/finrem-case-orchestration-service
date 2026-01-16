package uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient;

public record RepresentativeInContext(
    String userId,
    boolean isApplicationRepresentative,
    boolean isRespondentRepresentative,
    Integer intervenerIndex,          // null if not an intervener
    IntervenerRole intervenerRole     // null if not an intervener
) {

    public boolean isIntervenerRepresentative() {
        return intervenerIndex != null;
    }

    public boolean isIntervenerBarrister() {
        return intervenerRole == IntervenerRole.BARRISTER;
    }
}
