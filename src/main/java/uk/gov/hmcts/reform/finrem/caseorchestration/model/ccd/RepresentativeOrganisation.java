package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RepresentativeOrganisation {
    @JsonProperty("OrganisationName")
    private final String organisationName;

    @JsonProperty("OrganisationAddress")
    private final Address organisationAddress;

}
