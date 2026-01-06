package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;

@Value
@Builder(toBuilder = true)
public class ChangeOfRepresentationRequest {

    public static final String APPLICANT_PARTY = "Applicant";
    public static final String RESPONDENT_PARTY = "Respondent";
    public static final String INTERVENER_ONE_PARTY = "Intervener 1";
    public static final String INTERVENER_TWO_PARTY = "Intervener 2";
    public static final String INTERVENER_THREE_PARTY = "Intervener 3";
    public static final String INTERVENER_FOUR_PARTY = "Intervener 4";

    RepresentationUpdateHistory current;
    String party;
    String clientName;
    ChangedRepresentative addedRepresentative;
    ChangedRepresentative removedRepresentative;
    String by;
    String via;

    public static String getIntervenerPartyByType(IntervenerType intervenerType) {
        return switch (intervenerType) {
            case INTERVENER_ONE -> INTERVENER_ONE_PARTY;
            case INTERVENER_TWO -> INTERVENER_TWO_PARTY;
            case INTERVENER_THREE -> INTERVENER_THREE_PARTY;
            case INTERVENER_FOUR -> INTERVENER_FOUR_PARTY;
        };
    }

    public static String getIntervenerPartyByIndex(int index) {
        return switch (index) {
            case 1 -> INTERVENER_ONE_PARTY;
            case 2 -> INTERVENER_TWO_PARTY;
            case 3 -> INTERVENER_THREE_PARTY;
            case 4 -> INTERVENER_FOUR_PARTY;
            default -> null;
        };
    }
}
