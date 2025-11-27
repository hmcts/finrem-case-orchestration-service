package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum NoticeOfChangeParty {
    APPLICANT("applicant"),
    RESPONDENT("respondent");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static NoticeOfChangeParty forValue(String value) {
        return Arrays.stream(NoticeOfChangeParty.values())
            .filter(option -> option.getValue().equalsIgnoreCase(value))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }

    /**
     * Checks if the party selected for the change in representation
     * is the applicant.
     *
     * @param finremCaseData the case data containing the party selection
     * @return true if the selected party is the applicant; false otherwise
     */
    public static boolean isApplicantForRepresentationChange(FinremCaseData finremCaseData) {
        return APPLICANT.equals(finremCaseData.getContactDetailsWrapper().getNocParty());
    }

    /**
     * Checks if the party selected for the change in representation
     * is the respondent.
     *
     * @param finremCaseData the case data containing the party selection
     * @return true if the selected party is the respondent; false otherwise
     */
    public static boolean isRespondentForRepresentationChange(FinremCaseData finremCaseData) {
        return RESPONDENT.equals(finremCaseData.getContactDetailsWrapper().getNocParty());
    }
}
