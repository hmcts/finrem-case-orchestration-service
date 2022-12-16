package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum TypeOfApplicantion {

    @JsonEnumDefaultValue
    TYPE_OF_APPLICATION_DEFAULT_TO("In connection to matrimonial and civil partnership proceedings"),
    SCHEDULE_ONE("Under paragraph 1 or 2 of schedule 1 children act 1989");

    private final String typeOfApplication;

    @JsonValue
    public String getTypeOfApplication() {
        return typeOfApplication;
    }

    public static TypeOfApplicantion forValue(String value) {
        return Arrays.stream(TypeOfApplicantion.values())
            .filter(option -> value.equalsIgnoreCase(option.getTypeOfApplication()))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
