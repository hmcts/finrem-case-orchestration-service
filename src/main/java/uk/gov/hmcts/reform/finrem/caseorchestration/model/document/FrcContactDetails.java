package uk.gov.hmcts.reform.finrem.caseorchestration.model.document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@EqualsAndHashCode
public class FrcContactDetails {
    @JsonProperty("courtName")
    private String courtName;

    @JsonProperty("careOf")
    private String careOf;

    @JsonProperty("poBox")
    private String poBox;

    @JsonProperty("town")
    private String town;

    @JsonProperty("postcode")
    private String postcode;

    @JsonProperty("emailAddress")
    private String emailAddress;

    @JsonProperty("phoneNumber")
    private String phoneNumber;
}
