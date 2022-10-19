package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum Region {
    MIDLANDS("midlands"),
    LONDON("london"),
    NORTHWEST("northwest"),
    NORTHEAST("northeast"),
    SOUTHEAST("southeast"),
    SOUTHWEST("southwest"),
    WALES("wales");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }
}
