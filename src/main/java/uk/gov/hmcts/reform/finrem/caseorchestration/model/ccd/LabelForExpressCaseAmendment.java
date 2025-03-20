package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum LabelForExpressCaseAmendment {

    SUITABLE_FOR_EXPRESS_LABEL("suitableForExpressCaseAmendmentLabel"),
    UNSUITABLE_FOR_EXPRESS_LABEL("unsuitableForExpressCaseAmendmentLabel"),
    SHOW_NEITHER_PAGE_NOR_LABEL("noLabel");

    private final String value;

    /*
     * This value will be a label name.  It will match a label in CCD definitions, for when amending a case.
     * The value is used by a show condition, to determine whether the label should be shown,
     */
    @JsonValue
    public String getValue() {
        return value;
    }

    public static LabelForExpressCaseAmendment forValue(String value) {
        return Arrays.stream(LabelForExpressCaseAmendment.values())
            .filter(option -> option.getValue().equalsIgnoreCase(value))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
