package uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient;

public record Representation(
    String userId,
    boolean isRepresentingApplicant,
    boolean isRepresentingRespondent,
    Integer intervenerIndex,          // null if not an intervener
    IntervenerRole intervenerRole     // null if not an intervener
) {

    public boolean isRepresentingAnyInterveners() {
        return intervenerIndex != null;
    }

    public boolean isRepresentingAnyIntervenerBarristers() {
        return intervenerRole == IntervenerRole.BARRISTER;
    }
}
