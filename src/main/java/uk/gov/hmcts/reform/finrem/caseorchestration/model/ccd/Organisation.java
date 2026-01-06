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

    /**
     * Determines whether two organisations are the same by comparing their organisation IDs.
     *
     * <p>
     * The comparison is null-safe. If an organisation or its ID is {@code null},
     * a default value is used to prevent {@link NullPointerException}.
     *
     * @param org1 the first organisation
     * @param org2 the second organisation
     * @return {@code true} if both organisations have the same organisation ID;
     *         {@code false} otherwise
     */
    public static boolean isSameOrganisation(Organisation org1, Organisation org2) {
        return nullSafeOrganisationId(org1, " ")
            .equals(nullSafeOrganisationId(org2, "  "));
    }

    private static String nullSafeOrganisationId(Organisation organisation, String defaultOrdId) {
        return ofNullable(organisation).map(Organisation::getOrganisationID).orElse(defaultOrdId);
    }
}
