package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ChangeOrganisationRequest {
    @JsonProperty("NotesReason")
    private String reason;

    @JsonProperty("CaseRoleId")
    private String caseRoleId;

    @JsonProperty("RequestTimestamp")
    private LocalDateTime requestTimestamp;

    @JsonProperty("ApprovalRejectionTimeStamp")
    private LocalDateTime approvalRejectionTimestamp;

    @JsonProperty("OrganisationToAdd")
    private Organisation organisationToAdd;

    @JsonProperty("OrganisationToRemove")
    private Organisation organisationToRemove;

    @JsonProperty("ApprovalStatus")
    private int approvalStatus;

}
