package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum Schedule1OrMatrimonialAndCpList {

    MATRIMONIAL_AND_CIVIL_PARTNERSHIP_PROCEEDINGS("In connection to matrimonial and civil partnership proceedings",
        "In connection to matrimonial and civil partnership proceedings (divorce/dissolution etc)"),
    SCHEDULE_1_CHILDREN_ACT_1989("Under paragraph 1 or 2 of schedule 1 children act 1989", "Under paragraph 1 or 2 of schedule 1 children act 1989");

    private final String value;
    private final String text;

    @JsonValue
    public String getValue() {
        return value;
    }

    public String getText() {
        return text;
    }

    public static Schedule1OrMatrimonialAndCpList forValue(String value) {
        return Arrays.stream(Schedule1OrMatrimonialAndCpList.values())
            .filter(option -> option.getValue().equalsIgnoreCase(value))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
