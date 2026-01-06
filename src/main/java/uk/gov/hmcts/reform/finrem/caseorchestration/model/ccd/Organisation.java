package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static java.util.Optional.ofNullable;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class Organisation {
    @JsonProperty("OrganisationID")
    private String organisationID;
    @JsonProperty("OrganisationName")
    private String organisationName;

    public static Organisation organisation(String id) {
        return Organisation.builder()
            .organisationID(id)
            .build();
    }

    public static boolean isSameOrganisation(Organisation org1, Organisation org2) {
        if (org1 == null || org2 == null) {
            return false;
        }

        String orgId1 = org1.getOrganisationID();
        String orgId2 = org2.getOrganisationID();

        if (orgId1 == null || orgId2 == null) {
            return false;
        }

        if (orgId1.isBlank() || orgId2.isBlank()) {
            return false;
        }

        return orgId1.equals(orgId2);
    }
}
