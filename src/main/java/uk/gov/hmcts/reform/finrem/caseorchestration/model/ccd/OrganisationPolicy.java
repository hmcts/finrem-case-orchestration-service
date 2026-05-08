package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class OrganisationPolicy {
    @JsonProperty("Organisation")
    private Organisation organisation;

    @JsonProperty("OrgPolicyCaseAssignedRole")
    private String orgPolicyCaseAssignedRole;

    @JsonProperty("OrgPolicyReference")
    private String orgPolicyReference;

    @JsonIgnore
    public Organisation getOrganisation() {
        if (organisation == null) {
            return Organisation.builder().build();
        } else
            return organisation;
    }

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

    /**
     * Determines whether two {@link OrganisationPolicy} instances refer to the same organisation.
     *
     * <p>This method safely extracts the {@link Organisation} from each policy (handling {@code null}
     * values) and delegates the comparison to {@link Organisation#isSameOrganisation(Organisation, Organisation)}.
     *
     * @param organisationPolicy1 the first organisation policy, may be {@code null}
     * @param organisationPolicy2 the second organisation policy, may be {@code null}
     * @return {@code true} if both policies reference the same organisation or are both {@code null};
     *         {@code false} otherwise
     */
    public static boolean isSameOrganisation(OrganisationPolicy organisationPolicy1, OrganisationPolicy organisationPolicy2) {
        return Organisation.isSameOrganisation(
            ofNullable(organisationPolicy1)
                .map(OrganisationPolicy::getOrganisation).orElse(null),
            ofNullable(organisationPolicy2)
                .map(OrganisationPolicy::getOrganisation).orElse(null));
    }
}
