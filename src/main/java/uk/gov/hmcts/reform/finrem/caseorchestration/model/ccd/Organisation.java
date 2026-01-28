package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    /**
     * Checks whether two {@link Organisation} objects represent the same organisation.
     *
     * <p>
     * Two organisations are considered the same if:
     * <ul>
     *   <li>Both organisation objects are not {@code null}</li>
     *   <li>Both organisation IDs are not {@code null}</li>
     *   <li>Both organisation IDs are not blank</li>
     *   <li>The organisation IDs are equal</li>
     * </ul>
     *
     * @param org1 the first organisation to compare
     * @param org2 the second organisation to compare
     * @return {@code true} if both organisations have the same non-blank organisation ID;
     *         {@code false} otherwise
     */
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
