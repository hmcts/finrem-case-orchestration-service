package uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrganisationContactInformation {
    @JsonProperty(value = "addressLine1")
    private String addressLine1;
    @JsonProperty(value = "addressLine2")
    private String addressLine2;
    @JsonProperty(value = "addressLine3")
    private String addressLine3;
    @JsonProperty(value = "country")
    private String country;
    @JsonProperty(value = "county")
    private String county;
    @JsonProperty(value = "postCode")
    private String postcode;
    @JsonProperty(value = "townCity")
    private String townCity;
}