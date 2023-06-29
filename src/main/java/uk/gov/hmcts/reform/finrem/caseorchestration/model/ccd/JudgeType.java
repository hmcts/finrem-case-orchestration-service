package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum JudgeType {
    DISTRICT_JUDGE("District Judge"),
    DEPUTY_DISTRICT_JUDGE("Deputy District Judge"),
    HIS_HONOUR_JUDGE("His Honour Judge"),
    HER_HONOUR_JUDGE("Her Honour Judge"),
    RECORDER("Recorder"),
    PROPER_OFFICER_OF_THE_COURT("Proper Officer of the Court"),
    THE_HONOURABLE_MR_JUSTICE("The Honourable Mr Justice"),
    THE_HONOURABLE_MRS_JUSTICE("The Honourable Mrs Justice"),
    THE_HONOURABLE_MS_JUSTICE("The Honourable Ms Justice");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static JudgeType forValue(String value) {
        return Arrays.stream(JudgeType.values())
            .filter(option -> option.getValue().equalsIgnoreCase(value))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
