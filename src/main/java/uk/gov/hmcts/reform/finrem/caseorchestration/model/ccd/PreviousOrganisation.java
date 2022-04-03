package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
public class PreviousOrganisation {
    @JsonProperty("FromTimeStamp")
    private LocalDateTime from;

    @JsonProperty("ToTimeStamp")
    private LocalDateTime to;

    @JsonProperty("OrganisationName")
    private String organisationName;

    @JsonProperty("OrganisationAddress")
    private String organisationAddress;
}
