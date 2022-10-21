package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum CaseDocumentParty {
    APPLICANT("applicant"),
    RESPONDENT("respondent"),
    CASE("case");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static CaseDocumentParty forValue(String value) {
        return Arrays.stream(CaseDocumentParty.values())
            .filter(option -> value.equalsIgnoreCase(option.getValue()))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
