package uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrganisationsResponse {
    @JsonProperty(value = "contactInformation")
    private List<OrganisationContactInformation> contactInformation;
    @JsonProperty(value = "name")
    private String name;
    @JsonProperty(value = "organisationIdentifier")
    private String organisationIdentifier;
}
