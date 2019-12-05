package uk.gov.hmcts.reform.finrem.caseorchestration.model.scan.validation.in;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class OcrDataField {

    @JsonProperty
    private final String name;

    @JsonProperty
    private final String value;

    @JsonCreator
    public OcrDataField(
        @JsonProperty("name") String name,
        @JsonProperty("value") String value
    ) {
        this.name = name;
        this.value = value;
    }
}
