package uk.gov.hmcts.reform.finrem.caseorchestration.model.bulkscan.validation.in;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OcrDataField {

    @JsonProperty
    public final String name;

    @JsonProperty
    public final String value;

    @JsonCreator
    public OcrDataField(
            @JsonProperty("name") String name,
            @JsonProperty("value") String value
    ) {
        this.name = name;
        this.value = value;
    }
}

