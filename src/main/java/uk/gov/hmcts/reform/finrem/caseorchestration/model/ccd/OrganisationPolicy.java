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
public class OrganisationPolicy {
    @JsonProperty("Organisation")
    private Organisation organisation;

    @JsonProperty("OrgPolicyCaseAssignedRole")
    private String orgPolicyCaseAssignedRole;

    @JsonProperty("OrgPolicyReference")
    private String orgPolicyReference;

    /**
     * Creates a default {@link OrganisationPolicy} for the specified case role.
     *
     * <p>The returned policy contains an empty {@link Organisation} with
     * {@code organisationID} and {@code organisationName} set to {@code null},
     * and no organisation policy reference. The {@code orgPolicyCaseAssignedRole}
     * is populated using the CCD role code from the provided {@link CaseRole}.</p>
     *
     * <p>This is typically used to initialise or reset organisation policy fields
     * where an organisation has not yet been assigned but a case role must be
     * present.</p>
     *
     * @param role the {@link CaseRole} whose CCD role code will be assigned to the policy
     * @return a default {@link OrganisationPolicy} instance with the specified role
     */
    public static OrganisationPolicy getDefaultOrganisationPolicy(CaseRole role) {
        return OrganisationPolicy.builder()
            .organisation(Organisation.builder().organisationID(null).organisationName(null).build())
            .orgPolicyReference(null)
            .orgPolicyCaseAssignedRole(role.getCcdCode())
            .build();
    }
}
