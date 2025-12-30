package uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient;

public record Representation(
    String userId,
    boolean isRepresentingApplicant, boolean isRepresentingRespondent,
    boolean isRepresentingIntervenerOneSolicitor, boolean isRepresentingIntervenerOneBarrister,
    boolean isRepresentingIntervenerTwoSolicitor, boolean isRepresentingIntervenerTwoBarrister,
    boolean isRepresentingIntervenerThreeSolicitor, boolean isRepresentingIntervenerThreeBarrister,
    boolean isRepresentingIntervenerFourSolicitor, boolean isRepresentingIntervenerFourBarrister,
    int intervenerIndex
) {

    public boolean isRepresentingAnyInterveners() {
        return isRepresentingIntervenerOneSolicitor
            || isRepresentingIntervenerTwoSolicitor
            || isRepresentingIntervenerThreeSolicitor
            || isRepresentingIntervenerFourSolicitor
            || isRepresentingIntervenerOneBarrister
            || isRepresentingIntervenerTwoBarrister
            || isRepresentingIntervenerThreeBarrister
            || isRepresentingIntervenerFourBarrister;
    }

    public boolean isRepresentingAnyIntervenerBarristers() {
        return isRepresentingIntervenerOneBarrister
            || isRepresentingIntervenerTwoBarrister
            || isRepresentingIntervenerThreeBarrister
            || isRepresentingIntervenerFourBarrister;
    }
}
